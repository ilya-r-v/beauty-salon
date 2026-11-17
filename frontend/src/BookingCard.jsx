import React from "react";
import { api } from "./api";
import { date, Field, Options, statuses, tags, useRows } from "./common";
export default function BookingCard({ id, user, run, close, changed }) {
  const path = "/bookings/" + id,
    admin = user.role === "ADMIN",
    editor = user.role !== "CLIENT";
  const [data, reload, error] = useRows(
    () => Promise.all([api(path), api(path + "/notes")]),
    [id],
  );
  const refresh = () => {
    reload();
    changed();
  };
  async function mutate(suffix, method, body) {
    await api(path + suffix, { method, body });
    refresh();
  }
  if (!data)
    return (
      <p>
        {error || "Загрузка карточки…"} <button onClick={close}>Закрыть</button>
      </p>
    );
  const [b, notes] = data;
  const next =
    {
      PENDING: ["CONFIRMED", "CANCELLED"],
      CONFIRMED: ["COMPLETED", "CANCELLED"],
    }[b.status] || [];
  return (
    <article className="panel">
      <div className="title">
        <h2>Запись № {b.bookingNumber}</h2>
        <button onClick={close}>Закрыть</button>
      </div>
      <p>
        {b.clientName} · {b.serviceVariantTitle} · {b.slot.masterName} ·{" "}
        {date(b.slot.startTime)}
      </p>
      <p>
        {statuses[b.status]} · {b.price} ₽ · Приоритет{" "}
        {b.priority === "HIGH" ? "высокий" : "обычный"} ·{" "}
        {b.archived ? "В архиве" : "Активна"}
      </p>
      <small>
        Код {b.externalCode} · Создана {date(b.createdAt)} · Изменена{" "}
        {date(b.updatedAt)} · Версия {b.version}
      </small>
      <form
        key={b.version}
        onSubmit={(e) => {
          e.preventDefault();
          const f = new FormData(e.currentTarget);
          run(() =>
            mutate("", "PUT", {
              version: b.version,
              comment: f.get("comment"),
              prepaid: f.has("prepaid"),
              tags: tags(f.get("tags")),
            }),
          );
        }}
      >
        <div className="grid">
          <Field label="Комментарий">
            <input
              name="comment"
              disabled={!admin}
              defaultValue={b.comment || ""}
              maxLength="500"
            />
          </Field>
          <Field label="Теги через запятую">
            <input
              name="tags"
              disabled={!admin}
              defaultValue={b.tags.join(", ")}
            />
          </Field>
          <label>
            <input
              name="prepaid"
              type="checkbox"
              disabled={!admin}
              defaultChecked={b.prepaid}
            />{" "}
            Предоплата
          </label>
        </div>
        {admin && <button>Сохранить карточку</button>}
      </form>
      <div className="actions">
        {editor &&
          next.map((s) => (
            <button
              key={s}
              onClick={() =>
                run(() =>
                  mutate("/status", "PATCH", { status: s, version: b.version }),
                )
              }
            >
              {statuses[s]}
            </button>
          ))}
        {admin && (
          <button
            onClick={() =>
              run(() =>
                mutate(
                  b.archived ? "/restore" : "",
                  b.archived ? "POST" : "DELETE",
                ),
              )
            }
          >
            {b.archived ? "Восстановить" : "В архив"}
          </button>
        )}
        {admin && (
          <button
            onClick={() => {
              if (confirm("Удалить запись окончательно?"))
                run(async () => {
                  await api(path + "/permanent", { method: "DELETE" });
                  close();
                  changed();
                });
            }}
          >
            Удалить окончательно
          </button>
        )}
        <button onClick={refresh}>Перечитать</button>
      </div>
      <h3>Заметки</h3>
      {notes.map((n) => (
        <form
          key={n.id + n.updated_at}
          onSubmit={(e) => {
            e.preventDefault();
            const f = new FormData(e.currentTarget);
            run(() =>
              mutate("/notes/" + n.id, "PUT", {
                body: f.get("body"),
                pinned: f.has("pinned"),
              }),
            );
          }}
        >
          <small>
            {n.author} · {date(n.created_at)}
          </small>
          <div className="actions">
            <input
              disabled={!editor}
              name="body"
              required
              maxLength="2000"
              defaultValue={n.body}
            />
            <label>
              <input
                type="checkbox"
                name="pinned"
                disabled={!editor}
                defaultChecked={n.pinned}
              />{" "}
              Закреплена
            </label>
            {editor && (
              <>
                <button>Сохранить</button>
                <button
                  type="button"
                  onClick={() => run(() => mutate("/notes/" + n.id, "DELETE"))}
                >
                  Удалить
                </button>
              </>
            )}
          </div>
        </form>
      ))}
      {editor && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const f = new FormData(e.currentTarget);
            run(() =>
              mutate("/notes", "POST", {
                body: f.get("body"),
                pinned: f.has("pinned"),
              }),
            );
          }}
        >
          <div className="actions">
            <input
              name="body"
              placeholder="Новая заметка"
              required
              maxLength="2000"
            />
            <label>
              <input name="pinned" type="checkbox" /> Закрепить
            </label>
            <button>Добавить</button>
          </div>
        </form>
      )}
    </article>
  );
}
