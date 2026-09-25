package ru.mirea.beautysalon;

import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.BookingStatus;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.BookingRepositoryJdbc;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.ClientRepositoryJdbc;
import ru.mirea.beautysalon.service.BookingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final BookingRepository bookingRepository = new BookingRepositoryJdbc();
    private static final ClientRepository clientRepository = new ClientRepositoryJdbc();
    private static final BookingService bookingService = new BookingService(bookingRepository, clientRepository);

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> System.out.println("TODO: меню 'Клиенты и мастера' — реализует участник команды, отвечающий за Client/MasterProfile");
                case "2" -> System.out.println("TODO: меню 'Услуги' — Service/ServiceType/ServiceVariant, аналогично Booking по структуре");
                case "3" -> bookingMenu();
                case "4" -> searchMenu();
                case "5" -> filterMenu();
                case "6" -> statisticsMenu();
                case "7" -> System.out.println("TODO: экспорт в Excel через util/ExcelExporter (Apache POI уже подключен в pom.xml)");
                case "8" -> System.out.println("TODO: вывод списка таблиц БД (SELECT table_name FROM information_schema.tables)");
                case "0" -> running = false;
                default -> System.out.println("Неизвестный пункт меню, попробуйте снова.");
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

    //Записи на услугу основная сущность

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
            try {
                switch (choice) {
                    case "1" -> createBooking();
                    case "2" -> printBookings(bookingService.findAll());
                    case "3" -> findBookingById();
                    case "4" -> changeBookingStatus();
                    case "5" -> deleteBooking();
                    case "0" -> back = true;
                    default -> System.out.println("Неизвестный пункт меню.");
                }
            } catch (BusinessException | EntityNotFoundException e) {
                // Ожидаемые ошибки
                System.out.println("Ошибка: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
    }

    private static void createBooking() {
        UUID clientId = readUuid("ID клиента: ");
        UUID masterId = readUuid("ID мастера: ");
        UUID serviceVariantId = readUuid("ID варианта услуги: ");
        LocalDateTime time = readDateTime("Дата и время (yyyy-MM-ddTHH:mm), напр. 2026-10-01T14:00: ");

        Booking booking = new Booking();
        booking.setClientId(clientId);
        booking.setMasterId(masterId);
        booking.setServiceVariantId(serviceVariantId);
        booking.setTime(time);
        booking.setStatus(BookingStatus.PENDING);

        Booking created = bookingService.create(booking);
        System.out.println("Запись создана: " + created);
    }

    private static void findBookingById() {
        UUID id = readUuid("ID записи: ");
        Booking booking = bookingService.getById(id);
        System.out.println(booking);
    }

    private static void changeBookingStatus() {
        UUID id = readUuid("ID записи: ");
        System.out.print("Новый статус (PENDING/CONFIRMED/COMPLETED/CANCELLED): ");
        BookingStatus status = readStatus();
        bookingService.changeStatus(id, status);
        System.out.println("Статус обновлён.");
    }

    private static void deleteBooking() {
        UUID id = readUuid("ID записи для удаления: ");
        bookingService.delete(id);
        System.out.println("Запись удалена.");
    }

    //Поиск

    private static void searchMenu() {
        System.out.println("\n--- Поиск записей ---");
        System.out.println("1. По имени клиента");
        System.out.println("2. По названию услуги");
        System.out.print("Выберите способ поиска: ");
        String choice = scanner.nextLine().trim();
        try {
            List<Booking> result = switch (choice) {
                case "1" -> {
                    System.out.print("Имя (или часть имени) клиента: ");
                    yield bookingService.searchByClientName(scanner.nextLine().trim());
                }
                case "2" -> {
                    System.out.print("Название (или часть названия) услуги: ");
                    yield bookingService.searchByServiceTitle(scanner.nextLine().trim());
                }
                default -> List.of();
            };
            printBookings(result);
        } catch (RuntimeException e) {
            System.out.println("Ошибка поиска: " + e.getMessage());
        }
    }

    //Фильтрация

    private static void filterMenu() {
        System.out.println("\n--- Фильтрация записей ---");
        System.out.println("1. По статусу");
        System.out.println("2. По мастеру");
        System.out.println("3. По диапазону дат");
        System.out.print("Выберите фильтр: ");
        String choice = scanner.nextLine().trim();
        try {
            List<Booking> result = switch (choice) {
                case "1" -> {
                    System.out.print("Статус (PENDING/CONFIRMED/COMPLETED/CANCELLED): ");
                    yield bookingService.filterByStatus(readStatus());
                }
                case "2" -> bookingService.filterByMaster(readUuid("ID мастера: "));
                case "3" -> {
                    LocalDateTime from = readDateTime("С (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime to = readDateTime("По (yyyy-MM-ddTHH:mm): ");
                    yield bookingService.filterByDateRange(from, to);
                }
                default -> List.of();
            };
            printBookings(result);
        } catch (RuntimeException e) {
            System.out.println("Ошибка фильтрации: " + e.getMessage());
        }
    }

    //Статистика

    private static void statisticsMenu() {
        List<Booking> all = bookingService.findAll();
        Map<BookingStatus, Long> byStatus = bookingService.countByStatus();

        System.out.println("\n--- Статистика ---");
        System.out.println("Всего записей: " + all.size());
        System.out.println("Ожидают подтверждения (PENDING): " + byStatus.getOrDefault(BookingStatus.PENDING, 0L));
        System.out.println("Подтверждено (CONFIRMED): " + byStatus.getOrDefault(BookingStatus.CONFIRMED, 0L));
        System.out.println("Завершено (COMPLETED): " + byStatus.getOrDefault(BookingStatus.COMPLETED, 0L));
        System.out.println("Отменено (CANCELLED): " + byStatus.getOrDefault(BookingStatus.CANCELLED, 0L));
        // TODO: добавить минимум ещё 1 показатель
    }

    //Утилиты ввода/вывода

    private static void printBookings(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }
        for (Booking b : bookings) {
            System.out.println(b);
        }
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
}
