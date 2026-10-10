package ru.mirea.beautysalon.fx;

import ru.mirea.beautysalon.repository.*;
import ru.mirea.beautysalon.service.BookingService;
import ru.mirea.beautysalon.service.ClientService;
import ru.mirea.beautysalon.service.ServiceCatalogService;
import ru.mirea.beautysalon.util.ExcelExporter;

// Точка доступа к репозиториям и сервисам приложения.
// Каждый FXML-контроллер создаётся FXMLLoader-ом отдельно и не может принять зависимости через конструктор, поэтому все общие объекты
// собраны здесь один раз (упрощённый вариант Service Locator / Singleton)
public final class ServiceRegistry {

    private static final ClientRepository CLIENT_REPOSITORY = new ClientRepositoryJdbc();
    private static final RoleRepository ROLE_REPOSITORY = new RoleRepositoryJdbc();
    private static final MasterProfileRepository MASTER_PROFILE_REPOSITORY = new MasterProfileRepositoryJdbc();
    private static final BookingRepository BOOKING_REPOSITORY = new BookingRepositoryJdbc();
    private static final ServiceTypeRepository SERVICE_TYPE_REPOSITORY = new ServiceTypeRepositoryJdbc();
    private static final ServiceEntityRepository SERVICE_ENTITY_REPOSITORY = new ServiceEntityRepositoryJdbc();
    private static final ServiceVariantRepository SERVICE_VARIANT_REPOSITORY = new ServiceVariantRepositoryJdbc();

    private static final BookingService BOOKING_SERVICE =
            new BookingService(BOOKING_REPOSITORY, CLIENT_REPOSITORY, MASTER_PROFILE_REPOSITORY);
    private static final ClientService CLIENT_SERVICE =
            new ClientService(CLIENT_REPOSITORY, ROLE_REPOSITORY, MASTER_PROFILE_REPOSITORY, BOOKING_REPOSITORY);
    private static final ServiceCatalogService SERVICE_CATALOG_SERVICE =
            new ServiceCatalogService(SERVICE_TYPE_REPOSITORY, SERVICE_ENTITY_REPOSITORY,
                    SERVICE_VARIANT_REPOSITORY, MASTER_PROFILE_REPOSITORY);

    private static final ExcelExporter EXCEL_EXPORTER =
            new ExcelExporter(CLIENT_REPOSITORY, SERVICE_VARIANT_REPOSITORY);

    private ServiceRegistry() {}

    public static BookingService bookingService() { return BOOKING_SERVICE; }
    public static ClientService clientService() { return CLIENT_SERVICE; }
    public static ServiceCatalogService serviceCatalogService() { return SERVICE_CATALOG_SERVICE; }
    public static RoleRepository roleRepository() { return ROLE_REPOSITORY; }
    public static ExcelExporter excelExporter() { return EXCEL_EXPORTER; }
}
