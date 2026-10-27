package ru.mirea.beautysalon.fx;

import ru.mirea.beautysalon.client.dto.AuthResponse;

public final class AppState {

    private static final AppState INSTANCE = new AppState();

    private String token;
    private AuthResponse profile;

    private AppState() {}

    public static AppState current() {
        return INSTANCE;
    }

    public void login(AuthResponse response) {
        this.profile = response;
        this.token = response.token();
        ApiRegistry.api().setToken(token);
    }

    public void logout() {
        this.profile = null;
        this.token = null;
        ApiRegistry.api().setToken(null);
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public AuthResponse getProfile() {
        return profile;
    }

    public String getToken() {
        return token;
    }
}
