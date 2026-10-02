package com.fanny.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.RegistroRequest;
import com.fanny.dto.UsuarioResponse;
import com.fanny.security.UsuarioPrincipal;
import com.fanny.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Registro de nuevos clientes y consulta de "quien soy" (perfil del usuario logueado).
 * El login en si no es un endpoint: se hace mandando email+password por HTTP Basic
 * en cada peticion (lo revisa Spring Security automaticamente).
 */
@RestController
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/api/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest req) {
        return usuarioService.registrarCliente(req);
    }

    @GetMapping("/api/auth/me")
    public UsuarioResponse yo(Authentication authentication) {
        UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
        return usuarioService.obtener(principal.getId());
    }
}
