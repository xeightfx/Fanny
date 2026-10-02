package com.fanny.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.CarritoItemRequest;
import com.fanny.dto.CarritoItemResponse;
import com.fanny.dto.CarritoResponse;
import com.fanny.dto.CantidadRequest;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.exception.ValidacionNegocioException;
import com.fanny.model.Carrito;
import com.fanny.model.DetalleCarrito;
import com.fanny.model.Producto;
import com.fanny.repository.CarritoRepository;
import com.fanny.repository.DetalleCarritoRepository;
import com.fanny.repository.ProductoRepository;

/**
 * Reglas de negocio del carrito de compras.
 * GRASP - Information Expert: el carrito conoce sus detalles; este Service solo
 * valida stock y calcula totales, que es informacion que vive en Producto y en los detalles.
 */
@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleCarritoRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository, DetalleCarritoRepository detalleCarritoRepository,
            ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.detalleCarritoRepository = detalleCarritoRepository;
        this.productoRepository = productoRepository;
    }

    public CarritoResponse verCarrito(Long usuarioId) {
        return aResponse(buscarCarritoDelUsuario(usuarioId));
    }

    @Transactional
    public CarritoResponse agregarItem(Long usuarioId, CarritoItemRequest req) {
        Carrito carrito = buscarCarritoDelUsuario(usuarioId);
        Producto producto = productoRepository.findById(req.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el producto"));

        DetalleCarrito detalle = carrito.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElse(null);

        int cantidadDeseada = req.cantidad() + (detalle != null ? detalle.getCantidad() : 0);
        validarStock(producto, cantidadDeseada);

        if (detalle != null) {
            detalle.setCantidad(cantidadDeseada);
        } else {
            detalle = new DetalleCarrito();
            detalle.setCarrito(carrito);
            detalle.setProducto(producto);
            detalle.setCantidad(req.cantidad());
            carrito.getDetalles().add(detalle);
        }
        return aResponse(carrito);
    }

    @Transactional
    public CarritoResponse actualizarCantidad(Long usuarioId, Long productoId, CantidadRequest req) {
        Carrito carrito = buscarCarritoDelUsuario(usuarioId);
        DetalleCarrito detalle = buscarDetalle(carrito, productoId);
        validarStock(detalle.getProducto(), req.cantidad());
        detalle.setCantidad(req.cantidad());
        return aResponse(carrito);
    }

    @Transactional
    public CarritoResponse quitarItem(Long usuarioId, Long productoId) {
        Carrito carrito = buscarCarritoDelUsuario(usuarioId);
        DetalleCarrito detalle = buscarDetalle(carrito, productoId);
        carrito.getDetalles().remove(detalle);
        detalleCarritoRepository.delete(detalle);
        return aResponse(carrito);
    }

    @Transactional
    public void vaciar(Long usuarioId) {
        Carrito carrito = buscarCarritoDelUsuario(usuarioId);
        carrito.getDetalles().clear();
    }

    public Carrito buscarCarritoDelUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Este usuario no tiene carrito"));
    }

    private DetalleCarrito buscarDetalle(Carrito carrito, Long productoId) {
        return carrito.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Ese producto no esta en tu carrito"));
    }

    private void validarStock(Producto producto, int cantidadDeseada) {
        if (cantidadDeseada > producto.getStock()) {
            throw new ValidacionNegocioException(
                    "Solo hay " + producto.getStock() + " unidades disponibles de " + producto.getNombre());
        }
    }

    private CarritoResponse aResponse(Carrito carrito) {
        List<CarritoItemResponse> items = carrito.getDetalles().stream()
                .map(d -> {
                    Producto p = d.getProducto();
                    String imagen = p.getImagenes().isEmpty() ? null : p.getImagenes().get(0).getUrl();
                    BigDecimal subtotal = p.getPrecio().multiply(BigDecimal.valueOf(d.getCantidad()));
                    return new CarritoItemResponse(p.getId(), p.getNombre(), p.getMarca(), p.getTalla(),
                            p.getColor(), imagen, p.getPrecio(), d.getCantidad(), subtotal, p.getStock());
                })
                .toList();

        BigDecimal total = items.stream().map(CarritoItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = items.stream().mapToInt(CarritoItemResponse::cantidad).sum();

        return new CarritoResponse(carrito.getId(), items, totalItems, total);
    }
}
