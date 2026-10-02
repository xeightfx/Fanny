package com.fanny.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.Envio;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    Optional<Envio> findByPedidoId(Long pedidoId);

    List<Envio> findAllByOrderByIdDesc();
}
