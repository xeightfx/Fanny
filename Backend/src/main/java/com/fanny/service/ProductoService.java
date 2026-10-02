package com.fanny.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.FiltrosResponse;
import com.fanny.dto.PrecioRequest;
import com.fanny.dto.ProductoFiltro;
import com.fanny.dto.ProductoRequest;
import com.fanny.dto.ProductoResponse;
import com.fanny.dto.StockRequest;
import com.fanny.exception.AccesoNoAutorizadoException;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.model.Categoria;
import com.fanny.model.EstadoProducto;
import com.fanny.model.Producto;
import com.fanny.model.ProductoImagen;
import com.fanny.model.Usuario;
import com.fanny.repository.ProductoBusquedaRepository;
import com.fanny.repository.ProductoRepository;

/**
 * Reglas de negocio del catalogo.
 *
 * GRASP - Information Expert: Producto sabe su propio precio y descuento;
 * este Service solo orquesta validaciones y coordina con Categoria.
 * GRASP - Low Coupling: el Controller nunca toca ProductoRepository directamente,
 * siempre pasa por aqui.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoBusquedaRepository productoBusquedaRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository,
            ProductoBusquedaRepository productoBusquedaRepository,
            CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.productoBusquedaRepository = productoBusquedaRepository;
        this.categoriaService = categoriaService;
    }

    public List<ProductoResponse> buscar(ProductoFiltro filtro) {
        return productoBusquedaRepository.buscar(filtro).stream().map(this::aResponse).toList();
    }

    public FiltrosResponse filtrosDisponibles() {
        return new FiltrosResponse(productoRepository.marcas(), productoRepository.tallas(),
                productoRepository.colores());
    }

    public ProductoResponse obtener(Long id) {
        return aResponse(buscarONotFound(id));
    }

    public List<ProductoResponse> misProductos(Long vendedorId) {
        return productoRepository.findByVendedorIdOrderByIdDesc(vendedorId).stream().map(this::aResponse).toList();
    }

    @Transactional
    public ProductoResponse crear(Usuario vendedor, ProductoRequest req) {
        Categoria categoria = categoriaService.buscarONotFound(req.categoriaId());

        Producto producto = new Producto();
        producto.setNombre(req.nombre());
        producto.setDescripcion(req.descripcion());
        producto.setMarca(req.marca());
        producto.setPrecio(req.precio());
        producto.setPrecioAnterior(req.precioAnterior());
        producto.setDescuento(calcularDescuento(req.precio(), req.precioAnterior()));
        producto.setStock(req.stock());
        producto.setTalla(req.talla());
        producto.setColor(req.color());
        producto.setCategoria(categoria);
        producto.setVendedor(vendedor);

        if (req.imagenUrl() != null && !req.imagenUrl().isBlank()) {
            ProductoImagen imagen = new ProductoImagen();
            imagen.setProducto(producto);
            imagen.setUrl(req.imagenUrl());
            imagen.setPrincipal(true);
            producto.getImagenes().add(imagen);
        }

        return aResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Usuario vendedor, Long id, ProductoRequest req) {
        Producto producto = buscarDelVendedorOAdmin(vendedor, id);
        Categoria categoria = categoriaService.buscarONotFound(req.categoriaId());

        producto.setNombre(req.nombre());
        producto.setDescripcion(req.descripcion());
        producto.setMarca(req.marca());
        producto.setPrecio(req.precio());
        producto.setPrecioAnterior(req.precioAnterior());
        producto.setDescuento(calcularDescuento(req.precio(), req.precioAnterior()));
        producto.setStock(req.stock());
        producto.setTalla(req.talla());
        producto.setColor(req.color());
        producto.setCategoria(categoria);

        return aResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizarPrecio(Usuario vendedor, Long id, PrecioRequest req) {
        Producto producto = buscarDelVendedorOAdmin(vendedor, id);
        producto.setPrecioAnterior(producto.getPrecio());
        producto.setPrecio(req.precio());
        producto.setDescuento(calcularDescuento(producto.getPrecio(), producto.getPrecioAnterior()));
        return aResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizarStock(Usuario vendedor, Long id, StockRequest req) {
        Producto producto = buscarDelVendedorOAdmin(vendedor, id);
        producto.setStock(req.stock());
        return aResponse(producto);
    }

    @Transactional
    public void desactivar(Usuario vendedor, Long id) {
        Producto producto = buscarDelVendedorOAdmin(vendedor, id);
        producto.setEstado(EstadoProducto.INACTIVO);
    }

    @Transactional
    public void activar(Usuario vendedor, Long id) {
        Producto producto = buscarDelVendedorOAdmin(vendedor, id);
        producto.setEstado(EstadoProducto.ACTIVO);
    }

    public Producto buscarONotFound(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el producto con id " + id));
    }

    /** El vendedor solo puede tocar sus propios productos; el admin puede tocar cualquiera. */
    private Producto buscarDelVendedorOAdmin(Usuario usuario, Long productoId) {
        Producto producto = buscarONotFound(productoId);
        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));
        if (!esAdmin && !producto.getVendedor().getId().equals(usuario.getId())) {
            throw new AccesoNoAutorizadoException("Este producto no te pertenece");
        }
        return producto;
    }

    private BigDecimal calcularDescuento(BigDecimal precio, BigDecimal precioAnterior) {
        if (precioAnterior == null || precioAnterior.compareTo(BigDecimal.ZERO) <= 0
                || precioAnterior.compareTo(precio) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal diferencia = precioAnterior.subtract(precio);
        return diferencia.multiply(BigDecimal.valueOf(100))
                .divide(precioAnterior, 0, java.math.RoundingMode.HALF_UP);
    }

    private ProductoResponse aResponse(Producto p) {
        String imagenUrl = p.getImagenes().stream()
                .filter(ProductoImagen::getPrincipal)
                .map(ProductoImagen::getUrl)
                .findFirst()
                .orElse(p.getImagenes().isEmpty() ? null : p.getImagenes().get(0).getUrl());

        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getMarca(), p.getPrecio(),
                p.getPrecioAnterior(), p.getDescuento(), p.getStock(), p.getTalla(), p.getColor(),
                p.getEstado().name(), p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getVendedor().getId(), p.getVendedor().getNombres() + " " + p.getVendedor().getApellidos(),
                imagenUrl, p.getImagenes().stream().map(ProductoImagen::getUrl).toList());
    }
}
