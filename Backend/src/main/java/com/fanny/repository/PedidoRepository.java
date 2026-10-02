package com.fanny.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fanny.model.EstadoPedido;
import com.fanny.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByFechaPedidoDesc(Long clienteId);

    List<Pedido> findAllByOrderByFechaPedidoDesc();

    List<Pedido> findByEstadoOrderByFechaPedidoDesc(EstadoPedido estado);

    List<Pedido> findByEstadoInOrderByFechaPedidoDesc(Collection<EstadoPedido> estados);

    List<Pedido> findByFechaPedidoBetween(OffsetDateTime desde, OffsetDateTime hasta);

    long countByEstado(EstadoPedido estado);

    @Query("select distinct p from Pedido p join p.detalles d where d.producto.vendedor.id = :vendedorId order by p.fechaPedido desc")
    List<Pedido> pedidosDelVendedor(@Param("vendedorId") Long vendedorId);

    @Query("select sum(p.total) from Pedido p where p.estado not in :excluidos")
    BigDecimal totalVentas(@Param("excluidos") Collection<EstadoPedido> excluidos);
}
