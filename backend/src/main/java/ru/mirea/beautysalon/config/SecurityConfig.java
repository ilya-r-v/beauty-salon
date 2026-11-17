package ru.mirea.beautysalon.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.mirea.beautysalon.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ru.mirea.beautysalon.error.SecurityErrors securityErrors;
    private final JwtAuthenticationFilter jwtAuthFilter;

    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .exceptionHandling(errors ->
                errors
                    .authenticationEntryPoint((req, res, ex) ->
                        securityErrors.write(req, res, 401, "Требуется вход")
                    )
                    .accessDeniedHandler((req, res, ex) ->
                        securityErrors.write(req, res, 403, "Недостаточно прав")
                    )
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico")
                    .permitAll()
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers(SWAGGER_WHITELIST)
                    .hasRole("ADMIN")
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/error"
                    )
                    .permitAll()
                    .requestMatchers("/api/auth/me", "/api/auth/change-password")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/bookings/*/notes")
                    .hasAnyRole("CLIENT", "MASTER", "ADMIN")
                    .requestMatchers("/api/bookings/*/notes", "/api/bookings/*/notes/*")
                    .hasAnyRole("MASTER", "ADMIN")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/bookings",
                        "/api/bookings/*",
                        "/api/services",
                        "/api/services/*",
                        "/api/service-types",
                        "/api/service-variants",
                        "/api/service-variants/*"
                    )
                    .hasAnyRole("CLIENT", "MASTER", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/status")
                    .hasAnyRole("MASTER", "ADMIN")
                    .requestMatchers("/api/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
