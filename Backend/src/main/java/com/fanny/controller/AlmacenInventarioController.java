package com.fanny.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.AjusteRequest;
import com.fanny.dto.EntradaRequest;
import com.fanny.dto.InventarioConfigRequest;
import com.fanny.dto.InventarioResponse;
import com.fanny.dto.MovimientoResponse;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.InventarioService;

import jakarta.validation.Valid;

/** Dashboard de almacen (seccion 22): stock, entradas, ajustes e historial. */
@RestController
@RequestMapping("/api/almacen/inventario")
public class AlmacenInventarioController {

    private final InventarioService inventarioService;

    public AlmacenInventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public List<InventarioResponse> listar() {
        return inventarioService.listar();
    }

    @GetMapping("/stock-bajo")
    public List<InventarioResponse> stockBajo() {
        return inventarioService.stockBajo();
    }

    @PutMapping("/{productoId}/configuracion")
    public InventarioResponse configurar(@PathVariable Long productoId,
            @Valid @RequestBody InventarioConfigRequest req) {
        return inventarioService.configurar(productoId, req);
    }

    @PostMapping("/entradas")
    public MovimientoResponse entrada(Authentication auth, @Valid @RequestBody EntradaRequest req) {
        return inventarioService.registrarEntrada(usuario(auth), req);
    }

    @PostMapping("/ajustes")
    public MovimientoResponse ajuste(Authentication auth, @Valid @RequestBody AjusteRequest req) {
        return inventarioService.registrarAjuste(usuario(auth), req);
    }

    @GetMapping("/movimientos")
    public List<MovimientoResponse> historial() {
        return inventarioService.historial();
    }

    @GetMapping("/movimientos/{productoId}")
    public List<MovimientoResponse> historialDeProducto(@PathVariable Long productoId) {
        return inventarioService.historialDeProducto(productoId);
    }

    private com.fanny.model.Usuario usuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuario();
    }
}
