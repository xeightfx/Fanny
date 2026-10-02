package com.fanny.security;

import org.springframework.security.core.context.SecurityContextHolder;

import com.fanny.model.Usuario;

/** Metodo de ayuda para obtener, desde cualquier Service o Controller, el usuario que hizo la peticion. */
public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Usuario usuarioActual() {
        UsuarioPrincipal principal = (UsuarioPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return principal.getUsuario();
    }

    public static Long idUsuarioActual() {
        return usuarioActual().getId();
    }
}
