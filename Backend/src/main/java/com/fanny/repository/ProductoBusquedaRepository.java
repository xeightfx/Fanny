package com.fanny.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fanny.dto.ProductoFiltro;
import com.fanny.model.EstadoProducto;
import com.fanny.model.Producto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

/**
 * Busqueda dinamica del catalogo: solo agrega a la consulta los filtros que el cliente envio.
 */
@Repository
public class ProductoBusquedaRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Producto> buscar(ProductoFiltro f) {
        StringBuilder jpql = new StringBuilder("select p from Producto p where p.estado = :activo");
        Map<String, Object> params = new HashMap<>();
        params.put("activo", EstadoProducto.ACTIVO);

        if (hayTexto(f.q())) {
            jpql.append(" and (lower(p.nombre) like :q or lower(p.marca) like :q)");
            params.put("q", "%" + f.q().trim().toLowerCase() + "%");
        }
        if (f.categoriaId() != null) {
            jpql.append(" and p.categoria.id = :categoriaId");
            params.put("categoriaId", f.categoriaId());
        }
        if (hayTexto(f.marca())) {
            jpql.append(" and lower(p.marca) = :marca");
            params.put("marca", f.marca().trim().toLowerCase());
        }
        if (f.precioMin() != null) {
            jpql.append(" and p.precio >= :precioMin");
            params.put("precioMin", f.precioMin());
        }
        if (f.precioMax() != null) {
            jpql.append(" and p.precio <= :precioMax");
            params.put("precioMax", f.precioMax());
        }
        if (hayTexto(f.talla())) {
            jpql.append(" and lower(p.talla) = :talla");
            params.put("talla", f.talla().trim().toLowerCase());
        }
        if (hayTexto(f.color())) {
            jpql.append(" and lower(p.color) = :color");
            params.put("color", f.color().trim().toLowerCase());
        }
        if (Boolean.TRUE.equals(f.soloDisponibles())) {
            jpql.append(" and p.stock > 0");
        }
        if (Boolean.TRUE.equals(f.soloOfertas())) {
            jpql.append(" and p.descuento > 0");
        }
        jpql.append(ordenar(f.orden()));

        TypedQuery<Producto> query = em.createQuery(jpql.toString(), Producto.class);
        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
        }
        return query.getResultList();
    }

    private boolean hayTexto(String s) {
        return s != null && !s.isBlank();
    }

    private String ordenar(String orden) {
        String o = orden == null ? "" : orden;
        return switch (o) {
            case "precio_asc" -> " order by p.precio asc";
            case "precio_desc" -> " order by p.precio desc";
            case "nombre" -> " order by p.nombre asc";
            case "descuento" -> " order by p.descuento desc";
            default -> " order by p.fechaCreacion desc";
        };
    }
}
