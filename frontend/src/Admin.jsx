import React, { useState } from "react";
import { api } from "./api";
import { Field, Options, Pager, date, roles, useRows } from "./common";
function Reference({ run }) {
  const [types, reload, error] = useRows(() => api("/service-types"), []);
  function submit(e, id) {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    run(async () => {
      await api("/service-types" + (id ? "/" + id : ""), {
        method: id ? "PUT" : "POST",
        body: { name: f.get("name"), active: f.has("active") },
      });
      reload();
    });
  }
  return (
    <>
      <h1>Типы услуг</h1>
      <p>{error}</p>
      <form className="panel actions" onSubmit={(e) => submit(e)}>
        <input
          aria-label="Новый тип услуги"
          placeholder="Название нового типа"
          name="name"
          required
          maxLength="100"
        />
        <button>Создать</button>
      </form>
      {types?.map((t) => (
        <form
          className="panel actions"
          key={t.id}
          onSubmit={(e) => submit(e, t.id)}
        >
          <input name="name" defaultValue={t.name} required maxLength="100" />
          <label>
            <input name="active" type="checkbox" defaultChecked={t.active} />{" "}
            Включён
          </label>
          <button>Сохранить</button>
        </form>
      ))}
    </>
  );
}
function Analytics() {
  const [s, , error] = useRows(() => api("/analytics/summary"), []);
  return (
    <>
      <h1>Аналитика</h1>
      {error && <p>{error}</p>}
      {s && (
        <>
          <div className="metrics">
            {Object.entries({
              total: "Всего",
              active: "Активные",
              archived: "В архиве",
              prepaid: "С предоплатой",
              average_price: "Средняя цена, ₽",
            }).map(([key, title]) => (
              <div className="panel" key={key}>
                <small>{title}</small>
                <h2>{s[key]}</h2>
              </div>
            ))}
          </div>
          <div className="grid">
            {Object.entries({
              byType: "По типу услуги",
              byStatus: "По статусу",
              byPriority: "По приоритету",
            }).map(([k, title]) => (
              <section className="panel" key={k}>
                <h2>{title}</h2>
                {s[k].map((row) => (
                  <p key={row.label}>
                    {row.label} · {row.count}
                  </p>
                ))}
              </section>
            ))}
          </div>
        </>
      )}
    </>
  );
}
function Audit() {
  const [page, setPage] = useState(0),
    [search, setSearch] = useState(""),
    [dir, setDir] = useState("desc");
  const [data, , error] = useRows(
    () =>
      api(
        "/admin/audit?" +
          new URLSearchParams({ page, size: 15, search, direction: dir }),
      ),
    [page, search, dir],
  );
  return (
    <>
      <h1>Журнал аудита</h1>
      <div className="actions">
        <input
          placeholder="Событие, пользователь или UUID"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
        />
        <select
          value={dir}
          onChange={(e) => {
            setDir(e.target.value);
            setPage(0);
          }}
        >
          <option value="desc">Сначала новые</option>
          <option value="asc">Сначала старые</option>
        </select>
      </div>
      {error && <p>{error}</p>}
      {data && (
        <>
          <div className="scroll">
            <table>
              <thead>
                <tr>
                  {["Время", "Кто", "Действие", "Объект", "Запрос"].map((t) => (
                    <th key={t}>{t}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {data.content.map((a) => (
                  <tr key={a.id}>
                    <td>{date(a.occurred_at)}</td>
                    <td>{a.actor_name || a.actor || "Система"}</td>
                    <td>{a.description}</td>
                    <td>{a.entity_id}</td>
                    <td>{a.correlation_id}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager
            page={page}
            total={data.totalElements}
            size={15}
            change={setPage}
          />
        </>
      )}
    </>
  );
}
function temporary() {
  return (
    "Aa1!" +
    Array.from(crypto.getRandomValues(new Uint8Array(14)), (n) =>
      n.toString(16).padStart(2, "0"),
    ).join("")
  );
}
function Users({ run }) {
  const [page, setPage] = useState(0),
    [edit, setEdit] = useState(null),
    [pass, setPass] = useState(""),
    [notice, setNotice] = useState("");
  const [data, reload, error] = useRows(
    () => api("/admin/users?page=" + page + "&size=10"),
    [page],
  );
  return (
    <>
      <div className="title">
        <h1>Пользователи</h1>
        <button
          onClick={() => {
            setEdit({ role: "CLIENT", active: true });
            setPass(temporary());
            setNotice("");
          }}
        >
          Новый пользователь
        </button>
      </div>
      {notice && (
        <p role="status">
          {notice} <button onClick={() => setNotice("")}>Скрыть</button>
        </p>
      )}
      {error && <p>{error}</p>}
      {edit && (
        <form
          key={edit.id || "new"}
          className="panel"
          onSubmit={(e) => {
            e.preventDefault();
            const f = new FormData(e.currentTarget),
              user = {
                name: f.get("name"),
                phone: f.get("phone"),
                role: f.get("role"),
                active: f.has("active"),
              };
            run(async () => {
              await api("/admin/users" + (edit.id ? "/" + edit.id : ""), {
                method: edit.id ? "PUT" : "POST",
                body: edit.id ? user : { user, temporaryPassword: pass },
              });
              if (!edit.id) setNotice("Временный пароль: " + pass);
              setEdit(null);
              reload();
            });
          }}
        >
          <div className="grid">
            <Field label="Имя">
              <input
                name="name"
                defaultValue={edit.name}
                maxLength="100"
                required
              />
            </Field>
            <Field label="Телефон">
              <input
                name="phone"
                type="tel"
                defaultValue={edit.phone}
                required
              />
            </Field>
            <Field label="Роль">
              <select name="role" defaultValue={edit.role}>
                <Options values={roles} />
              </select>
            </Field>
            <label>
              <input
                name="active"
                type="checkbox"
                defaultChecked={edit.active}
              />{" "}
              Активен
            </label>
          </div>
          <button>Сохранить</button>{" "}
          <button type="button" onClick={() => setEdit(null)}>
            Отмена
          </button>
        </form>
      )}
      {data && (
        <>
          <div className="scroll">
            <table>
              <thead>
                <tr>
                  <th>Имя</th>
                  <th>Телефон</th>
                  <th>Роль</th>
                  <th>Доступ</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((u) => (
                  <tr key={u.id}>
                    <td>{u.name}</td>
                    <td>{u.phone}</td>
                    <td>{roles[u.role]}</td>
                    <td>{u.active ? "Активен" : "Отключён"}</td>
                    <td>
                      <button onClick={() => setEdit(u)}>Изменить</button>{" "}
                      <button
                        onClick={() => {
                          if (
                            confirm(
                              "Сбросить пароль пользователя " + u.name + "?",
                            )
                          )
                            run(async () => {
                              const value = temporary();
                              await api(
                                "/admin/users/" + u.id + "/reset-password",
                                {
                                  method: "POST",
                                  body: { temporaryPassword: value },
                                },
                              );
                              setNotice(
                                "Временный пароль для " + u.name + ": " + value,
                              );
                            });
                        }}
                      >
                        Сброс пароля
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager
            page={page}
            total={data.totalElements}
            size={10}
            change={setPage}
          />
        </>
      )}
    </>
  );
}
export default function Admin({ tab, run }) {
  switch (tab) {
    case "Справочник":
      return <Reference run={run} />;
    case "Аналитика":
      return <Analytics />;
    case "Аудит":
      return <Audit />;
    default:
      return <Users run={run} />;
  }
}
