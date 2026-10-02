package com.fanny.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.ConsultaResponse;
import com.fanny.dto.PedidoResponse;
import com.fanny.dto.RespuestaRequest;
import com.fanny.dto.RespuestaResponse;
import com.fanny.dto.VendedorResumenResponse;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.ConsultaService;
import com.fanny.service.PedidoService;
import com.fanny.service.ReporteService;

import jakarta.validation.Valid;

/** Dashboard del vendedor: sus pedidos, sus consultas y su resumen de ventas (seccion 20). */
@RestController
@RequestMapping("/api/vendedor")
public class VendedorController {

    private final PedidoService pedidoService;
    private final ConsultaService consultaService;
    private final ReporteService reporteService;

    public VendedorController(PedidoService pedidoService, ConsultaService consultaService,
            ReporteService reporteService) {
        this.pedidoService = pedidoService;
        this.consultaService = consultaService;
        this.reporteService = reporteService;
    }

    @GetMapping("/pedidos")
    public List<PedidoResponse> misPedidos(Authentication auth) {
        return pedidoService.pedidosDelVendedor(id(auth));
    }

    @GetMapping("/consultas")
    public List<ConsultaResponse> misConsultas(Authentication auth) {
        return consultaService.deVendedor(id(auth));
    }

    @PostMapping("/consultas/{id}/responder")
    public RespuestaResponse responder(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody RespuestaRequest req) {
        return consultaService.responder(usuario(auth), id, req);
    }

    @GetMapping("/resumen")
    public VendedorResumenResponse resumen(Authentication auth) {
        return reporteService.resumenVendedor(id(auth));
    }

    private Long id(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getId();
    }

    private com.fanny.model.Usuario usuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuario();
    }
}
