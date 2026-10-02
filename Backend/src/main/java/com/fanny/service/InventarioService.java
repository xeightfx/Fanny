package com.fanny.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.AjusteRequest;
import com.fanny.dto.EntradaRequest;
import com.fanny.dto.InventarioConfigRequest;
import com.fanny.dto.InventarioResponse;
import com.fanny.dto.MovimientoResponse;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.model.Inventario;
import com.fanny.model.MovimientoInventario;
import com.fanny.model.Pedido;
import com.fanny.model.Producto;
import com.fanny.model.TipoMovimiento;
import com.fanny.model.Usuario;
import com.fanny.repository.InventarioRepository;
import com.fanny.repository.MovimientoInventarioRepository;
import com.fanny.repository.ProductoRepository;

/**
 * Reglas de negocio de inventario: entradas de mercaderia, ajustes de stock y
 * el historial de movimientos. Toda entrada/ajuste queda registrada (seccion 13).
 */
@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository, ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    public List<InventarioResponse> listar() {
        return inventarioRepository.findAllByOrderByProductoNombreAsc().stream().map(this::aResponse).toList();
    }

    public List<InventarioResponse> stockBajo() {
        return inventarioRepository.stockBajo().stream().map(this::aResponse).toList();
    }

    public List<InventarioResponse> stockBajoDelVendedor(Long vendedorId) {
        return inventarioRepository.stockBajoDelVendedor(vendedorId).stream().map(this::aResponse).toList();
    }

    @Transactional
    public InventarioResponse configurar(Long productoId, InventarioConfigRequest req) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId).orElseGet(() -> {
            Inventario nuevo = new Inventario();
            nuevo.setProducto(productoRepository.findById(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el producto")));
            return nuevo;
        });
        inventario.setStockMinimo(req.stockMinimo());
        inventario.setUbicacion(req.ubicacion());
        return aResponse(inventarioRepository.save(inventario));
    }

    @Transactional
    public MovimientoResponse registrarEntrada(Usuario responsable, EntradaRequest req) {
        Producto producto = productoRepository.buscarParaActualizar(req.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el producto"));

        producto.setStock(producto.getStock() + req.cantidad());

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(req.cantidad());
        movimiento.setUsuario(responsable);
        movimiento.setObservacion(req.observacion());
        return movimientoAResponse(movimientoInventarioRepository.save(movimiento));
    }

    @Transactional
    public MovimientoResponse registrarAjuste(Usuario responsable, AjusteRequest req) {
        Producto producto = productoRepository.buscarParaActualizar(req.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el producto"));

        int diferencia = req.nuevoStock() - producto.getStock();
        producto.setStock(req.nuevoStock());

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.AJUSTE);
        movimiento.setCantidad(Math.abs(diferencia));
        movimiento.setUsuario(responsable);
        movimiento.setObservacion(req.observacion());
        return movimientoAResponse(movimientoInventarioRepository.save(movimiento));
    }

    @Transactional
    public void registrarDevolucionEnInventario(Usuario responsable, Producto producto, int cantidad, Pedido pedido) {
        producto.setStock(producto.getStock() + cantidad);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.DEVOLUCION);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(responsable);
        movimiento.setPedido(pedido);
        movimiento.setObservacion("Devolucion aprobada - pedido " + pedido.getNumeroPedido());
        movimientoInventarioRepository.save(movimiento);
    }

    public List<MovimientoResponse> historial() {
        return movimientoInventarioRepository.findTop100ByOrderByFechaDesc().stream()
                .map(this::movimientoAResponse).toList();
    }

    public List<MovimientoResponse> historialDeProducto(Long productoId) {
        return movimientoInventarioRepository.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(this::movimientoAResponse).toList();
    }

    private InventarioResponse aResponse(Inventario i) {
        Producto p = i.getProducto();
        return new InventarioResponse(p.getId(), p.getNombre(), p.getMarca(), p.getStock(), i.getStockMinimo(),
                i.getUbicacion(), p.getStock() <= i.getStockMinimo());
    }

    private MovimientoResponse movimientoAResponse(MovimientoInventario m) {
        return new MovimientoResponse(m.getId(), m.getProducto().getId(), m.getProducto().getNombre(),
                m.getTipo().name(), m.getCantidad(), m.getFecha(),
                m.getUsuario().getNombres() + " " + m.getUsuario().getApellidos(),
                m.getPedido() != null ? m.getPedido().getNumeroPedido() : null, m.getObservacion());
    }
}