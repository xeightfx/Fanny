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

import com.fanny.dto.DespachoRequest;
import com.fanny.dto.EnvioEstadoRequest;
import com.fanny.dto.EnvioResponse;
import com.fanny.dto.PedidoResponse;
import com.fanny.model.EstadoPedido;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.EnvioService;
import com.fanny.service.PedidoService;

import jakarta.validation.Valid;

/** Preparacion de pedidos y despachos que hace el almacen (seccion 22). */
@RestController
@RequestMapping("/api/almacen")
public class AlmacenPedidoController {

    private final PedidoService pedidoService;
    private final EnvioService envioService;

    public AlmacenPedidoController(PedidoService pedidoService, EnvioService envioService) {
        this.pedidoService = pedidoService;
        this.envioService = envioService;
    }

    @GetMapping("/pedidos/pendientes")
    public List<PedidoResponse> pendientes() {
        return pedidoService.pedidosPorEstado(EstadoPedido.PAGADO);
    }

    @GetMapping("/pedidos/preparados")
    public List<PedidoResponse> preparados() {
        return pedidoService.pedidosPorEstado(EstadoPedido.PREPARADO);
    }

    @PutMapping("/pedidos/{id}/preparar")
    public PedidoResponse preparar(@PathVariable Long id) {
        return pedidoService.cambiarEstado(id, EstadoPedido.EN_PREPARACION);
    }

    @PutMapping("/pedidos/{id}/marcar-preparado")
    public PedidoResponse marcarPreparado(@PathVariable Long id) {
        return pedidoService.cambiarEstado(id, EstadoPedido.PREPARADO);
    }

    @PostMapping("/pedidos/{id}/despacho")
    public EnvioResponse despachar(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody DespachoRequest req) {
        return envioService.registrarDespacho(usuario(auth), id, req);
    }

    @PutMapping("/envios/{pedidoId}/estado")
    public EnvioResponse actualizarEnvio(@PathVariable Long pedidoId, @Valid @RequestBody EnvioEstadoRequest req) {
        return envioService.actualizarEstado(pedidoId, req);
    }

    @GetMapping("/envios")
    public List<EnvioResponse> listarEnvios() {
        return envioService.listar();
    }

    private com.fanny.model.Usuario usuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuario();
    }
}
