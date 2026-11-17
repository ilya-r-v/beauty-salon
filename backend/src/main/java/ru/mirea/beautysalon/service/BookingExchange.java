package ru.mirea.beautysalon.service;

import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import java.io.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.mirea.beautysalon.domain.BookingStatus;
import ru.mirea.beautysalon.dto.booking.*;
import ru.mirea.beautysalon.exception.*;
import ru.mirea.beautysalon.filter.BookingFilter;

@Service
@RequiredArgsConstructor
public class BookingExchange {

    private final BookingService bookings;
    private final TransactionTemplate transactions;
    private final JdbcTemplate db;
    private final Validator validation;

    public enum Action {
        ARCHIVE,
        RESTORE,
        STATUS,
        ADD_TAG,
        REMOVE_TAG,
    }

    public record Bulk(
        @NotEmpty List<@NotNull UUID> ids,
        @NotNull Action action,
        BookingStatus status,
        @Size(max = 50) String tag
    ) {}

    public record BulkResult(List<UUID> success, Map<UUID, String> errors) {}

    public BulkResult bulk(Bulk command) {
        var success = new ArrayList<UUID>();
        var errors = new LinkedHashMap<UUID, String>();
        for (var id : new LinkedHashSet<>(command.ids())) {
            try {
                transactions.executeWithoutResult(tx -> perform(id, command));
                success.add(id);
            } catch (RuntimeException ex) {
                errors.put(id, reason(ex));
            }
        }
        return new BulkResult(success, errors);
    }

    private void perform(UUID id, Bulk cmd) {
        var b = bookings.getBookingById(id);
        switch (cmd.action()) {
            case ARCHIVE -> bookings.deleteBooking(id);
            case RESTORE -> bookings.restore(id);
            case STATUS -> bookings.updateStatus(id, new StatusRequest(b.getVersion(), cmd.status()));
            case ADD_TAG, REMOVE_TAG -> {
                if (cmd.tag() == null || cmd.tag().isBlank()) throw new BusinessException("Укажите тег");
                var tags = new HashSet<>(b.getTags());
                if (cmd.action() == Action.ADD_TAG) tags.add(cmd.tag().strip());
                else tags.remove(cmd.tag().strip());
                var change = new BookingEdit(b.getVersion(), b.getComment(), b.isPrepaid(), tags);
                validate(change);
                bookings.edit(id, change);
            }
        }
    }

    private static final String[] HEADER = {
        "code",
        "clientId",
        "slotId",
        "variantId",
        "comment",
        "prepaid",
        "tags",
        "version",
        "status",
        "price",
        "archived",
    };

    @Transactional(
        readOnly = true,
        isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ
    )
    public String export(BookingFilter filter) throws IOException {
        var output = new StringWriter();
        try (var csv = new CSVPrinter(output, CSVFormat.RFC4180)) {
            csv.printRecord((Object[]) HEADER);
            for (int n = 0; ; n++) {
                var page = bookings.list(filter, n, 100);
                for (var b : page.content())
                    csv.printRecord(
                        b.getExternalCode(),
                        b.getClientId(),
                        b.getSlot().getId(),
                        b.getServiceVariantId(),
                        b.getComment(),
                        b.isPrepaid(),
                        jsonTags(b.getTags()),
                        b.getVersion(),
                        b.getStatus(),
                        b.getPrice(),
                        b.isArchived()
                    );
                if ((long) (n + 1) * 100 >= page.totalElements()) break;
            }
        }
        return output.toString();
    }

    private final com.fasterxml.jackson.databind.ObjectMapper json;

    private String jsonTags(Set<String> tags) throws IOException {
        return json.writeValueAsString(tags);
    }

    public enum Mode {
        NEW,
        UPSERT,
    }

    public record Preview(UUID previewId, int valid, int invalid, Map<Long, String> errors) {}

    public Preview preview(UUID actor, String data, Mode mode) throws IOException {
        var errors = new LinkedHashMap<Long, String>();
        var codes = new HashSet<String>();
        var slots = new HashSet<UUID>();
        var records = parse(data);
        for (var row : records) {
            try {
                String code = row.get("code");
                if (!codes.add(code)) throw new BusinessException("Повтор кода в файле");
                transactions.executeWithoutResult(tx -> {
                    importRow(row, mode);
                    tx.setRollbackOnly();
                });
                if (
                    !row.get("status").equals("CANCELLED") && !slots.add(UUID.fromString(row.get("slotId")))
                ) throw new BusinessException("Повтор слота в файле");
            } catch (RuntimeException ex) {
                errors.put(row.getRecordNumber(), reason(ex));
            }
        }
        UUID id = errors.isEmpty()
            ? db.queryForObject(
                  "insert into import_preview(actor,csv,mode) values(?,?,?) returning id",
                  UUID.class,
                  actor,
                  data,
                  mode.name()
              )
            : null;
        return new Preview(id, records.size() - errors.size(), errors.size(), errors);
    }

    @Transactional
    public int apply(UUID actor, UUID id) throws IOException {
        var previews = db.queryForList(
            "delete from import_preview where id=? and actor=? returning csv,mode",
            id,
            actor
        );
        if (previews.isEmpty()) throw new ConflictException("Предпросмотр не найден или уже применён");
        var p = previews.getFirst();
        var records = parse((String) p.get("csv"));
        for (var row : records) importRow(row, Mode.valueOf((String) p.get("mode")));
        return records.size();
    }

    private List<CSVRecord> parse(String data) throws IOException {
        try (
            var parser = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get()
                .parse(new StringReader(data))
        ) {
            if (!parser.getHeaderNames().equals(List.of(HEADER))) throw new BusinessException(
                "Заголовок CSV должен совпадать с экспортом"
            );
            var rows = parser.getRecords();
            if (rows.isEmpty()) throw new BusinessException(
                "CSV не содержит записей"
            );
            return rows;
        } catch (UncheckedIOException | org.apache.commons.csv.CSVException ex) {
            throw new BusinessException("Некорректный CSV");
        }
    }

    private boolean bool(String text) {
        if (!Set.of("true", "false").contains(text)) throw new BusinessException("Допустимы true и false");
        return Boolean.parseBoolean(text);
    }

    private void importRow(CSVRecord r, Mode mode) {
        if (!r.isConsistent()) throw new BusinessException("Неверное число столбцов");
        String code = r.get("code").strip();
        if (code.isBlank() || code.length() > 80) throw new BusinessException(
            "Укажите код длиной до 80 символов"
        );
        Set<String> tags;
        try {
            tags = json.readValue(
                r.get("tags"),
                new com.fasterxml.jackson.core.type.TypeReference<Set<String>>() {}
            );
        } catch (IOException ex) {
            throw new BusinessException("Теги должны быть массивом JSON");
        }
        var ids = db.queryForList("select id from booking where external_code=?", UUID.class, code);
        var client = UUID.fromString(r.get("clientId"));
        var slot = UUID.fromString(r.get("slotId"));
        var variant = UUID.fromString(r.get("variantId"));
        var status = BookingStatus.valueOf(r.get("status"));
        boolean archived = bool(r.get("archived"));
        boolean prepaid = bool(r.get("prepaid"));
        var price = new java.math.BigDecimal(r.get("price"));
        if (
            price.signum() <= 0 || price.scale() > 2 || price.precision() - price.scale() > 8
        ) throw new BusinessException("Неверная цена");
        if (ids.isEmpty()) {
            var create = CreateBookingRequest.builder()
                .externalCode(code)
                .clientId(client)
                .slotId(slot)
                .serviceVariantId(variant)
                .comment(r.get("comment"))
                .prepaid(prepaid)
                .tags(tags)
                .build();
            validate(create);
            if (tags == null) throw new BusinessException("Укажите массив тегов");
            bookings.create(create, status, archived, price);
        } else {
            if (mode == Mode.NEW) throw new ConflictException("Запись с таким кодом уже есть");
            var b = bookings.getBookingById(ids.getFirst());
            if (
                !b.getClientId().equals(client) ||
                !b.getSlot().getId().equals(slot) ||
                !b.getServiceVariantId().equals(variant) ||
                b.getPrice().compareTo(price) != 0
            ) throw new BusinessException("Клиент, слот, услуга и цена существующей записи фиксированы");
            var edit = new BookingEdit(Long.valueOf(r.get("version")), r.get("comment"), prepaid, tags);
            validate(edit);
            var updated = bookings.edit(b.getId(), edit);
            if (status != updated.getStatus()) bookings.updateStatus(
                b.getId(),
                new StatusRequest(updated.getVersion(), status)
            );
            if (archived != b.isArchived()) {
                if (archived) bookings.deleteBooking(b.getId());
                else bookings.restore(b.getId());
            }
        }
    }

    private void validate(Object dto) {
        var errors = validation.validate(dto);
        if (!errors.isEmpty()) throw new BusinessException(errors.iterator().next().getMessage());
    }

    private String reason(RuntimeException ex) {
        if (
            ex instanceof BusinessException ||
            ex instanceof ConflictException ||
            ex instanceof EntityNotFoundException
        ) return ex.getMessage();
        return "Некорректные данные или конфликт записи";
    }
}
