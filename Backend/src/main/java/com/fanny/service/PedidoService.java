package com.fanny.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.CheckoutRequest;
import com.fanny.dto.DetallePedidoResponse;
import com.fanny.dto.PasoSeguimiento;
import com.fanny.dto.PedidoResponse;
import com.fanny.dto.SeguimientoResponse;
import com.fanny.exception.AccesoNoAutorizadoException;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.exception.ValidacionNegocioException;
import com.fanny.model.Carrito;
import com.fanny.model.Direccion;
import com.fanny.model.DetalleCarrito;
import com.fanny.model.DetallePedido;
import com.fanny.model.Envio;
import com.fanny.model.EstadoPedido;
import com.fanny.model.MovimientoInventario;
import com.fanny.model.Pedido;
import com.fanny.model.Producto;
import com.fanny.model.TipoMovimiento;
import com.fanny.model.Usuario;
import com.fanny.repository.EnvioRepository;
import com.fanny.repository.MovimientoInventarioRepository;
import com.fanny.repository.PedidoRepository;
import com.fanny.repository.ProductoRepository;

/**
 * Reglas de negocio de pedidos, incluida la transaccion completa de la compra (checkout).
 *
 * GRASP - Controller: PedidoService actua como el "controlador de caso de uso" de la
 * compra: coordina Carrito, Producto, Inventario y Pedido sin que ninguno de ellos
 * tenga que conocer a los demas (Low Coupling).
 */
@Service
public class PedidoService {

    private static final Set<EstadoPedido> ESTADOS_QUE_NO_CUENTAN_COMO_VENTA =
            EnumSet.of(EstadoPedido.CANCELADO, EstadoPedido.DEVUELTO);

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final EnvioRepository envioRepository;
    private final CarritoService carritoService;
    private final DireccionService direccionService;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository, EnvioRepository envioRepository,
            CarritoService carritoService, DireccionService direccionService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.envioRepository = envioRepository;
        this.carritoService = carritoService;
        this.direccionService = direccionService;
    }

    /**
     * Transaccion de la compra (seccion 17 del proyecto):
     * crear pedido -> registrar detalles -> verificar y descontar stock ->
     * registrar movimiento -> vaciar carrito -> confirmar.
     * Si algo falla (ej: no hay stock), @Transactional revierte TODO: no queda
     * un pedido a medias ni stock descontado de mas.
     */
    @Transactional
    public PedidoResponse checkout(Usuario cliente, CheckoutRequest req) {
        Carrito carrito = carritoService.buscarCarritoDelUsuario(cliente.getId());
        if (carrito.getDetalles().isEmpty()) {
            throw new ValidacionNegocioException("Tu carrito esta vacio");
        }

        Direccion direccion = null;
        if (req.direccionId() != null) {
            direccion = direccionService.buscarDelUsuario(cliente.getId(), req.direccionId());
        }

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(generarNumeroPedido());
        pedido.setCliente(cliente);
        pedido.setDireccion(direccion);
        pedido.setMetodoEntrega(req.metodoEntrega());
        pedido.setEstado(EstadoPedido.PAGADO); // pago simulado: se marca como pagado al confirmar

        BigDecimal total = BigDecimal.ZERO;

        // Bloquea cada producto (PESSIMISTIC_WRITE) para que dos compras simultaneas
        // no descuenten el mismo stock dos veces.
        for (DetalleCarrito itemCarrito : carrito.getDetalles()) {
            Producto producto = productoRepository.buscarParaActualizar(itemCarrito.getProducto().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Un producto de tu carrito ya no existe"));

            if (itemCarrito.getCantidad() > producto.getStock()) {
                throw new ValidacionNegocioException(
                        "Ya no hay stock suficiente de " + producto.getNombre() + " para completar la compra");
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(itemCarrito.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemCarrito.getCantidad()));
            detalle.setSubtotal(subtotal);
            pedido.getDetalles().add(detalle);
            total = total.add(subtotal);

            producto.setStock(producto.getStock() - itemCarrito.getCantidad());

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setTipo(TipoMovimiento.SALIDA);
            movimiento.setCantidad(itemCarrito.getCantidad());
            movimiento.setUsuario(cliente);
            movimiento.setPedido(pedido);
            movimiento.setObservacion("Venta - pedido " + pedido.getNumeroPedido());
            movimientoInventarioRepository.save(movimiento);
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        carritoService.vaciar(cliente.getId());

        return aResponse(pedido);
    }

    public List<PedidoResponse> misPedidos(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(clienteId).stream()
                .map(this::aResponse).toList();
    }

    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc().stream().map(this::aResponse).toList();
    }

    public List<PedidoResponse> pedidosDelVendedor(Long vendedorId) {
        return pedidoRepository.pedidosDelVendedor(vendedorId).stream().map(this::aResponse).toList();
    }

    public List<PedidoResponse> pedidosPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstadoOrderByFechaPedidoDesc(estado).stream().map(this::aResponse).toList();
    }

    public PedidoResponse obtener(Long id) {
        return aResponse(buscarONotFound(id));
    }

    public PedidoResponse obtenerDelCliente(Usuario cliente, Long id) {
        Pedido pedido = buscarONotFound(id);
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoNoAutorizadoException("Este pedido no te pertenece");
        }
        return aResponse(pedido);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarONotFound(id);
        pedido.setEstado(nuevoEstado);
        return aResponse(pedido);
    }

    public SeguimientoResponse seguimiento(Usuario cliente, Long id) {
        Pedido pedido = buscarONotFound(id);
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoNoAutorizadoException("Este pedido no te pertenece");
        }

        List<EstadoPedido> flujo = List.of(EstadoPedido.PAGADO, EstadoPedido.EN_PREPARACION,
                EstadoPedido.PREPARADO, EstadoPedido.DESPACHADO, EstadoPedido.EN_TRANSITO,
                EstadoPedido.ENTREGADO);
        List<String> etiquetas = List.of("Pago confirmado", "En preparacion", "Preparado",
                "Despachado", "En transito", "Entregado");

        int indiceActual = flujo.indexOf(pedido.getEstado());
        List<PasoSeguimiento> pasos = new java.util.ArrayList<>();
        for (int i = 0; i < flujo.size(); i++) {
            pasos.add(new PasoSeguimiento(etiquetas.get(i), indiceActual >= 0 && i <= indiceActual));
        }

        var envioResponse = envioRepository.findByPedidoId(pedido.getId())
                .map(this::envioAResponse)
                .orElse(null);

        return new SeguimientoResponse(pedido.getNumeroPedido(), pedido.getEstado().name(), pasos, envioResponse);
    }

    public Pedido buscarONotFound(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el pedido con id " + id));
    }

    public BigDecimal totalVentas() {
        BigDecimal total = pedidoRepository.totalVentas(ESTADOS_QUE_NO_CUENTAN_COMO_VENTA);
        return total == null ? BigDecimal.ZERO : total;
    }

    private String generarNumeroPedido() {
        String fecha = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sufijo = 100000 + (int) (Math.random() * 900000);
        return "PED-" + fecha + "-" + sufijo;
    }

    private com.fanny.dto.EnvioResponse envioAResponse(Envio e) {
        return new com.fanny.dto.EnvioResponse(e.getId(), e.getPedido().getId(), e.getPedido().getNumeroPedido(),
                e.getEstado().name(), e.getEmpresaTransporte(), e.getCodigoSeguimiento(),
                e.getResponsable() != null ? e.getResponsable().getNombres() + " " + e.getResponsable().getApellidos() : null,
                e.getFechaDespacho(), e.getFechaEntrega());
    }

    private PedidoResponse aResponse(Pedido p) {
        List<DetallePedidoResponse> detalles = p.getDetalles().stream()
                .map(d -> {
                    String imagen = d.getProducto().getImagenes().isEmpty() ? null
                            : d.getProducto().getImagenes().get(0).getUrl();
                    return new DetallePedidoResponse(d.getProducto().getId(), d.getProducto().getNombre(), imagen,
                            d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
                })
                .toList();

        String direccionTexto = p.getDireccion() == null ? null
                : p.getDireccion().getDireccion() + ", " + p.getDireccion().getDistrito();

        return new PedidoResponse(p.getId(), p.getNumeroPedido(), p.getCliente().getId(),
                p.getCliente().getNombres() + " " + p.getCliente().getApellidos(), direccionTexto,
                p.getMetodoEntrega().name(), p.getEstado().name(), p.getTotal(), p.getFechaPedido(), detalles);
    }
}
