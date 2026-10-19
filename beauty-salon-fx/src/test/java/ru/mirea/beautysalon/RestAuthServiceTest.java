package ru.mirea.beautysalon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mirea.beautysalon.client.AuthRestClient;
import ru.mirea.beautysalon.client.dto.AuthResponseDto;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.fx.Session;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.service.AuthService;
import ru.mirea.beautysalon.service.RestAuthService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestAuthServiceTest {

    static class MockAuthRestClient extends AuthRestClient {
        private final UUID userId = UUID.randomUUID();

        @Override
        public AuthResponseDto login(String phone, String password) {
            return new AuthResponseDto("jwt.mock.token", "Bearer", userId, "Мария Иванова", phone, "MASTER");
        }

        @Override
        public AuthResponseDto register(String name, String phone, String password, String passwordConfirm) {
            return new AuthResponseDto("jwt.mock.token.reg", "Bearer", UUID.randomUUID(), name, phone, "CLIENT");
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    private RestAuthService restAuthService;
    private MockAuthRestClient mockClient;

    @BeforeEach
    void setUp() {
        Session.logout();
        mockClient = new MockAuthRestClient();
        restAuthService = new RestAuthService(mockClient);
    }

    @Test
    void testRestLoginSuccess() {
        Client client = restAuthService.login("+79001112233", "salon123");

        assertNotNull(client);
        assertEquals("+79001112233", client.getPhone());
        assertEquals("Мария Иванова", client.getName());
        assertEquals("MASTER", restAuthService.roleName(client));
        assertEquals("jwt.mock.token", Session.currentToken());
    }

    @Test
    void testRestRegisterSuccess() {
        Client client = restAuthService.register("Новый Клиент", "+79998887766", "password123", "password123");

        assertNotNull(client);
        assertEquals("+79998887766", client.getPhone());
        assertEquals("Новый Клиент", client.getName());
        assertEquals("CLIENT", restAuthService.roleName(client));
        assertEquals("jwt.mock.token.reg", Session.currentToken());
    }

    @Test
    void testServiceRegistryToggle() {
        ServiceRegistry.setUseRestAuth(false);
        AuthService jdbcService = ServiceRegistry.authService();
        assertSame(ServiceRegistry.jdbcAuthService(), jdbcService);

        ServiceRegistry.setUseRestAuth(true);
        AuthService restService = ServiceRegistry.authService();
        assertSame(ServiceRegistry.restAuthService(), restService);
    }
}
