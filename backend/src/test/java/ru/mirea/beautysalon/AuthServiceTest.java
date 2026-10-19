package ru.mirea.beautysalon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mirea.beautysalon.config.JwtTokenProvider;
import ru.mirea.beautysalon.dto.AuthResponse;
import ru.mirea.beautysalon.dto.LoginRequest;
import ru.mirea.beautysalon.dto.RegisterRequest;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.Role;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.RoleRepository;
import ru.mirea.beautysalon.service.AuthService;
import ru.mirea.beautysalon.util.PasswordHasher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RoleRepository roleRepository;

    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    private final UUID roleId = UUID.randomUUID();
    private final Role clientRole = new Role(roleId, "CLIENT");

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "supersecretkeybeauty-salon-must-be-at-least-256-bits-long-key-12345",
                3600000
        );
        authService = new AuthService(clientRepository, roleRepository, jwtTokenProvider);
    }

    @Test
    void testPasswordHasher() {
        String raw = "salon123";
        String hash = PasswordHasher.hash(raw);

        assertTrue(hash.startsWith("pbkdf2$65536$"));
        assertTrue(PasswordHasher.verify(raw, hash));
        assertFalse(PasswordHasher.verify("wrongpass", hash));
    }

    @Test
    void testJwtTokenProvider() {
        Client client = new Client(UUID.randomUUID(), "Анна", "+79001112233", "dummy", roleId);
        String token = jwtTokenProvider.generateToken(client, "CLIENT");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("+79001112233", jwtTokenProvider.getPhoneFromToken(token));
        assertEquals("CLIENT", jwtTokenProvider.getRoleFromToken(token));
        assertEquals("Анна", jwtTokenProvider.getNameFromToken(token));
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest("Иван Иванов", "+79991234567", "secret123", "secret123");

        when(clientRepository.findByPhone("+79991234567")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(clientRole));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals("Иван Иванов", res.getName());
        assertEquals("+79991234567", res.getPhone());
        assertEquals("CLIENT", res.getRole());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void testRegisterPasswordMismatch() {
        RegisterRequest req = new RegisterRequest("Иван", "+79991234567", "secret123", "different");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertEquals("Пароли не совпадают", ex.getMessage());
    }

    @Test
    void testRegisterInvalidPhone() {
        RegisterRequest req = new RegisterRequest("Иван", "89991234567", "secret123", "secret123");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("+7XXXXXXXXXX"));
    }

    @Test
    void testLoginSuccess() {
        String hash = PasswordHasher.hash("salon123");
        Client client = new Client(UUID.randomUUID(), "Мария", "+79001112233", hash, roleId);

        when(clientRepository.findByPhone("+79001112233")).thenReturn(Optional.of(client));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(clientRole));

        LoginRequest req = new LoginRequest("+79001112233", "salon123");
        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals("Мария", res.getName());
        assertEquals("+79001112233", res.getPhone());
        assertEquals("CLIENT", res.getRole());
    }

    @Test
    void testLoginInvalidPassword() {
        String hash = PasswordHasher.hash("salon123");
        Client client = new Client(UUID.randomUUID(), "Мария", "+79001112233", hash, roleId);

        when(clientRepository.findByPhone("+79001112233")).thenReturn(Optional.of(client));

        LoginRequest req = new LoginRequest("+79001112233", "wrongpassword");
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(req));
        assertEquals("Неверный телефон или пароль", ex.getMessage());
    }
}
