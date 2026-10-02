package com.fanny.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fanny.model.Devolucion;
import com.fanny.model.EstadoDevolucion;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    List<Devolucion> findByClienteIdOrderByFechaSolicitudDesc(Long clienteId);

    List<Devolucion> findAllByOrderByFechaSolicitudDesc();

    List<Devolucion> findByEstadoOrderByFechaSolicitudDesc(EstadoDevolucion estado);

    List<Devolucion> findByPedidoId(Long pedidoId);

    long countByEstado(EstadoDevolucion estado);
}
