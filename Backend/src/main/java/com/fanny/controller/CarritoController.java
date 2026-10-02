package com.fanny.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.CantidadRequest;
import com.fanny.dto.CarritoItemRequest;
import com.fanny.dto.CarritoResponse;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.CarritoService;

import jakarta.validation.Valid;

/** El carrito es siempre "el mio": se identifica por el usuario logueado, nunca por un id en la URL. */
@RestController
@RequestMapping("/api/cliente/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public CarritoResponse ver(Authentication auth) {
        return carritoService.verCarrito(id(auth));
    }

    @PostMapping("/items")
    public CarritoResponse agregar(Authentication auth, @Valid @RequestBody CarritoItemRequest req) {
        return carritoService.agregarItem(id(auth), req);
    }

    @PutMapping("/items/{productoId}")
    public CarritoResponse actualizarCantidad(Authentication auth, @PathVariable Long productoId,
            @Valid @RequestBody CantidadRequest req) {
        return carritoService.actualizarCantidad(id(auth), productoId, req);
    }

    @DeleteMapping("/items/{productoId}")
    public CarritoResponse quitar(Authentication auth, @PathVariable Long productoId) {
        return carritoService.quitarItem(id(auth), productoId);
    }

    @DeleteMapping
    public void vaciar(Authentication auth) {
        carritoService.vaciar(id(auth));
    }

    private Long id(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getId();
    }
}
