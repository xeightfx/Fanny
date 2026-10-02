package com.fanny.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.RespuestaConsulta;

public interface RespuestaConsultaRepository extends JpaRepository<RespuestaConsulta, Long> {

    List<RespuestaConsulta> findByConsultaId(Long consultaId);
}
