package ru.mirea.beautysalon;

import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.beautysalon.security.JwtTokenProvider;
import ru.mirea.beautysalon.repository.ClientRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class RoleAccessTests {
    @Autowired MockMvc http;
    @Autowired JdbcTemplate db;
    @Autowired ObjectMapper json;
    @Autowired JwtTokenProvider jwt;
    @Autowired ClientRepository clients;
    private String bearer(UUID id) {
        var user=clients.findById(id).orElseThrow();
        user.setPasswordChangeRequired(false);
        clients.saveAndFlush(user);
        return "Bearer "+jwt.generateToken(user);
    }
    @Test void repeatedLoginFailuresThrottleOnlyRapidRetries() throws Exception {
        String phone = "+79991112222";
        http.perform(post("/api/auth/register").contentType("application/json")
            .content("""
                {"name":"Тест","phone":"+79991112222","password":"12345","passwordConfirm":"12345"}
                """)).andExpect(status().is2xxSuccessful());
        for (int attempt = 0; attempt < 5; attempt++) {
            http.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("phone", phone, "password", "wrong"))))
                .andExpect(status().isUnauthorized());
        }
        var login = post("/api/auth/login").contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("phone", phone, "password", "12345")));
        http.perform(login).andExpect(status().isTooManyRequests());
        db.update("update login_attempt set since=clock_timestamp()-interval '1 second' where phone=?", phone);
        http.perform(login).andExpect(status().isOk());
        assertEquals(0, db.queryForObject("select count(*) from login_attempt where phone=?", Integer.class, phone));
    }
    @Test void clientReadsOnlyOwnBookingsAndCannotEditStatus() throws Exception {
        UUID id=UUID.fromString("10000000-0000-0000-0000-000000000001");
        String token=bearer(id);
        var page=http.perform(get("/api/bookings").header("Authorization",token)).andExpect(status().isOk()).andReturn();
        var rows=json.readTree(page.getResponse().getContentAsByteArray()).get("content");
        assertTrue(rows.size()>0);
        for(var row:rows) assertEquals(id.toString(),row.get("clientId").asText());
        UUID other=db.queryForObject("select id from booking where client_id<>? limit 1",UUID.class,id);
        http.perform(get("/api/bookings/"+other).header("Authorization",token)).andExpect(status().isNotFound());
        http.perform(patch("/api/bookings/"+other+"/status").header("Authorization",token)).andExpect(status().isForbidden());
    }
    @Test void masterCannotUpdateAnotherMastersBooking() throws Exception {
        UUID id=UUID.fromString("10000000-0000-0000-0000-000000000002");
        UUID other=db.queryForObject("select b.id from booking b join time_slot s on s.id=b.slot_id where s.master_id<>? limit 1",UUID.class,id);
        http.perform(patch("/api/bookings/"+other+"/status").header("Authorization",bearer(id))
              .contentType("application/json").content("{\"status\":\"CONFIRMED\",\"version\":0}"))
              .andExpect(status().isNotFound());
    }
}
