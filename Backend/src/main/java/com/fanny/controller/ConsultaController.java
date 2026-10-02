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

import com.fanny.dto.ConsultaRequest;
import com.fanny.dto.ConsultaResponse;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.ConsultaService;

import jakarta.validation.Valid;

/** Cualquiera puede leer las preguntas de un producto; solo un cliente logueado puede preguntar. */
@RestController
@RequestMapping("/api/productos/{productoId}/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public List<ConsultaResponse> listar(@PathVariable Long productoId) {
        return consultaService.deProducto(productoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaResponse crear(Authentication auth, @PathVariable Long productoId,
            @Valid @RequestBody ConsultaRequest req) {
        // productoId de la URL manda; se ignora si el body trae otro distinto
        ConsultaRequest normalizado = new ConsultaRequest(productoId, req.mensaje());
        return consultaService.crear(usuario(auth), normalizado);
    }

    private com.fanny.model.Usuario usuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuario();
    }
}
