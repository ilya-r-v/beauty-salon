package ru.mirea.beautysalon.fx;

import ru.mirea.beautysalon.model.Client;

// Хранит пользователя, который сейчас вошёл в приложение
public final class Session {

    private static Client currentUser;
    private static String currentRole;

    private Session() {}

    public static void login(Client user, String roleName) {
        currentUser = user;
        currentRole = roleName;
    }

    public static void logout() {
        currentUser = null;
        currentRole = null;
    }

    public static Client currentUser() { return currentUser; }
    public static String currentRole() { return currentRole; }
    public static boolean isLoggedIn() { return currentUser != null; }
}
