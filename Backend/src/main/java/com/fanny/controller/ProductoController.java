package com.fanny.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.FiltrosResponse;
import com.fanny.dto.ProductoFiltro;
import com.fanny.dto.ProductoResponse;
import com.fanny.service.ProductoService;

/**
 * Catalogo publico: buscar, filtrar y ver el detalle de un producto.
 * No necesita login (permitAll en SecurityConfig para GET).
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Boolean soloDisponibles,
            @RequestParam(required = false) Boolean soloOfertas,
            @RequestParam(required = false) String orden) {

        ProductoFiltro filtro = new ProductoFiltro(q, categoriaId, marca, precioMin, precioMax,
                talla, color, soloDisponibles, soloOfertas, orden);
        return productoService.buscar(filtro);
    }

    @GetMapping("/filtros")
    public FiltrosResponse filtros() {
        return productoService.filtrosDisponibles();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }
}
