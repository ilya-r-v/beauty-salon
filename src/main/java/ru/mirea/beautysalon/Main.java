package ru.mirea.beautysalon;

import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.*;
import ru.mirea.beautysalon.repository.*;
import ru.mirea.beautysalon.service.BookingService;
import ru.mirea.beautysalon.service.ClientService;
import ru.mirea.beautysalon.service.ServiceCatalogService;
import ru.mirea.beautysalon.util.DatabaseManager;
import ru.mirea.beautysalon.util.ExcelExporter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final ClientRepository clientRepository = new ClientRepositoryJdbc();
    private static final RoleRepository roleRepository = new RoleRepositoryJdbc();
    private static final MasterProfileRepository masterProfileRepository = new MasterProfileRepositoryJdbc();
    private static final BookingRepository bookingRepository = new BookingRepositoryJdbc();
    private static final ServiceTypeRepository serviceTypeRepository = new ServiceTypeRepositoryJdbc();
    private static final ServiceEntityRepository serviceEntityRepository = new ServiceEntityRepositoryJdbc();
    private static final ServiceVariantRepository serviceVariantRepository = new ServiceVariantRepositoryJdbc();

    private static final BookingService bookingService =
            new BookingService(bookingRepository, clientRepository);
    private static final ClientService clientService =
            new ClientService(clientRepository, roleRepository, masterProfileRepository, bookingRepository);
    private static final ServiceCatalogService catalogService =
            new ServiceCatalogService(serviceTypeRepository, serviceEntityRepository, serviceVariantRepository, masterProfileRepository);

    private static final ExcelExporter excelExporter =
            new ExcelExporter(clientRepository, serviceVariantRepository);

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> clientsMenu();
                    case "2" -> servicesMenu();
                    case "3" -> bookingMenu();
                    case "4" -> searchMenu();
                    case "5" -> filterMenu();
                    case "6" -> statisticsMenu();
                    case "7" -> exportMenu();
                    case "8" -> printDatabaseTables();
                    case "0" -> running = false;
                    default -> System.out.println("Неизвестный пункт меню, попробуйте снова.");
                }
            } catch (BusinessException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        System.out.println("До свидания!");
    }

    private static void printMainMenu() {
        System.out.println("\n======================================");
        System.out.println("          САЛОН КРАСОТЫ");
        System.out.println("======================================");
        System.out.println("1. Клиенты и мастера");
        System.out.println("2. Услуги");
        System.out.println("3. Записи на услугу");
        System.out.println("4. Поиск");
        System.out.println("5. Фильтрация");
        System.out.println("6. Статистика");
        System.out.println("7. Экспорт данных");
        System.out.println("8. Вывести таблицы базы данных");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    //Клиенты и мастера

    private static void clientsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Клиенты и мастера ---");
            System.out.println("1. Создать клиента");
            System.out.println("2. Показать всех клиентов");
            System.out.println("3. Найти клиента по ID");
            System.out.println("4. Изменить клиента");
            System.out.println("5. Удалить клиента");
            System.out.println("6. Сделать клиента мастером");
            System.out.println("7. Показать всех мастеров");
            System.out.println("0. Назад");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createClient();
                case "2" -> printClients(clientService.findAll());
                case "3" -> System.out.println(clientService.getById(readUuid("ID клиента: ")));
                case "4" -> updateClient();
                case "5" -> { clientService.delete(readUuid("ID клиента для удаления: ")); System.out.println("Клиент удалён."); }
                case "6" -> promoteToMaster();
                case "7" -> printMasters(clientService.findAllMasters());
                case "0" -> back = true;
                default -> System.out.println("Неизвестный пункт меню.");
            }
        }
    }

    private static void createClient() {
        System.out.print("Имя: ");
        String name = scanner.nextLine().trim();
        System.out.print("Телефон: ");
        String phone = scanner.nextLine().trim();

        List<Role> roles = roleRepository.findAll();
        System.out.println("Доступные роли:");
        roles.forEach(r -> System.out.println("  " + r.getId() + " — " + r.getName()));
        UUID roleId = readUuid("ID роли: ");

        Client client = new Client();
        client.setName(name);
        client.setPhone(phone);
        client.setPasswordHash("changeme");
        client.setRoleId(roleId);

        Client created = clientService.create(client);
        System.out.println("Клиент создан: " + created);
    }

    private static void updateClient() {
        UUID id = readUuid("ID клиента: ");
        Client existing = clientService.getById(id);
        System.out.print("Новое имя (Enter — оставить \"" + existing.getName() + "\"): ");
        String name = scanner.nextLine().trim();
        if (!name.isBlank()) existing.setName(name);
        System.out.print("Новый телефон (Enter — оставить \"" + existing.getPhone() + "\"): ");
        String phone = scanner.nextLine().trim();
        if (!phone.isBlank()) existing.setPhone(phone);
        clientService.update(existing);
        System.out.println("Клиент обновлён.");
    }

    private static void promoteToMaster() {
        UUID clientId = readUuid("ID клиента: ");
        System.out.print("Описание мастера: ");
        String description = scanner.nextLine().trim();
        MasterProfile profile = clientService.promoteToMaster(clientId, description);
        System.out.println("Клиент теперь мастер: " + profile.getClientId());
    }

    //Услуги

    private static void servicesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Услуги ---");
            System.out.println("1. Создать тип услуги");
            System.out.println("2. Показать все типы услуг");
            System.out.println("3. Создать услугу (привязка к мастеру и типу)");
            System.out.println("4. Показать все услуги");
            System.out.println("5. Удалить услугу");
            System.out.println("6. Создать вариант услуги (с ценой)");
            System.out.println("7. Показать все варианты услуг");
            System.out.println("8. Удалить вариант услуги");
            System.out.println("0. Назад");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createServiceType();
                case "2" -> catalogService.findAllTypes().forEach(t -> System.out.println(t.getId() + " — " + t.getName()));
                case "3" -> createService();
                case "4" -> printServices(catalogService.findAllServices());
                case "5" -> { catalogService.deleteService(readUuid("ID услуги для удаления: ")); System.out.println("Услуга удалена."); }
                case "6" -> createServiceVariant();
                case "7" -> printVariants(catalogService.findAllVariants());
                case "8" -> { catalogService.deleteVariant(readUuid("ID варианта для удаления: ")); System.out.println("Вариант удалён."); }
                case "0" -> back = true;
                default -> System.out.println("Неизвестный пункт меню.");
            }
        }
    }

    private static void createServiceType() {
        System.out.print("Название типа услуги: ");
        String name = scanner.nextLine().trim();
        ServiceType type = catalogService.createType(name);
        System.out.println("Тип услуги создан: " + type.getId());
    }

    private static void createService() {
        System.out.print("Название услуги: ");
        String title = scanner.nextLine().trim();
        UUID masterId = readUuid("ID мастера: ");
        UUID typeId = readUuid("ID типа услуги: ");

        ServiceEntity service = new ServiceEntity(null, title, masterId, typeId);
        ServiceEntity created = catalogService.createService(service);
        System.out.println("Услуга создана: " + created.getId());
    }

    private static void createServiceVariant() {
        System.out.print("Название варианта: ");
        String title = scanner.nextLine().trim();
        System.out.print("Описание: ");
        String description = scanner.nextLine().trim();
        System.out.print("Цена: ");
        BigDecimal price = readBigDecimal();
        UUID serviceId = readUuid("ID услуги: ");

        ServiceVariant variant = new ServiceVariant(null, title, description, price, serviceId);
        ServiceVariant created = catalogService.createVariant(variant);
        System.out.println("Вариант услуги создан: " + created.getId());
    }

    //Записи на услугу

    private static void bookingMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Записи на услугу ---");
            System.out.println("1. Создать запись");
            System.out.println("2. Показать все записи");
            System.out.println("3. Найти запись по ID");
            System.out.println("4. Изменить статус записи");
            System.out.println("5. Удалить запись");
            System.out.println("0. Назад");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createBooking();
                case "2" -> printBookings(bookingService.findAll());
                case "3" -> System.out.println(bookingService.getById(readUuid("ID записи: ")));
                case "4" -> changeBookingStatus();
                case "5" -> { bookingService.delete(readUuid("ID записи для удаления: ")); System.out.println("Запись удалена."); }
                case "0" -> back = true;
                default -> System.out.println("Неизвестный пункт меню.");
            }
        }
    }

    private static void createBooking() {
        UUID clientId = readUuid("ID клиента: ");
        UUID serviceVariantId = readUuid("ID варианта услуги: ");
        LocalDateTime time = readDateTime("Дата и время (yyyy-MM-ddTHH:mm), напр. 2026-10-01T14:00: ");

        Booking booking = new Booking();
        booking.setClientId(clientId);
        booking.setServiceVariantId(serviceVariantId);
        booking.setTime(time);
        booking.setStatus(BookingStatus.PENDING);

        Booking created = bookingService.create(booking);
        System.out.println("Запись создана: " + created);
    }

    private static void changeBookingStatus() {
        UUID id = readUuid("ID записи: ");
        System.out.print("Новый статус (PENDING/CONFIRMED/COMPLETED/CANCELLED): ");
        BookingStatus status = readStatus();
        bookingService.changeStatus(id, status);
        System.out.println("Статус обновлён.");
    }

    //Поиск

    private static void searchMenu() {
        System.out.println("\n--- Поиск ---");
        System.out.println("1. Записи — по имени клиента");
        System.out.println("2. Записи — по названию услуги");
        System.out.println("3. Клиенты — по имени");
        System.out.println("4. Клиенты — по телефону");
        System.out.println("5. Услуги — по названию");
        System.out.print("Выберите способ поиска: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> { System.out.print("Имя клиента: "); printBookings(bookingService.searchByClientName(scanner.nextLine().trim())); }
            case "2" -> { System.out.print("Название услуги: "); printBookings(bookingService.searchByServiceTitle(scanner.nextLine().trim())); }
            case "3" -> { System.out.print("Имя: "); printClients(clientService.searchByName(scanner.nextLine().trim())); }
            case "4" -> { System.out.print("Телефон: "); printClients(clientService.searchByPhone(scanner.nextLine().trim())); }
            case "5" -> { System.out.print("Название: "); printServices(catalogService.searchByTitle(scanner.nextLine().trim())); }
            default -> System.out.println("Неизвестный пункт меню.");
        }
    }

    //Фильтрация

    private static void filterMenu() {
        System.out.println("\n--- Фильтрация ---");
        System.out.println("1. Записи — по статусу");
        System.out.println("2. Записи — по мастеру");
        System.out.println("3. Записи — по диапазону дат");
        System.out.println("4. Услуги — по мастеру");
        System.out.println("5. Услуги — по типу");
        System.out.println("6. Варианты услуг — по диапазону цен");
        System.out.print("Выберите фильтр: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> { System.out.print("Статус: "); printBookings(bookingService.filterByStatus(readStatus())); }
            case "2" -> printBookings(bookingService.filterByMaster(readUuid("ID мастера: ")));
            case "3" -> {
                LocalDateTime from = readDateTime("С (yyyy-MM-ddTHH:mm): ");
                LocalDateTime to = readDateTime("По (yyyy-MM-ddTHH:mm): ");
                printBookings(bookingService.filterByDateRange(from, to));
            }
            case "4" -> printServices(catalogService.filterByMaster(readUuid("ID мастера: ")));
            case "5" -> printServices(catalogService.filterByType(readUuid("ID типа услуги: ")));
            case "6" -> {
                System.out.print("Мин. цена: ");
                BigDecimal min = readBigDecimal();
                System.out.print("Макс. цена: ");
                BigDecimal max = readBigDecimal();
                printVariants(catalogService.filterByPriceRange(min, max));
            }
            default -> System.out.println("Неизвестный пункт меню.");
        }
    }

    //Статистика

    private static void statisticsMenu() {
        List<Booking> allBookings = bookingService.findAll();
        Map<BookingStatus, Long> byStatus = bookingService.countByStatus();
        List<Client> allClients = clientService.findAll();
        List<MasterProfile> allMasters = clientService.findAllMasters();
        List<ServiceVariant> allVariants = catalogService.findAllVariants();

        double totalRevenue = 0.0;
        for (Booking booking : allBookings) {
            if (booking.getStatus() != BookingStatus.COMPLETED) {
                continue;
            }
            for (ServiceVariant variant : allVariants) {
                if (variant.getId().equals(booking.getServiceVariantId())) {
                    totalRevenue += variant.getPrice().doubleValue();
                    break;
                }
            }
        }

        System.out.println("\n--- Статистика ---");
        System.out.println("Всего клиентов: " + allClients.size());
        System.out.println("Всего мастеров: " + allMasters.size());
        System.out.println("Всего записей: " + allBookings.size());
        System.out.println("Ожидают подтверждения (PENDING): " + byStatus.getOrDefault(BookingStatus.PENDING, 0L));
        System.out.println("Подтверждено (CONFIRMED): " + byStatus.getOrDefault(BookingStatus.CONFIRMED, 0L));
        System.out.println("Завершено (COMPLETED): " + byStatus.getOrDefault(BookingStatus.COMPLETED, 0L));
        System.out.println("Отменено (CANCELLED): " + byStatus.getOrDefault(BookingStatus.CANCELLED, 0L));
        System.out.printf("Выручка по завершённым записям: %.2f руб.%n", totalRevenue);
    }

    // Экспорт

    private static void exportMenu() {
        System.out.print("Путь для сохранения файла (например, bookings.xlsx): ");
        String path = scanner.nextLine().trim();
        try {
            excelExporter.exportBookings(bookingService.findAll(), path);
            System.out.println("Экспорт завершён: " + path);
        } catch (Exception e) {
            System.out.println("Ошибка экспорта: " + e.getMessage());
        }
    }

    //Таблицы БД

    private static void printDatabaseTables() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\n--- Таблицы базы данных ---");
            while (rs.next()) {
                System.out.println("  " + rs.getString("table_name"));
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении списка таблиц: " + e.getMessage());
        }
    }

    //Вывод

    private static void printBookings(List<Booking> bookings) {
        if (bookings.isEmpty()) { System.out.println("Ничего не найдено."); return; }
        bookings.forEach(System.out::println);
    }

    private static void printClients(List<Client> clients) {
        if (clients.isEmpty()) { System.out.println("Ничего не найдено."); return; }
        clients.forEach(c -> System.out.println(c.getId() + " — " + c));
    }

    private static void printMasters(List<MasterProfile> masters) {
        if (masters.isEmpty()) { System.out.println("Мастеров пока нет."); return; }
        masters.forEach(m -> System.out.println(m.getClientId() + " — рейтинг " + m.getRating() + " — " + m.getDescription()));
    }

    private static void printServices(List<ServiceEntity> services) {
        if (services.isEmpty()) { System.out.println("Ничего не найдено."); return; }
        services.forEach(s -> System.out.println(s.getId() + " — " + s.getTitle()));
    }

    private static void printVariants(List<ServiceVariant> variants) {
        if (variants.isEmpty()) { System.out.println("Ничего не найдено."); return; }
        variants.forEach(v -> System.out.println(v.getId() + " — " + v));
    }


    private static UUID readUuid(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return UUID.fromString(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: введите корректный UUID.");
            }
        }
    }

    private static LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(input, DATE_FMT);
            } catch (Exception e) {
                System.out.println("Ошибка: неверный формат даты/времени.");
            }
        }
    }

    private static BookingStatus readStatus() {
        while (true) {
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return BookingStatus.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.print("Ошибка: недопустимый статус, попробуйте снова: ");
            }
        }
    }

    private static BigDecimal readBigDecimal() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.print("Ошибка: введите корректное число: ");
            }
        }
    }
}
