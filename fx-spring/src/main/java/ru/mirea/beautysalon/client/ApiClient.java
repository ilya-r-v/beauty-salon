package ru.mirea.beautysalon.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.mirea.beautysalon.client.dto.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class ApiClient {

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper json;

    private String token;

    public ApiClient() {
        this.baseUrl = loadBaseUrl();
        this.http = HttpClient.newHttpClient();
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static String loadBaseUrl() {
        try (InputStream in = ApiClient.class.getResourceAsStream("/api.properties")) {
            if (in == null) throw new IllegalStateException("Файл api.properties не найден в resources");
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("api.base-url");
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить api.properties: " + e.getMessage(), e);
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean hasToken() {
        return token != null && !token.isBlank();
    }

    public AuthResponse register(String name, String phone, String password) {
        String body = json("""
                {"name":"%s","phone":"%s","password":"%s","passwordConfirm":"%s"}
                """.formatted(name, phone, password, password).trim());
        return post("/api/auth/register", body, AuthResponse.class);
    }

    public AuthResponse login(String phone, String password) {
        String body = json("""
                {"phone":"%s","password":"%s"}
                """.formatted(phone, password).trim());
        return post("/api/auth/login", body, AuthResponse.class);
    }

    public List<ClientResponse> getClients() {
        return getList("/api/clients", new TypeReference<>() {});
    }

    public List<ClientResponse> searchClients(String search, String searchBy) {
        return getList("/api/clients?search=%s&searchBy=%s".formatted(encode(search), encode(searchBy)),
                new TypeReference<>() {});
    }

    public ClientResponse createClient(String name, String phone, String password) {
        String body = json("""
                {"name":"%s","phone":"%s","password":"%s"}
                """.formatted(name, phone, password).trim());
        return post("/api/clients", body, ClientResponse.class);
    }

    public ClientResponse updateClient(UUID id, String name, String phone) {
        String body = json("""
                {"name":"%s","phone":"%s"}
                """.formatted(name, phone).trim());
        return put("/api/clients/" + id, body, ClientResponse.class);
    }

    public void deleteClient(UUID id) {
        delete("/api/clients/" + id);
    }

    public void promoteToMaster(UUID clientId, String description) {
        String desc = description == null ? "" : description.replace("\"", "\\\"");
        String body = json("""
                {"description":"%s"}
                """.formatted(desc).trim());
        post("/api/clients/" + clientId + "/promote", body, Object.class);
    }

    public List<MasterResponse> getMasters() {
        return getList("/api/masters", new TypeReference<>() {});
    }

    public List<TimeSlotResponse> getFreeSlots() {
        return getList("/api/time-slots", new TypeReference<>() {});
    }

    public List<TimeSlotResponse> getSlotsByMaster(UUID masterId) {
        return getList("/api/time-slots?masterId=" + masterId, new TypeReference<>() {});
    }

    public List<BookingResponse> getBookings() {
        return getList("/api/bookings", new TypeReference<>() {});
    }

    public List<BookingResponse> getBookings(String search, String searchBy, String status,
                                             UUID masterId, LocalDateTime dateFrom, LocalDateTime dateTo,
                                             String sortBy) {
        StringBuilder url = new StringBuilder("/api/bookings?");
        if (search != null && !search.isBlank())
            url.append("search=").append(encode(search)).append("&searchBy=").append(encode(searchBy)).append("&");
        if (status != null && !status.isBlank())
            url.append("status=").append(status).append("&");
        if (masterId != null)
            url.append("masterId=").append(masterId).append("&");
        if (dateFrom != null)
            url.append("dateFrom=").append(encode(dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))).append("&");
        if (dateTo != null)
            url.append("dateTo=").append(encode(dateTo.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))).append("&");
        if (sortBy != null)
            url.append("sortBy=").append(encode(sortBy));
        return getList(url.toString(), new TypeReference<>() {});
    }

    public BookingResponse createBooking(UUID clientId, UUID slotId, UUID serviceVariantId) {
        String body = json("""
                {"clientId":"%s","slotId":"%s","serviceVariantId":"%s"}
                """.formatted(clientId, slotId, serviceVariantId).trim());
        return post("/api/bookings", body, BookingResponse.class);
    }

    public BookingResponse updateBookingStatus(UUID bookingId, String status) {
        String body = json("""
                {"status":"%s"}
                """.formatted(status).trim());
        return patch("/api/bookings/" + bookingId + "/status", body, BookingResponse.class);
    }

    public void deleteBooking(UUID id) {
        delete("/api/bookings/" + id);
    }

    public List<ServiceTypeResponse> getServiceTypes() {
        return getList("/api/service-types", new TypeReference<>() {});
    }

    public ServiceTypeResponse createServiceType(String name) {
        String body = json("""
                {"name":"%s"}
                """.formatted(name).trim());
        return post("/api/service-types", body, ServiceTypeResponse.class);
    }

    public List<ServiceResponse> getServices() {
        return getList("/api/services", new TypeReference<>() {});
    }

    public List<ServiceResponse> getServices(UUID masterId, UUID typeId) {
        StringBuilder url = new StringBuilder("/api/services?");
        if (masterId != null) url.append("masterId=").append(masterId).append("&");
        if (typeId != null) url.append("typeId=").append(typeId);
        return getList(url.toString(), new TypeReference<>() {});
    }

    public ServiceResponse createService(String title, UUID masterId, UUID serviceTypeId) {
        String body = json("""
                {"title":"%s","masterId":"%s","serviceTypeId":"%s"}
                """.formatted(title, masterId, serviceTypeId).trim());
        return post("/api/services", body, ServiceResponse.class);
    }

    public void deleteService(UUID id) {
        delete("/api/services/" + id);
    }

    public List<VariantResponse> getVariants() {
        return getList("/api/service-variants", new TypeReference<>() {});
    }

    public List<VariantResponse> getVariants(UUID serviceId, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder url = new StringBuilder("/api/service-variants?");
        if (serviceId != null) url.append("serviceId=").append(serviceId).append("&");
        if (minPrice != null) url.append("minPrice=").append(minPrice).append("&");
        if (maxPrice != null) url.append("maxPrice=").append(maxPrice);
        return getList(url.toString(), new TypeReference<>() {});
    }

    public VariantResponse createVariant(String title, String description, BigDecimal price, UUID serviceId) {
        String desc = description == null ? "" : description.replace("\"", "\\\"");
        String body = json("""
                {"title":"%s","description":"%s","price":%s,"serviceId":"%s"}
                """.formatted(title, desc, price.toPlainString(), serviceId).trim());
        return post("/api/service-variants", body, VariantResponse.class);
    }

    public void deleteVariant(UUID id) {
        delete("/api/service-variants/" + id);
    }

    public StatisticsResponse getStatistics() {
        return get("/api/statistics", StatisticsResponse.class);
    }

    private <T> T get(String path, Class<T> type) {
        HttpRequest req = requestBuilder(path).GET().build();
        return send(req, type);
    }

    private <T> List<T> getList(String path, TypeReference<List<T>> type) {
        HttpRequest req = requestBuilder(path).GET().build();
        return sendList(req, type);
    }

    private <T> T post(String path, String body, Class<T> type) {
        HttpRequest req = requestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return send(req, type);
    }

    private <T> T put(String path, String body, Class<T> type) {
        HttpRequest req = requestBuilder(path)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return send(req, type);
    }

    private <T> T patch(String path, String body, Class<T> type) {
        HttpRequest req = requestBuilder(path)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        return send(req, type);
    }

    private void delete(String path) {
        HttpRequest req = requestBuilder(path).DELETE().build();
        send(req, Void.class);
    }

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private <T> T send(HttpRequest req, Class<T> type) {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            checkStatus(resp);
            if (type == Void.class || resp.body().isBlank()) return null;
            return json.readValue(resp.body(), type);
        } catch (ApiException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            throw new ApiException(0, "Ошибка соединения с сервером: " + e.getMessage());
        }
    }

    private <T> List<T> sendList(HttpRequest req, TypeReference<List<T>> type) {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            checkStatus(resp);
            if (resp.body().isBlank()) return List.of();
            return json.readValue(resp.body(), type);
        } catch (ApiException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            throw new ApiException(0, "Ошибка соединения с сервером: " + e.getMessage());
        }
    }

    private void checkStatus(HttpResponse<String> resp) {
        int code = resp.statusCode();
        if (code >= 200 && code < 300) return;

        String message = extractMessage(resp.body(), code);
        throw new ApiException(code, message);
    }

    private String extractMessage(String body, int code) {
        if (body != null && !body.isBlank()) {
            try {
                var node = json.readTree(body);
                if (node.has("message")) return node.get("message").asText();
                if (node.has("error")) return node.get("error").asText();
            } catch (Exception ignored) {}
            if (body.length() < 200) return body;
        }
        return switch (code) {
            case 400 -> "Некорректный запрос";
            case 401 -> "Необходима авторизация";
            case 403 -> "Доступ запрещён";
            case 404 -> "Запись не найдена";
            case 409 -> "Конфликт: данные уже существуют или заняты";
            case 422 -> "Ошибка валидации";
            default -> "Ошибка сервера (HTTP " + code + ")";
        };
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String json(String s) {
        return s.strip();
    }
}
