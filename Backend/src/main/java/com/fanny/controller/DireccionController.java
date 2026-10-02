package com.fanny.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.DireccionRequest;
import com.fanny.dto.DireccionResponse;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.DireccionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cliente/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping
    public List<DireccionResponse> listar(Authentication auth) {
        return direccionService.misDirecciones(id(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DireccionResponse crear(Authentication auth, @Valid @RequestBody DireccionRequest req) {
        return direccionService.crear(usuario(auth), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(Authentication auth, @PathVariable Long id) {
        direccionService.eliminar(usuario(auth), id);
    }

    private Long id(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getId();
    }

    private com.fanny.model.Usuario usuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuario();
    }
}
