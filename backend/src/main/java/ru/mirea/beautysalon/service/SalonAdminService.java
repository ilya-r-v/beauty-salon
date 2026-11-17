package ru.mirea.beautysalon.service;

import jakarta.validation.constraints.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.beautysalon.domain.Role;
import ru.mirea.beautysalon.dto.PageResult;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.security.PasswordPolicy;

@Service
@RequiredArgsConstructor
public class SalonAdminService {

    private final JdbcTemplate db;
    private final PasswordEncoder passwords;

    public record UserFields(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = "[+]?[0-9]{7,19}") String phone,
        @NotNull Role role,
        boolean active
    ) {}

    public record UserCreate(
        @jakarta.validation.Valid @NotNull UserFields user,
        @NotBlank String temporaryPassword
    ) {}

    public record Reset(@NotBlank String temporaryPassword) {}

    public PageResult<Map<String, Object>> users(int page, int size) {
        var p = PageResult.request(page, size, org.springframework.data.domain.Sort.unsorted());
        return new PageResult<>(
            db.queryForList(
                "select id,name,phone,role,active from client order by name,id limit ? offset ?",
                size,
                p.getOffset()
            ),
            db.queryForObject("select count(*) from client", Long.class),
            page,
            size
        );
    }

    @Transactional
    public UUID create(UserCreate request) {
        var u = request.user();
        PasswordPolicy.validate(request.temporaryPassword());
        UUID id = db.queryForObject(
            "insert into client(name,phone,role,active,password_hash,password_change_required) values(?,?,?,?,?,true) returning id",
            UUID.class,
            u.name().strip(),
            u.phone(),
            u.role().name(),
            u.active(),
            passwords.encode(request.temporaryPassword())
        );
        ensureMaster(id, u.role());
        return id;
    }

    @Transactional
    public void update(UUID id, UserFields u) {
        require(
            db.update(
                "update client set name=?,phone=?,role=?,active=? where id=?",
                u.name().strip(),
                u.phone(),
                u.role().name(),
                u.active(),
                id
            )
        );
        ensureMaster(id, u.role());
        if (!u.active()) db.update("delete from refresh_session where client_id=?", id);
    }

    private void ensureMaster(UUID id, Role role) {
        if (role == Role.MASTER) db.update(
            "insert into master_profile(client_id) values(?) on conflict do nothing",
            id
        );
    }

    @Transactional
    public void reset(UUID id, Reset request) {
        PasswordPolicy.validate(request.temporaryPassword());
        require(
            db.update(
                "update client set password_hash=?,password_change_required=true, credential_version=credential_version+1 where id=?",
                passwords.encode(request.temporaryPassword()),
                id
            )
        );
        db.update("delete from refresh_session where client_id=?", id);
    }

    private void require(int changed) {
        if (changed == 0) throw new EntityNotFoundException("Пользователь не найден");
    }

    public PageResult<Map<String, Object>> audit(String search, String direction, int page, int size) {
        var p = PageResult.request(page, size, org.springframework.data.domain.Sort.unsorted());
        String term = "%" + search + "%";
        String where =
            " from audit_event a left join client c on c.id=a.actor where concat_ws(' ',c.name,a.actor,a.event,a.entity,a.entity_id,a.description,a.correlation_id) ilike ?";
        String order = org.springframework.data.domain.Sort.Direction.fromString(direction).isAscending()
            ? " asc"
            : " desc";
        return new PageResult<>(
            db.queryForList(
                "select a.*,c.name as actor_name" +
                    where +
                    " order by occurred_at" +
                    order +
                    ",a.id limit ? offset ?",
                term,
                size,
                p.getOffset()
            ),
            db.queryForObject("select count(*)" + where, Long.class, term),
            page,
            size
        );
    }

    public Map<String, Object> summary() {
        var result = db.queryForMap(
            """
            select count(*) as total,count(*) filter(where not archived) as active,
                   count(*) filter(where archived) as archived,count(*) filter(where prepaid) as prepaid,
                   coalesce(round(avg(booked_price),2),0) as average_price from booking
            """
        );
        result.put(
            "byStatus",
            db.queryForList(
                "select status as label,count(*) as count from booking group by status order by status"
            )
        );
        result.put(
            "byPriority",
            db.queryForList(
                "select priority as label,count(*) as count from booking group by priority order by priority"
            )
        );
        result.put(
            "byType",
            db.queryForList(
                """
                select t.name as label,count(b.id) as count from service_type t left join service s on s.service_type_id=t.id
                left join service_variant v on v.service_id=s.id left join booking b on b.service_variant_id=v.id group by t.id order by t.name
                """
            )
        );
        return result;
    }
}
