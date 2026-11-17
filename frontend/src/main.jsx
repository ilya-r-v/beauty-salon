import React, { useState, useEffect } from "react";
import { createRoot } from "react-dom/client";
import { api, setSession, logout } from "./api";
import { Field, roles } from "./common";
import Bookings from "./Bookings";
import Admin from "./Admin";
import "./style.css";

function App() {
  const [user, setUser] = useState(null),
    [tab, setTab] = useState("Записи"),
    [message, setMessage] = useState(""),
    [password, setPassword] = useState(false);
  useEffect(() => {
    const clear = () => {
      setUser(null);
      setPassword(false);
      setTab("Записи");
    };
    window.addEventListener("session-ended", clear);
    return () => window.removeEventListener("session-ended", clear);
  }, []);
  async function run(action) {
    setMessage("");
    try {
      await action();
    } catch (e) {
      setMessage(
        e.status === 409
          ? "Конфликт изменений. Обновите данные. " + e.message
          : e.message,
      );
    }
  }
  async function submit(event) {
    event.preventDefault();
    const fields = Object.fromEntries(new FormData(event.currentTarget));
    await run(async () => {
      if (user) {
        await api("/auth/change-password", { method: "POST", body: fields });
        await logout();
        setMessage("Пароль изменён. Войдите снова.");
      } else {
        const result = await api("/auth/login", {
          method: "POST",
          body: fields,
        });
        setSession(result);
        setUser(result);
        setPassword(result.passwordChangeRequired);
      }
    });
  }
  return (
    <>
      <header>
        <div className="brand">Салон красоты</div>
        {user && (
          <div>
            {user.name} · {roles[user.role]}{" "}
            <button onClick={() => run(logout)}>Выйти</button>
          </div>
        )}
      </header>
      <main>
        {message && (
          <p role="alert">
            {message} <button onClick={() => setMessage("")}>Закрыть</button>
          </p>
        )}
        {!user || password ? (
          <form key={user ? "change-password" : "login"} className="login" onSubmit={submit}>
            <h1>{user ? "Смена пароля" : "Вход в салон"}</h1>
            {user ? (
              <>
                <Field label={user.passwordChangeRequired ? "Временный пароль, с которым вы вошли" : "Текущий пароль"}>
                  <input name="currentPassword" type="password" autoComplete="current-password" required />
                </Field>
                <Field label="Новый пароль">
                  <input
                    name="newPassword"
                    type="password"
                    autoComplete="new-password"
                    required
                  />
                </Field>
              </>
            ) : (
              <>
                <Field label="Телефон">
                  <input
                    name="phone"
                    type="tel"
                    autoComplete="username"
                    required
                  />
                </Field>
                <Field label="Пароль">
                  <input
                    name="password"
                    type="password"
                    autoComplete="current-password"
                    required
                  />
                </Field>
              </>
            )}
            <button className="primary">
              {user ? "Сохранить пароль" : "Войти"}
            </button>
            {user && !user.passwordChangeRequired && (
              <button type="button" onClick={() => setPassword(false)}>
                Назад
              </button>
            )}
          </form>
        ) : (
          <>
            <nav>
              {(user.role === "ADMIN"
                ? ["Записи", "Справочник", "Аналитика", "Пользователи", "Аудит"]
                : ["Записи"]
              ).map((t) => (
                <button
                  className={t === tab ? "current" : ""}
                  key={t}
                  onClick={() => setTab(t)}
                >
                  {t}
                </button>
              ))}
              <button onClick={() => setPassword(true)}>Пароль</button>
              {user.role === "ADMIN" && (
                <button
                  onClick={() =>
                    run(async () => {
                      await api("/admin/docs", { method: "POST" });
                      location.href = "/swagger-ui.html";
                    })
                  }
                >
                  Swagger
                </button>
              )}
            </nav>
            {tab === "Записи" ? (
              <Bookings user={user} run={run} />
            ) : (
              <Admin key={tab} tab={tab} run={run} />
            )}
          </>
        )}
      </main>
    </>
  );
}
createRoot(document.getElementById("root")).render(<App />);
