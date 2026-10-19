package ru.mirea.beautysalon.fx;

import ru.mirea.beautysalon.model.Client;

// Хранит пользователя и JWT-токен, который сейчас вошёл в приложение
public final class Session {

    private static Client currentUser;
    private static String currentRole;
    private static String currentToken;

    private Session() {}

    public static void login(Client user, String roleName) {
        login(user, roleName, null);
    }

    public static void login(Client user, String roleName, String token) {
        currentUser = user;
        currentRole = roleName;
        currentToken = token;
    }

    public static void logout() {
        currentUser = null;
        currentRole = null;
        currentToken = null;
    }

    public static Client currentUser() { return currentUser; }
    public static String currentRole() { return currentRole; }
    public static String currentToken() { return currentToken; }
    public static void setToken(String token) { currentToken = token; }
    public static boolean isLoggedIn() { return currentUser != null; }
}
