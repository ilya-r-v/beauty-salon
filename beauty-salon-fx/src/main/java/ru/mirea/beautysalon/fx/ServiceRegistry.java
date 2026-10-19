package ru.mirea.beautysalon.fx;

import ru.mirea.beautysalon.client.AuthRestClient;
import ru.mirea.beautysalon.repository.*;
import ru.mirea.beautysalon.service.*;
import ru.mirea.beautysalon.util.ExcelExporter;

import java.io.InputStream;
import java.util.Properties;

// Точка доступа к репозиториям и сервисам приложения.
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

    // JDBC реализация авторизации (оригинальная)
    private static final AuthService AUTH_SERVICE =
            new AuthService(CLIENT_REPOSITORY, ROLE_REPOSITORY);

    // REST реализация авторизации (через Spring Boot)
    private static final AuthRestClient AUTH_REST_CLIENT;
    private static final RestAuthService REST_AUTH_SERVICE;

    private static final ServiceCatalogService SERVICE_CATALOG_SERVICE =
            new ServiceCatalogService(SERVICE_TYPE_REPOSITORY, SERVICE_ENTITY_REPOSITORY,
                    SERVICE_VARIANT_REPOSITORY, MASTER_PROFILE_REPOSITORY);

    private static final ExcelExporter EXCEL_EXPORTER =
            new ExcelExporter(CLIENT_REPOSITORY, SERVICE_VARIANT_REPOSITORY);

    private static boolean useRestAuth = false;

    static {
        String backendUrl = "http://localhost:8080/api";
        try (InputStream in = ServiceRegistry.class.getResourceAsStream("/database.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String mode = props.getProperty("auth.mode", "jdbc");
                useRestAuth = "rest".equalsIgnoreCase(mode);
                if (props.getProperty("backend.url") != null && !props.getProperty("backend.url").isBlank()) {
                    backendUrl = props.getProperty("backend.url").trim();
                }
            }
        } catch (Exception ignored) {}

        AUTH_REST_CLIENT = new AuthRestClient(backendUrl);
        REST_AUTH_SERVICE = new RestAuthService(AUTH_REST_CLIENT, CLIENT_REPOSITORY, ROLE_REPOSITORY);
    }

    private ServiceRegistry() {}

    public static BookingService bookingService() { return BOOKING_SERVICE; }
    public static ClientService clientService() { return CLIENT_SERVICE; }

    // Возвращает активный сервис авторизации в зависимости от режима (auth.mode=rest или jdbc)
    public static AuthService authService() {
        return useRestAuth ? REST_AUTH_SERVICE : AUTH_SERVICE;
    }

    // Прямой доступ к конкретным реализациям
    public static AuthService jdbcAuthService() { return AUTH_SERVICE; }
    public static RestAuthService restAuthService() { return REST_AUTH_SERVICE; }
    public static AuthRestClient authRestClient() { return AUTH_REST_CLIENT; }

    public static boolean isUseRestAuth() { return useRestAuth; }
    public static void setUseRestAuth(boolean rest) { useRestAuth = rest; }

    public static ServiceCatalogService serviceCatalogService() { return SERVICE_CATALOG_SERVICE; }
    public static RoleRepository roleRepository() { return ROLE_REPOSITORY; }
    public static ExcelExporter excelExporter() { return EXCEL_EXPORTER; }
}
