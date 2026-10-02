package com.fanny.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.CheckoutRequest;
import com.fanny.dto.DevolucionRequest;
import com.fanny.dto.DevolucionResponse;
import com.fanny.dto.PedidoResponse;
import com.fanny.dto.SeguimientoResponse;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.DevolucionService;
import com.fanny.service.PedidoService;

import jakarta.validation.Valid;

/** Todo lo que el CLIENTE puede hacer con sus propios pedidos: comprar, ver y seguir. */
@RestController
@RequestMapping("/api/cliente")
public class PedidoClienteController {

    private final PedidoService pedidoService;
    private final DevolucionService devolucionService;

    public PedidoClienteController(PedidoService pedidoService, DevolucionService devolucionService) {
        this.pedidoService = pedidoService;
        this.devolucionService = devolucionService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse checkout(Authentication auth, @Valid @RequestBody CheckoutRequest req) {
        return pedidoService.checkout(usuario(auth), req);
    }

    @GetMapping("/pedidos")
    public List<PedidoResponse> misPedidos(Authentication auth) {
        return pedidoService.misPedidos(id(auth));
    }

    @GetMapping("/pedidos/{id}")
    public PedidoResponse obtener(Authentication auth, @PathVariable Long id) {
        return pedidoService.obtenerDelCliente(usuario(auth), id);
    }

    @GetMapping("/pedidos/{id}/seguimiento")
    public SeguimientoResponse seguimiento(Authentication auth, @PathVariable Long id) {
        return pedidoService.seguimiento(usuario(auth), id);
    }

    @PostMapping("/devoluciones")
    @ResponseStatus(HttpStatus.CREATED)
    public DevolucionResponse solicitarDevolucion(Authentication auth, @Valid @RequestBody DevolucionRequest req) {
        return devolucionService.solicitar(usuario(auth), req);
    }

    @GetMapping("/devoluciones")
    public List<DevolucionResponse> misDevoluciones(Authentication auth) {
        return devolucionService.misDevoluciones(id(auth));
    }

    private Long id(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getId();
    }

    private com.fanny.model.Usuario usuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuario();
    }
}
