package com.fanny.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.ProductoImagen;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {
}
