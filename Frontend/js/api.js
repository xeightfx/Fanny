async function obtenerProductos() {
  if (CONFIG.USAR_DATOS_DEMO) {
    return PRODUCTOS;
  }
  const respuesta = await fetch(`${CONFIG.API_URL}/productos`).catch(() => { throw new Error("El navegador no pudo llegar a " + CONFIG.API_URL + " (backend apagado, otra dirección o bloqueo del navegador)"); });
  if (!respuesta.ok) {
    throw new Error("No se pudieron cargar los productos (HTTP " + respuesta.status + ")");
  }
  const lista = await respuesta.json();
  // El backend devuelve "imagenUrl"; el frontend usa "imagen"
  return lista.map(p => ({
    ...p,
    imagen: p.imagenUrl || "https://placehold.co/400x500/e9ecef/333?text=" + encodeURIComponent(p.nombre),
    marca: p.marca || "",
    categoria: p.categoria || ""
  }));
}

async function obtenerCategorias() {
  if (CONFIG.USAR_DATOS_DEMO) {
    return CATEGORIAS;
  }
  const respuesta = await fetch(`${CONFIG.API_URL}/categorias`).catch(() => { throw new Error("El navegador no pudo llegar a " + CONFIG.API_URL); });
  if (!respuesta.ok) {
    throw new Error("No se pudieron cargar las categorías (HTTP " + respuesta.status + ")");
  }
  const lista = await respuesta.json();
  return lista.map(c => c.nombre);   // el backend devuelve objetos; aquí solo se necesita el nombre
}
