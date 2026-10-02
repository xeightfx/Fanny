let categoriaActiva = "Todas";
let textoBusqueda = "";
let orden = "";
let productos = [];
let categorias = [];

function dinero(n) {
  return "S/ " + n.toFixed(2);
}

/* ---------- Carrito (en el backend, requiere login de cliente) ---------- */
async function agregarAlCarrito(id) {
  if (CONFIG.USAR_DATOS_DEMO) return;
  if (!esCliente()) { location.href = "login.html"; return; }
  try {
    const c = await apiFetch("/cliente/carrito/items", { method: "POST", body: JSON.stringify({ productoId: id, cantidad: 1 }) });
    document.getElementById("contadorCarrito").textContent = c.totalItems;
  } catch (err) { alert(err.message); }
}

/* ---------- Categorías ---------- */
function dibujarCategorias() {
  const lista = ["Todas", ...categorias];
  document.getElementById("categorias").innerHTML = lista
    .map(c => {
      const clase = c === categoriaActiva ? "btn-marca" : "btn-outline-dark";
      return `<button class="btn btn-sm rounded-pill text-nowrap ${clase}" data-cat="${c}">${c}</button>`;
    })
    .join("");
}

/* ---------- Productos ---------- */
function crearTarjeta(p) {
  const descuento = p.precioAnterior
    ? Math.round((1 - p.precio / p.precioAnterior) * 100)
    : 0;
  const insignia = descuento > 0
    ? `<span class="badge bg-danger position-absolute top-0 start-0 m-2">-${descuento}%</span>`
    : "";
  const anterior = p.precioAnterior
    ? `<small class="text-muted text-decoration-line-through ms-2">${dinero(p.precioAnterior)}</small>`
    : "";

  return `
    <div class="col-6 col-md-4 col-lg-3">
      <div class="card producto-card h-100 shadow-sm">
        <div class="position-relative">
          ${insignia}
          <img src="${p.imagen}" class="card-img-top" alt="${p.nombre}">
        </div>
        <div class="card-body d-flex flex-column">
          <small class="text-muted">${p.marca}</small>
          <h6 class="mt-1">${p.nombre}</h6>
          <p class="mb-3"><strong class="precio">${dinero(p.precio)}</strong>${anterior}</p>
          <button class="btn btn-marca btn-sm mt-auto" data-agregar="${p.id}">Agregar al carrito</button>
        </div>
      </div>
    </div>`;
}

function dibujarProductos() {
  let lista = productos.filter(p => {
    const coincideCategoria = categoriaActiva === "Todas" || p.categoria === categoriaActiva;
    const texto = (p.nombre + " " + p.marca + " " + p.categoria).toLowerCase();
    return coincideCategoria && texto.includes(textoBusqueda);
  });

  if (orden === "menor") lista.sort((a, b) => a.precio - b.precio);
  if (orden === "mayor") lista.sort((a, b) => b.precio - a.precio);

  document.getElementById("tituloProductos").textContent =
    categoriaActiva === "Todas" ? "Productos destacados" : categoriaActiva;

  document.getElementById("listaProductos").innerHTML = lista.length
    ? lista.map(crearTarjeta).join("")
    : `<p class="text-muted">No se encontraron productos.</p>`;
}

/* ---------- Eventos ---------- */
document.addEventListener("DOMContentLoaded", async () => {
  dibujarNav();
  try {
    [productos, categorias] = await Promise.all([obtenerProductos(), obtenerCategorias()]);
  } catch (err) {
    console.error(err);
    document.getElementById("listaProductos").innerHTML =
      `<div class="alert alert-danger"><b>Error al cargar el catálogo:</b> ${err.message}<br>
       <small>Backend: ${CONFIG.API_URL} · Abre F12 &gt; Consola para más detalle.</small></div>`;
    return;
  }
  dibujarCategorias();
  dibujarProductos();

  document.getElementById("categorias").addEventListener("click", e => {
    if (e.target.dataset.cat) {
      categoriaActiva = e.target.dataset.cat;
      dibujarCategorias();
      dibujarProductos();
    }
  });

  document.getElementById("listaProductos").addEventListener("click", e => {
    if (e.target.dataset.agregar) {
      agregarAlCarrito(Number(e.target.dataset.agregar));
    }
  });

  document.getElementById("formBuscar").addEventListener("submit", e => {
    e.preventDefault();
    textoBusqueda = document.getElementById("buscador").value.trim().toLowerCase();
    dibujarProductos();
  });

  document.getElementById("orden").addEventListener("change", e => {
    orden = e.target.value;
    dibujarProductos();
  });
});