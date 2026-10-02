package com.fanny.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
