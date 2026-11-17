let session = null;
let pendingRefresh;
export function setSession(value) {
  session = value;
}
export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}
export async function api(
  path,
  { method = "GET", body, csv = false } = {},
  retry = true,
) {
  const previous = session;
  const headers = session ? { Authorization: "Bearer " + session.token } : {};
  const multipart = body instanceof FormData;
  if (body !== undefined && !multipart)
    headers["Content-Type"] = "application/json";
  const res = await fetch("/api" + path, {
    method,
    headers,
    body: multipart ? body : JSON.stringify(body),
  });
  if (res.status === 401 && retry && session && !path.startsWith("/auth/")) {
    if (session === previous) {
      pendingRefresh ??= api(
        "/auth/refresh",
        { method: "POST", body: { refreshToken: session.refreshToken } },
        false,
      )
        .then((value) => {
          if (session === previous) setSession(value);
        })
        .catch((error) => {
          setSession(null);
          window.dispatchEvent(new Event("session-ended"));
          throw error;
        })
        .finally(() => {
          pendingRefresh = null;
        });
      await pendingRefresh;
    }
    return api(path, { method, body, csv }, false);
  }
  const text = await res.text();
  if (!res.ok) {
    let message;
    try {
      message = JSON.parse(text).message;
    } catch {}
    throw new ApiError(
      res.status,
      message || "Ошибка запроса (" + res.status + ")",
    );
  }
  return csv ? text : text ? JSON.parse(text) : null;
}
export async function logout() {
  try {
    if (session)
      await api("/auth/logout", {
        method: "POST",
        body: { refreshToken: session.refreshToken },
      });
  } finally {
    setSession(null);
    window.dispatchEvent(new Event("session-ended"));
  }
}
