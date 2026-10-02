/* Sesión: el backend usa HTTP Basic, así que guardamos el header en sessionStorage */
const Sesion = {
  guardar(email, password, usuario) {
    const auth = "Basic " + btoa(unescape(encodeURIComponent(email + ":" + password)));
    sessionStorage.setItem("sesion", JSON.stringify({ auth, usuario }));
  },
  leer() { try { return JSON.parse(sessionStorage.getItem("sesion")); } catch { return null; } },
  salir() { sessionStorage.removeItem("sesion"); location.href = "index.html"; }
};

/* Llama al backend con el usuario logueado. Devuelve JSON (o null si no hay cuerpo). */
async function apiFetch(ruta, opciones = {}) {
  const s = Sesion.leer();
  const headers = { "Content-Type": "application/json", "X-Requested-With": "XMLHttpRequest" };
  if (s) headers.Authorization = s.auth;
  let r;
  try {
    r = await fetch(CONFIG.API_URL + ruta, { ...opciones, headers });
  } catch {
    throw new Error("No se pudo conectar con el backend (" + CONFIG.API_URL + ")");
  }
  if (r.status === 401) throw new Error("Email o contraseña incorrectos, o sesión vencida");
  if (r.status === 403) throw new Error("Tu usuario no tiene permiso para esta acción");
  const texto = await r.text();
  const datos = texto ? JSON.parse(texto) : null;
  if (!r.ok) {
    const detalle = datos && datos.errores ? " " + Object.values(datos.errores).join(", ") : "";
    throw new Error((datos && datos.mensaje ? datos.mensaje : "Error " + r.status) + detalle);
  }
  return datos;
}

async function login(email, password) {
  Sesion.guardar(email, password, null);
  try {
    const usuario = await apiFetch("/auth/me");
    Sesion.guardar(email, password, usuario);
    return usuario;
  } catch (e) { sessionStorage.removeItem("sesion"); throw e; }
}

function esCliente() {
  const s = Sesion.leer();
  return !!(s && s.usuario && s.usuario.roles.some(r => r.replace("ROLE_", "") === "CLIENTE"));
}

/* Menú superior: Ingresar / Hola, nombre + Pedidos + Salir, y contador del carrito */
async function dibujarNav() {
  const s = Sesion.leer();
  const nav = document.getElementById("navUsuario");
  if (nav) {
    nav.innerHTML = s
      ? `<span class="nav-link d-inline">Hola, ${s.usuario.nombres}</span>
         <a class="nav-link d-inline" href="pedidos.html">Mis pedidos</a>
         <a class="nav-link d-inline" href="#" id="btnSalir">Salir</a>`
      : `<a class="nav-link" href="login.html">Ingresar</a>`;
    const b = document.getElementById("btnSalir");
    if (b) b.onclick = e => { e.preventDefault(); Sesion.salir(); };
  }
  const c = document.getElementById("contadorCarrito");
  if (c && esCliente()) {
    try { c.textContent = (await apiFetch("/cliente/carrito")).totalItems; } catch {}
  }
}
