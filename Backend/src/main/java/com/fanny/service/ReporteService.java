package com.fanny.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fanny.dto.DashboardResponse;
import com.fanny.dto.ReporteItem;
import com.fanny.dto.VendedorResumenResponse;
import com.fanny.model.EstadoDevolucion;
import com.fanny.model.EstadoPedido;
import com.fanny.model.EstadoProducto;
import com.fanny.repository.DetallePedidoRepository;
import com.fanny.repository.DevolucionRepository;
import com.fanny.repository.InventarioRepository;
import com.fanny.repository.PedidoRepository;
import com.fanny.repository.ProductoRepository;
import com.fanny.repository.UsuarioRepository;

/**
 * Reportes para los dashboards de administrador y vendedor (secciones 19 y 20).
 * Esta clase solo LEE y agrupa datos; no cambia el estado de nada.
 */
@Service
public class ReporteService {

    private static final Set<EstadoPedido> ESTADOS_EXCLUIDOS_DE_VENTAS =
            EnumSet.of(EstadoPedido.CANCELADO, EstadoPedido.DEVUELTO);

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final InventarioRepository inventarioRepository;
    private final DevolucionRepository devolucionRepository;

    public ReporteService(UsuarioRepository usuarioRepository, ProductoRepository productoRepository,
            PedidoRepository pedidoRepository, DetallePedidoRepository detallePedidoRepository,
            InventarioRepository inventarioRepository, DevolucionRepository devolucionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.inventarioRepository = inventarioRepository;
        this.devolucionRepository = devolucionRepository;
    }

    public DashboardResponse dashboardAdmin() {
        BigDecimal ventas = pedidoRepository.totalVentas(ESTADOS_EXCLUIDOS_DE_VENTAS);
        long devolucionesActivas = devolucionRepository.countByEstado(EstadoDevolucion.SOLICITADA)
                + devolucionRepository.countByEstado(EstadoDevolucion.EN_REVISION);

        return new DashboardResponse(
                usuarioRepository.count(),
                productoRepository.count(),
                pedidoRepository.count(),
                ventas == null ? BigDecimal.ZERO : ventas,
                inventarioRepository.stockBajo().size(),
                devolucionesActivas);
    }

    public VendedorResumenResponse resumenVendedor(Long vendedorId) {
        BigDecimal ventas = detallePedidoRepository.totalVentasVendedor(vendedorId, ESTADOS_EXCLUIDOS_DE_VENTAS);
        return new VendedorResumenResponse(
                productoRepository.countByVendedorId(vendedorId),
                productoRepository.countByVendedorIdAndEstado(vendedorId, EstadoProducto.ACTIVO),
                inventarioRepository.stockBajoDelVendedor(vendedorId).size(),
                pedidoRepository.pedidosDelVendedor(vendedorId).size(),
                ventas == null ? BigDecimal.ZERO : ventas);
    }

    public List<ReporteItem> ventasPorFecha(OffsetDateTime desde, OffsetDateTime hasta) {
        return pedidoRepository.findByFechaPedidoBetween(desde, hasta).stream()
                .filter(p -> !ESTADOS_EXCLUIDOS_DE_VENTAS.contains(p.getEstado()))
                .map(p -> new ReporteItem(p.getNumeroPedido(), p.getTotal(), 1L))
                .toList();
    }

    public List<ReporteItem> ventasPorVendedor() {
        return detallePedidoRepository.ventasPorVendedor(ESTADOS_EXCLUIDOS_DE_VENTAS).stream()
                .map(fila -> new ReporteItem(fila[0] + " " + fila[1], (BigDecimal) fila[2], (Long) fila[3]))
                .toList();
    }

    public List<ReporteItem> productosMasVendidos(int top) {
        return detallePedidoRepository.masVendidos(ESTADOS_EXCLUIDOS_DE_VENTAS, PageRequest.of(0, top)).stream()
                .map(fila -> new ReporteItem((String) fila[0], (BigDecimal) fila[2], (Long) fila[1]))
                .toList();
    }

    public List<ReporteItem> pedidosPorEstado() {
        return java.util.Arrays.stream(EstadoPedido.values())
                .map(estado -> new ReporteItem(estado.name(), null, pedidoRepository.countByEstado(estado)))
                .toList();
    }

    public List<ReporteItem> devolucionesPorEstado() {
        return java.util.Arrays.stream(EstadoDevolucion.values())
                .map(estado -> new ReporteItem(estado.name(), null, devolucionRepository.countByEstado(estado)))
                .toList();
    }
}
