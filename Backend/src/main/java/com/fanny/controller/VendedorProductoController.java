package com.fanny.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.PrecioRequest;
import com.fanny.dto.ProductoRequest;
import com.fanny.dto.ProductoResponse;
import com.fanny.dto.StockRequest;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.ProductoService;

import jakarta.validation.Valid;

/**
 * El vendedor administra SOLO sus propios productos (seccion 20).
 * ProductoService valida internamente que el producto le pertenezca.
 */
@RestController
@RequestMapping("/api/vendedor/productos")
public class VendedorProductoController {

    private final ProductoService productoService;

    public VendedorProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> misProductos(Authentication auth) {
        return productoService.misProductos(usuario(auth).getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(Authentication auth, @Valid @RequestBody ProductoRequest req) {
        return productoService.crear(usuario(auth).getUsuario(), req);
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody ProductoRequest req) {
        return productoService.actualizar(usuario(auth).getUsuario(), id, req);
    }

    @PutMapping("/{id}/precio")
    public ProductoResponse actualizarPrecio(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody PrecioRequest req) {
        return productoService.actualizarPrecio(usuario(auth).getUsuario(), id, req);
    }

    @PutMapping("/{id}/stock")
    public ProductoResponse actualizarStock(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody StockRequest req) {
        return productoService.actualizarStock(usuario(auth).getUsuario(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(Authentication auth, @PathVariable Long id) {
        productoService.desactivar(usuario(auth).getUsuario(), id);
    }

    @PostMapping("/{id}/activar")
    public void activar(Authentication auth, @PathVariable Long id) {
        productoService.activar(usuario(auth).getUsuario(), id);
    }

    private UsuarioPrincipal usuario(Authentication auth) {
        return (UsuarioPrincipal) auth.getPrincipal();
    }
}
