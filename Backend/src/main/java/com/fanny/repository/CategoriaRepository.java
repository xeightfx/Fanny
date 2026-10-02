package com.fanny.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Categoria> findByActivaTrueOrderByNombreAsc();

    List<Categoria> findAllByOrderByNombreAsc();
}
