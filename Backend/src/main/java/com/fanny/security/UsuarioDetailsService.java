package com.fanny.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fanny.model.Usuario;
import com.fanny.repository.UsuarioRepository;

/**
 * Le dice a Spring Security como buscar un usuario cuando alguien intenta iniciar sesion.
 * El "username" en este proyecto es el email.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el email: " + email));
        return new UsuarioPrincipal(usuario);
    }
}
