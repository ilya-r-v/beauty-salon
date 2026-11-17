package ru.mirea.beautysalon.security;
import ru.mirea.beautysalon.security.JwtTokenProvider;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.domain.Client;
import ru.mirea.beautysalon.repository.ClientRepository;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final ClientRepository clientRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            UUID userId = tokenProvider.getUserIdFromToken(token);

            Client client = clientRepository.findById(userId).orElse(null);
            if (client != null && client.isActive() && client.getCredentialVersion() == tokenProvider.credentialVersion(token)) {
                var auth = new UsernamePasswordAuthenticationToken(
                        client.getId(),
                        null,
                        List.of(new SimpleGrantedAuthority(client.isPasswordChangeRequired() ? "PASSWORD_CHANGE" : "ROLE_" + client.getRole())));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        String path = request.getRequestURI();
        if (request.getMethod().equals("GET") && (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) && request.getCookies()!=null) {
            for (var cookie : request.getCookies()) if ("salon_docs".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}