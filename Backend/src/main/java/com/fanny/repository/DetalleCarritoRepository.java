package com.fanny.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.DetalleCarrito;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
}
