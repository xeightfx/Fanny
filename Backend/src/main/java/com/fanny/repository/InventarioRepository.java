package com.fanny.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fanny.model.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoId(Long productoId);

    List<Inventario> findAllByOrderByProductoNombreAsc();

    @Query("select i from Inventario i where i.producto.stock <= i.stockMinimo order by i.producto.nombre")
    List<Inventario> stockBajo();

    @Query("select i from Inventario i where i.producto.vendedor.id = :vendedorId and i.producto.stock <= i.stockMinimo")
    List<Inventario> stockBajoDelVendedor(@Param("vendedorId") Long vendedorId);
}
