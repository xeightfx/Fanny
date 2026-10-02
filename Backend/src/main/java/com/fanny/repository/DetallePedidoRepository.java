package com.fanny.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fanny.model.DetallePedido;
import com.fanny.model.EstadoPedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    // Cada fila: [nombre del producto, unidades vendidas, total vendido]
    @Query("select p.nombre, sum(d.cantidad), sum(d.subtotal) from DetallePedido d join d.producto p "
            + "where d.pedido.estado not in :excluidos group by p.id, p.nombre order by sum(d.cantidad) desc")
    List<Object[]> masVendidos(@Param("excluidos") Collection<EstadoPedido> excluidos, Pageable pageable);

    // Cada fila: [nombres, apellidos, total vendido, unidades vendidas]
    @Query("select v.nombres, v.apellidos, sum(d.subtotal), sum(d.cantidad) from DetallePedido d "
            + "join d.producto p join p.vendedor v where d.pedido.estado not in :excluidos "
            + "group by v.id, v.nombres, v.apellidos order by sum(d.subtotal) desc")
    List<Object[]> ventasPorVendedor(@Param("excluidos") Collection<EstadoPedido> excluidos);

    @Query("select sum(d.subtotal) from DetallePedido d where d.producto.vendedor.id = :vendedorId "
            + "and d.pedido.estado not in :excluidos")
    BigDecimal totalVentasVendedor(@Param("vendedorId") Long vendedorId,
            @Param("excluidos") Collection<EstadoPedido> excluidos);
}
