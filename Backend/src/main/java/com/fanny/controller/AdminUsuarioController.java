package com.fanny.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.ActivoRequest;
import com.fanny.dto.RolesRequest;
import com.fanny.dto.UsuarioAdminRequest;
import com.fanny.dto.UsuarioResponse;
import com.fanny.service.UsuarioService;

import jakarta.validation.Valid;

/** Gestion de usuarios y roles, solo para ADMIN (seccion 1 y 19). */
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioAdminRequest req) {
        return usuarioService.crearUsuarioAdmin(req);
    }

    @PutMapping("/{id}/roles")
    public UsuarioResponse actualizarRoles(@PathVariable Long id, @Valid @RequestBody RolesRequest req) {
        return usuarioService.actualizarRoles(id, req);
    }

    @PutMapping("/{id}/activo")
    public UsuarioResponse actualizarActivo(@PathVariable Long id, @Valid @RequestBody ActivoRequest req) {
        return usuarioService.actualizarActivo(id, req);
    }
}
