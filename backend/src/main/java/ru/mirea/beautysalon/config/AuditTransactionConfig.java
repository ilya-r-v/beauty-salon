package ru.mirea.beautysalon.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.UUID;
import org.hibernate.Session;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.context.request.*;

@Configuration
public class AuditTransactionConfig {

    @Bean
    JpaTransactionManager transactionManager(EntityManagerFactory factory) {
        var manager = new JpaTransactionManager(factory);
        manager.setJpaDialect(
            new HibernateJpaDialect() {
                @Override
                public Object beginTransaction(
                    EntityManager em,
                    TransactionDefinition definition
                ) throws java.sql.SQLException {
                    Object tx = super.beginTransaction(em, definition);
                    if (!definition.isReadOnly()) {
                        var auth = SecurityContextHolder.getContext().getAuthentication();
                        String actor =
                            auth != null && auth.getPrincipal() instanceof UUID id ? id.toString() : "";
                        var attrs = RequestContextHolder.getRequestAttributes();
                        Object correlation =
                            attrs == null
                                ? null
                                : attrs.getAttribute("correlationId", RequestAttributes.SCOPE_REQUEST);
                        em.unwrap(Session.class).doWork(connection -> {
                            try (
                                var stmt = connection.prepareStatement(
                                    "select set_config('salon.actor',?,true),set_config('salon.correlation',?,true)"
                                )
                            ) {
                                stmt.setString(1, actor);
                                stmt.setString(2, correlation == null ? "" : correlation.toString());
                                stmt.execute();
                            }
                        });
                    }
                    return tx;
                }
            }
        );
        return manager;
    }
}
