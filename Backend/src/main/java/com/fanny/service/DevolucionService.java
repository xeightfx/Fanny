package com.fanny.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.DevolucionRequest;
import com.fanny.dto.DevolucionResponse;
import com.fanny.exception.AccesoNoAutorizadoException;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.exception.ValidacionNegocioException;
import com.fanny.model.Devolucion;
import com.fanny.model.EstadoDevolucion;
import com.fanny.model.EstadoPedido;
import com.fanny.model.Pedido;
import com.fanny.model.Producto;
import com.fanny.model.Usuario;
import com.fanny.repository.DevolucionRepository;
import com.fanny.repository.PedidoRepository;
import com.fanny.repository.ProductoRepository;

/**
 * Reglas de negocio de devoluciones (seccion 14): el cliente solicita,
 * el administrador supervisa. Al aprobar, el stock regresa via InventarioService.
 */
@Service
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    public DevolucionService(DevolucionRepository devolucionRepository, PedidoRepository pedidoRepository,
            ProductoRepository productoRepository, InventarioService inventarioService) {
        this.devolucionRepository = devolucionRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
    }

    @Transactional
    public DevolucionResponse solicitar(Usuario cliente, DevolucionRequest req) {
        Pedido pedido = pedidoRepository.findById(req.pedidoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el pedido"));
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoNoAutorizadoException("Este pedido no te pertenece");
        }
        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new ValidacionNegocioException("Solo se puede solicitar devolucion de pedidos ya entregados");
        }

        Producto producto = productoRepository.findById(req.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el producto"));

        Devolucion devolucion = new Devolucion();
        devolucion.setPedido(pedido);
        devolucion.setProducto(producto);
        devolucion.setCliente(cliente);
        devolucion.setCantidad(req.cantidad());
        devolucion.setMotivo(req.motivo());
        return aResponse(devolucionRepository.save(devolucion));
    }

    public List<DevolucionResponse> misDevoluciones(Long clienteId) {
        return devolucionRepository.findByClienteIdOrderByFechaSolicitudDesc(clienteId).stream()
                .map(this::aResponse).toList();
    }

    public List<DevolucionResponse> listarTodas() {
        return devolucionRepository.findAllByOrderByFechaSolicitudDesc().stream().map(this::aResponse).toList();
    }

    public List<DevolucionResponse> porEstado(EstadoDevolucion estado) {
        return devolucionRepository.findByEstadoOrderByFechaSolicitudDesc(estado).stream()
                .map(this::aResponse).toList();
    }

    @Transactional
    public DevolucionResponse cambiarEstado(Usuario responsable, Long id, EstadoDevolucion nuevoEstado) {
        Devolucion devolucion = buscarONotFound(id);
        devolucion.setEstado(nuevoEstado);
        devolucion.setFechaResolucion(OffsetDateTime.now());

        // Solo cuando se APRUEBA se devuelve el stock; evita descontarlo dos veces
        // si el estado se actualiza varias veces (ej: EN_REVISION -> APROBADA).
        if (nuevoEstado == EstadoDevolucion.APROBADA) {
            inventarioService.registrarDevolucionEnInventario(responsable, devolucion.getProducto(),
                    devolucion.getCantidad(), devolucion.getPedido());
        }
        return aResponse(devolucion);
    }

    public Devolucion buscarONotFound(Long id) {
        return devolucionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la devolucion con id " + id));
    }

    private DevolucionResponse aResponse(Devolucion d) {
        return new DevolucionResponse(d.getId(), d.getPedido().getId(), d.getPedido().getNumeroPedido(),
                d.getProducto().getId(), d.getProducto().getNombre(), d.getCliente().getId(),
                d.getCliente().getNombres() + " " + d.getCliente().getApellidos(), d.getCantidad(), d.getMotivo(),
                d.getEstado().name(), d.getFechaSolicitud(), d.getFechaResolucion());
    }
}
