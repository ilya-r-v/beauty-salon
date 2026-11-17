import React from "react";
export const statuses = {
  PENDING: "Ожидает",
  CONFIRMED: "Подтверждена",
  COMPLETED: "Завершена",
  CANCELLED: "Отменена",
};
export const roles = {
  ADMIN: "Администратор",
  MASTER: "Мастер",
  CLIENT: "Клиент",
};
export const date = (v) =>
  new Date(v).toLocaleString("ru-RU", {
    dateStyle: "short",
    timeStyle: "short",
  });
export function Field({ label, children }) {
  return (
    <label>
      {label}
      {children}
    </label>
  );
}
export function Pager({ page, total, size, change }) {
  return (
    <footer>
      <button disabled={!page} onClick={() => change(page - 1)}>
        Назад
      </button>
      <span>
        Страница {page + 1} из {Math.max(1, Math.ceil(total / size))}
      </span>
      <button
        disabled={(page + 1) * size >= total}
        onClick={() => change(page + 1)}
      >
        Далее
      </button>
    </footer>
  );
}
export function Options({ values }) {
  return Object.entries(values).map(([k, v]) => (
    <option key={k} value={k}>
      {v}
    </option>
  ));
}
export function tags(text) {
  return [
    ...new Set(
      text
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean),
    ),
  ];
}
export function useRows(loader, keys) {
  const [value, setValue] = React.useState(null),
    [error, setError] = React.useState(""),
    [revision, setRevision] = React.useState(0);
  React.useEffect(() => {
    let live = true;
    setError("");
    setValue(null);
    loader()
      .then((v) => {
        if (live) setValue(v);
      })
      .catch((e) => {
        if (live) setError(e.message);
      });
    return () => {
      live = false;
    };
  }, [...keys, revision]);
  return [value, () => setRevision((n) => n + 1), error];
}
