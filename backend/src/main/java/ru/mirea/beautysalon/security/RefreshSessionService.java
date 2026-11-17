package ru.mirea.beautysalon.security;

import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.security.JwtTokenProvider;
import ru.mirea.beautysalon.dto.auth.AuthResponse;

@Service
@RequiredArgsConstructor
public class RefreshSessionService {
    private final JdbcTemplate jdbc;
    private final ClientRepository clients;
    private final JwtTokenProvider jwt;
    private static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public String issue(UUID id) {
        byte[] bytes = new byte[48]; new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        jdbc.update("INSERT INTO refresh_session(token_hash,client_id,expires_at) VALUES (?,?,now()+interval '14 days')", hash(token), id);
        return token;
    }
    @Transactional
    public AuthResponse refresh(String token) {
        var ids = jdbc.query("DELETE FROM refresh_session WHERE token_hash=? AND expires_at>now() RETURNING client_id", (rs,n) -> rs.getObject(1, UUID.class), hash(token));
        if (ids.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var client = clients.findById(ids.getFirst()).filter(c -> c.isActive()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return AuthResponse.builder().id(client.getId()).name(client.getName()).phone(client.getPhone()).role(client.getRole().name()).token(jwt.generateToken(client)).refreshToken(issue(client.getId())).passwordChangeRequired(client.isPasswordChangeRequired()).build();
    }
    @Transactional public void logout(String token) { jdbc.update("DELETE FROM refresh_session WHERE token_hash=?", hash(token)); }
}