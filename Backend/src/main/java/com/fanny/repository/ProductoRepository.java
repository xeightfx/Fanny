package com.fanny.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fanny.model.EstadoProducto;
import com.fanny.model.Producto;

import jakarta.persistence.LockModeType;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByVendedorIdOrderByIdDesc(Long vendedorId);

    long countByVendedorId(Long vendedorId);

    long countByVendedorIdAndEstado(Long vendedorId, EstadoProducto estado);

    // Bloquea la fila mientras dura la transaccion (evita vender mas stock del que hay)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarParaActualizar(@Param("id") Long id);

    @Query("select distinct p.marca from Producto p where p.estado = com.fanny.model.EstadoProducto.ACTIVO and p.marca is not null order by p.marca")
    List<String> marcas();

    @Query("select distinct p.talla from Producto p where p.estado = com.fanny.model.EstadoProducto.ACTIVO and p.talla is not null order by p.talla")
    List<String> tallas();

    @Query("select distinct p.color from Producto p where p.estado = com.fanny.model.EstadoProducto.ACTIVO and p.color is not null order by p.color")
    List<String> colores();
}
