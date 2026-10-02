package com.fanny.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.DevolucionEstadoRequest;
import com.fanny.dto.DevolucionResponse;
import com.fanny.dto.PedidoEstadoRequest;
import com.fanny.dto.PedidoResponse;
import org.springframework.security.core.Authentication;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.DevolucionService;
import com.fanny.service.PedidoService;

import jakarta.validation.Valid;

/** El administrador supervisa TODOS los pedidos y devoluciones (secciones 1, 12 y 14). */
@RestController
@RequestMapping("/api/admin")
public class AdminPedidoController {

    private final PedidoService pedidoService;
    private final DevolucionService devolucionService;

    public AdminPedidoController(PedidoService pedidoService, DevolucionService devolucionService) {
        this.pedidoService = pedidoService;
        this.devolucionService = devolucionService;
    }

    @GetMapping("/pedidos")
    public List<PedidoResponse> listarPedidos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/pedidos/{id}")
    public PedidoResponse obtenerPedido(@PathVariable Long id) {
        return pedidoService.obtener(id);
    }

    @PutMapping("/pedidos/{id}/estado")
    public PedidoResponse cambiarEstadoPedido(@PathVariable Long id, @Valid @RequestBody PedidoEstadoRequest req) {
        return pedidoService.cambiarEstado(id, req.estado());
    }

    @GetMapping("/devoluciones")
    public List<DevolucionResponse> listarDevoluciones() {
        return devolucionService.listarTodas();
    }

    @PutMapping("/devoluciones/{id}/estado")
    public DevolucionResponse cambiarEstadoDevolucion(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody DevolucionEstadoRequest req) {
        return devolucionService.cambiarEstado(usuario(auth), id, req.estado());
    }

    private com.fanny.model.Usuario usuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuario();
    }
}
