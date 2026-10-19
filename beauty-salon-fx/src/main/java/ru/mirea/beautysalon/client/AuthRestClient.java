package ru.mirea.beautysalon.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.mirea.beautysalon.client.dto.AuthResponseDto;
import ru.mirea.beautysalon.client.dto.UserResponseDto;
import ru.mirea.beautysalon.exception.BusinessException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * REST HTTP-клиент для взаимодействия со Spring Boot бэкендом авторизации.
 */
public class AuthRestClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthRestClient() {
        this("http://localhost:8080/api");
    }

    public AuthRestClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public AuthResponseDto login(String phone, String password) {
        String url = baseUrl + "/auth/login";
        Map<String, String> payload = Map.of(
                "phone", phone != null ? phone.trim() : "",
                "password", password != null ? password : ""
        );
        return sendAuthRequest(url, payload);
    }

    public AuthResponseDto register(String name, String phone, String password, String passwordConfirm) {
        String url = baseUrl + "/auth/register";
        Map<String, String> payload = Map.of(
                "name", name != null ? name.trim() : "",
                "phone", phone != null ? phone.trim() : "",
                "password", password != null ? password : "",
                "passwordConfirm", passwordConfirm != null ? passwordConfirm : ""
        );
        return sendAuthRequest(url, payload);
    }

    public UserResponseDto getProfile(String token) {
        String url = baseUrl + "/auth/me";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), UserResponseDto.class);
            }
            throw new BusinessException(extractErrorMessage(response.body(), "Ошибка получения профиля"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Прерван запрос к серверу: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Сервер авторизации недоступен (" + baseUrl + "): " + e.getMessage(), e);
        }
    }

    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private AuthResponseDto sendAuthRequest(String url, Map<String, String> payload) {
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();

            if (code >= 200 && code < 300) {
                return objectMapper.readValue(response.body(), AuthResponseDto.class);
            }

            String errorMessage = extractErrorMessage(response.body(), "Ошибка аутентификации (HTTP " + code + ")");
            throw new BusinessException(errorMessage);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Прервано обращение к серверу авторизации: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Сервер авторизации недоступен (" + baseUrl + "): " + e.getMessage(), e);
        }
    }

    private String extractErrorMessage(String responseBody, String defaultMsg) {
        if (responseBody == null || responseBody.isBlank()) {
            return defaultMsg;
        }
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.has("error") && !node.get("error").isNull()) {
                return node.get("error").asText();
            }
            if (node.has("message") && !node.get("message").isNull()) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {}
        return defaultMsg;
    }
}
