import React, { useState } from "react";
import { api } from "./api";
import { date, Field, Options, Pager, statuses, tags, useRows } from "./common";
import BookingCard from "./BookingCard";

function CreateBooking({ close, done, run }) {
  const [choices, , error] = useRows(
    () =>
      Promise.all(
        ["/clients", "/time-slots", "/service-variants", "/services"].map((p) =>
          api(p),
        ),
      ),
    [],
  );
  const [slot, setSlot] = useState("");
  if (!choices) return <p>{error || "Загрузка…"}</p>;
  const [clients, slots, variants, services] = choices;
  const master = slots.find((s) => s.id === slot)?.masterId;
  return (
    <form
      className="panel"
      onSubmit={(e) => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.currentTarget));
        run(async () => {
          await api("/bookings", {
            method: "POST",
            body: {
              ...data,
              prepaid: data.prepaid === "on",
              tags: tags(data.tags),
            },
          });
          done();
        });
      }}
    >
      <h2>Новая запись</h2>
      <div className="grid">
        <Field label="Клиент">
          <select name="clientId" required>
            {clients.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Свободное время">
          <select
            name="slotId"
            required
            value={slot}
            onChange={(e) => setSlot(e.target.value)}
          >
            <option value="">Выберите время</option>
            {slots.map((s) => (
              <option key={s.id} value={s.id}>
                {date(s.startTime)} · {s.masterName}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Вариант услуги">
          <select key={slot} name="serviceVariantId" required>
            {variants
              .filter((v) =>
                services.some(
                  (s) => s.id === v.serviceId && s.masterId === master,
                ),
              )
              .map((v) => (
                <option key={v.id} value={v.id}>
                  {v.title} · {v.price} ₽
                </option>
              ))}
          </select>
        </Field>
        <Field label="Комментарий">
          <input name="comment" maxLength="500" />
        </Field>
        <Field label="Теги через запятую">
          <input name="tags" />
        </Field>
        <label>
          <input type="checkbox" name="prepaid" /> Предоплата
        </label>
      </div>
      <div className="actions">
        <button className="primary">Создать</button>
        <button type="button" onClick={close}>
          Отмена
        </button>
      </div>
    </form>
  );
}
export default function Bookings({ user, run }) {
  const admin = user.role === "ADMIN";
  const [filter, setFilter] = useState({ archived: "false" }),
    [page, setPage] = useState(0),
    [selected, setSelected] = useState([]),
    [card, setCard] = useState(""),
    [create, setCreate] = useState(false);
  const [action, setAction] = useState("ARCHIVE"),
    [argument, setArgument] = useState(""),
    [result, setResult] = useState(null),
    [preview, setPreview] = useState(null);
  const [types] = useRows(() => api("/service-types"), []);
  const query = new URLSearchParams(
    Object.entries(filter).filter(([, v]) => v !== ""),
  ).toString();
  const [rows, reload, error] = useRows(
    () => api("/bookings?" + query + "&size=10&page=" + page),
    [page, query],
  );
  function reset() {
    setSelected([]);
    reload();
  }
  function filterField(label, key, options, type = "text") {
    const props = {
      value: filter[key] ?? "",
      onChange: (e) => {
        setPage(0);
        setSelected([]);
        setFilter({ ...filter, [key]: e.target.value });
      },
    };
    return (
      <Field label={label}>
        {options ? (
          <select {...props}>
            <option value="">Все</option>
            <Options values={options} />
          </select>
        ) : (
          <input {...props} type={type} />
        )}
      </Field>
    );
  }
  async function download() {
    const data = await api("/bookings/export?" + query, { csv: true });
    const url = URL.createObjectURL(new Blob([data], { type: "text/csv" }));
    const a = document.createElement("a");
    a.href = url;
    a.download = "bookings.csv";
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
  return (
    <>
      <div className="title">
        <h1>Записи на приём</h1>
        {admin && (
          <button className="primary" onClick={() => setCreate(true)}>
            Новая запись
          </button>
        )}
      </div>
      {create && (
        <CreateBooking
          run={run}
          close={() => setCreate(false)}
          done={() => {
            setCreate(false);
            reset();
          }}
        />
      )}
      <section className="panel grid">
        {filterField("Поиск", "search")}
        {filterField("Статус", "status", statuses)}
        {filterField(
          "Тип услуги",
          "typeId",
          Object.fromEntries((types || []).map((t) => [t.id, t.name])),
        )}
        {filterField("Архив", "archived", {
          false: "Активные",
          true: "Архивные",
        })}
        {filterField("Предоплата", "prepaid", {
          true: "Внесена",
          false: "Нет",
        })}
        {filterField("Приоритет", "priority", {
          HIGH: "Высокий",
          NORMAL: "Обычный",
        })}
        {filterField("Тег", "tag")}
        {filterField("Цена от", "minPrice", null, "number")}
        {filterField("Цена до", "maxPrice", null, "number")}
        {filterField("Сортировка", "sortBy", {
          time: "Время",
          price: "Цена",
          number: "Номер",
          status: "Статус",
        })}
        {filterField("Порядок", "direction", {
          asc: "По возрастанию",
          desc: "По убыванию",
        })}
      </section>
      <div className="actions">
        <button onClick={reset}>Обновить</button>
        <button onClick={() => run(download)}>Экспорт CSV</button>
      </div>
      {error && <p role="alert">{error}</p>}
      {rows && (
        <>
          <div className="scroll">
            <table>
              <thead>
                <tr>
                  {admin && <th>Выбор</th>}
                  <th>Запись</th>
                  <th>Клиент</th>
                  <th>Время</th>
                  <th>Услуга</th>
                  <th>Статус</th>
                  <th>Цена</th>
                </tr>
              </thead>
              <tbody>
                {rows.content.map((b) => (
                  <tr key={b.id}>
                    {admin && (
                      <td>
                        <input
                          type="checkbox"
                          aria-label={"Выбрать №" + b.bookingNumber}
                          checked={selected.includes(b.id)}
                          onChange={(e) =>
                            setSelected(
                              e.target.checked
                                ? [...selected, b.id]
                                : selected.filter((id) => id !== b.id),
                            )
                          }
                        />
                      </td>
                    )}
                    <td>
                      <button onClick={() => setCard(b.id)}>
                        № {b.bookingNumber}
                      </button>
                    </td>
                    <td>{b.clientName}</td>
                    <td>{date(b.slot.startTime)}</td>
                    <td>{b.serviceVariantTitle}</td>
                    <td>{statuses[b.status]}</td>
                    <td>{b.price} ₽</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!rows.content.length && <p>Записей по этим условиям нет.</p>}
          </div>
          <Pager
            page={page}
            total={rows.totalElements}
            size={10}
            change={(n) => {
              setPage(n);
              setSelected([]);
            }}
          />
        </>
      )}
      {card && (
        <BookingCard
          key={card}
          id={card}
          user={user}
          run={run}
          close={() => setCard("")}
          changed={reset}
        />
      )}
      {admin && (
        <>
          <section className="panel">
            <h2>Выбранные записи · {selected.length}</h2>
            <div className="actions">
              <select
                value={action}
                onChange={(e) => {
                  setAction(e.target.value);
                  setArgument("");
                }}
              >
                <Options
                  values={{
                    ARCHIVE: "Архивировать",
                    RESTORE: "Восстановить",
                    STATUS: "Изменить статус",
                    ADD_TAG: "Добавить тег",
                    REMOVE_TAG: "Снять тег",
                  }}
                />
              </select>
              {action === "STATUS" ? (
                <select
                  value={argument}
                  onChange={(e) => setArgument(e.target.value)}
                >
                  <option value="">Выберите статус</option>
                  <Options values={statuses} />
                </select>
              ) : (
                action.endsWith("TAG") && (
                  <input
                    placeholder="Тег"
                    value={argument}
                    onChange={(e) => setArgument(e.target.value)}
                  />
                )
              )}
              <button
                disabled={!selected.length}
                onClick={() =>
                  run(async () => {
                    setResult(
                      await api("/bookings/bulk", {
                        method: "POST",
                        body: {
                          ids: selected,
                          action,
                          status: action === "STATUS" ? argument : null,
                          tag: argument,
                        },
                      }),
                    );
                    reset();
                  })
                }
              >
                Применить
              </button>
            </div>
            {result && (
              <p>
                Успешно: {result.success.length}. Ошибок:{" "}
                {Object.keys(result.errors).length}.{" "}
                {Object.entries(result.errors).map(([id, e]) => (
                  <span key={id}>
                    {id}: {e}{" "}
                  </span>
                ))}
              </p>
            )}
          </section>
          <form
            className="panel"
            onChange={() => setPreview(null)}
            onSubmit={(e) => {
              e.preventDefault();
              const f = new FormData(e.currentTarget);
              run(async () =>
                setPreview(
                  await api("/bookings/import/preview", {
                    method: "POST",
                    body: f,
                  }),
                ),
              );
            }}
          >
            <h2>Импорт CSV</h2>
            <div className="actions">
              <input name="file" type="file" accept=".csv" required />
              <select name="mode">
                <option value="NEW">Только новые</option>
                <option value="UPSERT">Обновлять по коду</option>
              </select>
              <button>Проверить файл</button>
            </div>
            {preview && (
              <div>
                <p>
                  Корректных строк: {preview.valid}. Ошибок: {preview.invalid}.
                </p>
                {Object.entries(preview.errors).map(([n, e]) => (
                  <p key={n}>
                    Строка {n}: {e}
                  </p>
                ))}
                {preview.previewId && (
                  <button
                    type="button"
                    onClick={() =>
                      run(async () => {
                        const count = await api("/bookings/import/apply", {
                          method: "POST",
                          body: { previewId: preview.previewId },
                        });
                        setPreview(null);
                        reset();
                        setResult({ success: Array(count), errors: {} });
                      })
                    }
                  >
                    Применить импорт
                  </button>
                )}
              </div>
            )}
          </form>
        </>
      )}
    </>
  );
}
