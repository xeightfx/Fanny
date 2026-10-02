package com.fanny.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.Direccion;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByUsuarioIdOrderByPrincipalDescIdAsc(Long usuarioId);
}
