package ru.mirea.beautysalon.security;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.beautysalon.security.PasswordPolicy;

@Component
public class BootstrapAccounts implements ApplicationRunner {

    private final JdbcTemplate db;
    private final PasswordEncoder encoder;
    private final String adminPassword;

    public BootstrapAccounts(
        JdbcTemplate db,
        PasswordEncoder encoder,
        @Value("${BOOTSTRAP_ADMIN_PASSWORD}") String adminPassword
    ) {
        this.db = db;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (var row : db.queryForList(
            "select id,role from client where password_hash='BOOTSTRAP_REQUIRED' for update"
        )) {
            String temporary = "ADMIN".equals(row.get("role")) ? adminPassword : UUID.randomUUID() + "Aa1";
            PasswordPolicy.validate(temporary);
            db.update(
                "update client set password_hash=? where id=?",
                encoder.encode(temporary),
                row.get("id")
            );
        }
    }
}
