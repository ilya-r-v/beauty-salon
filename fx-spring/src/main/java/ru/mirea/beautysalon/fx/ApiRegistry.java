package ru.mirea.beautysalon.fx;

import ru.mirea.beautysalon.client.ApiClient;

// Единственный экземпляр ApiClient для всего приложения.
// Контроллеры обращаются через ApiRegistry.api().метод(...)
// Аналог ServiceRegistry из beauty-salon-fx, но вместо репозиториев — REST-клиент.
public final class ApiRegistry {

    private static final ApiClient API = new ApiClient();

    private ApiRegistry() {}

    public static ApiClient api() {
        return API;
    }
}
