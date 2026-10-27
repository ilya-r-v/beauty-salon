package ru.mirea.beautysalon.fx;

import ru.mirea.beautysalon.client.ApiClient;

public final class ApiRegistry {

    private static final ApiClient API = new ApiClient();

    private ApiRegistry() {}

    public static ApiClient api() {
        return API;
    }
}
