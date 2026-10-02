package com.fanny.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fanny.model.Consulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByProductoIdOrderByFechaDesc(Long productoId);

    List<Consulta> findByClienteIdOrderByFechaDesc(Long clienteId);

    @Query("select c from Consulta c where c.producto.vendedor.id = :vendedorId order by c.fecha desc")
    List<Consulta> deVendedor(@Param("vendedorId") Long vendedorId);
}
