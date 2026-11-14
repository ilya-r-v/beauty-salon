package ru.mirea.beautysalon;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mirea.beautysalon.service.RefreshSessionService;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class RefreshSessionTests {
    @Autowired RefreshSessionService sessions;
    @Test void rotationConsumesOldTokenAndLogoutRevokesNewToken() {
        String first = sessions.issue(UUID.fromString("10000000-0000-0000-0000-000000000001"));
        var response = sessions.refresh(first);
        assertNotNull(response.getToken());
        assertNotEquals(first, response.getRefreshToken());
        assertThrows(ResponseStatusException.class, () -> sessions.refresh(first));
        sessions.logout(response.getRefreshToken());
        assertThrows(ResponseStatusException.class, () -> sessions.refresh(response.getRefreshToken()));
    }
}