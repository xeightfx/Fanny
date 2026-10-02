package com.fanny.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.MovimientoInventario;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findTop100ByOrderByFechaDesc();

    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(Long productoId);
}
