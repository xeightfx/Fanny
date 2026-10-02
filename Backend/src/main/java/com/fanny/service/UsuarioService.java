package com.fanny.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.ActivoRequest;
import com.fanny.dto.RegistroRequest;
import com.fanny.dto.RolesRequest;
import com.fanny.dto.UsuarioAdminRequest;
import com.fanny.dto.UsuarioResponse;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.exception.ValidacionNegocioException;
import com.fanny.model.Carrito;
import com.fanny.model.Rol;
import com.fanny.model.Usuario;
import com.fanny.repository.CarritoRepository;
import com.fanny.repository.RolRepository;
import com.fanny.repository.UsuarioRepository;

/**
 * Reglas de negocio de usuarios: registro publico, gestion de usuarios por el
 * administrador y asignacion de roles.
 *
 * GRASP - Information Expert: esta clase es quien tiene los datos (repositorios)
 * necesarios para crear/validar un usuario, por eso es quien hace el trabajo.
 * GRASP - Creator: UsuarioService es quien crea el Carrito de un cliente nuevo,
 * porque es quien "sabe" cuando nace un usuario.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CarritoRepository carritoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            CarritoRepository carritoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.carritoRepository = carritoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrarCliente(RegistroRequest req) {
        validarEmailDisponible(req.email());

        Usuario usuario = new Usuario();
        usuario.setNombres(req.nombres());
        usuario.setApellidos(req.apellidos());
        usuario.setEmail(req.email().trim().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(req.password()));
        usuario.setTelefono(req.telefono());
        usuario.setRoles(Set.of(buscarRolONotFound("ROLE_CLIENTE")));

        usuario = usuarioRepository.save(usuario);

        // Todo cliente nuevo empieza con su carrito vacio ya creado
        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carritoRepository.save(carrito);

        return aResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crearUsuarioAdmin(UsuarioAdminRequest req) {
        validarEmailDisponible(req.email());

        Usuario usuario = new Usuario();
        usuario.setNombres(req.nombres());
        usuario.setApellidos(req.apellidos());
        usuario.setEmail(req.email().trim().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(req.password()));
        usuario.setTelefono(req.telefono());
        usuario.setRoles(resolverRoles(req.roles()));

        usuario = usuarioRepository.save(usuario);

        if (usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_CLIENTE"))) {
            Carrito carrito = new Carrito();
            carrito.setUsuario(usuario);
            carritoRepository.save(carrito);
        }

        return aResponse(usuario);
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::aResponse).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return aResponse(buscarONotFound(id));
    }

    @Transactional
    public UsuarioResponse actualizarRoles(Long id, RolesRequest req) {
        Usuario usuario = buscarONotFound(id);
        usuario.setRoles(resolverRoles(req.roles()));
        return aResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarActivo(Long id, ActivoRequest req) {
        Usuario usuario = buscarONotFound(id);
        usuario.setActivo(req.activo());
        return aResponse(usuario);
    }

    public Usuario buscarONotFound(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el usuario con id " + id));
    }

    private void validarEmailDisponible(String email) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidacionNegocioException("Ya existe una cuenta registrada con ese email");
        }
    }

    private Rol buscarRolONotFound(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new ValidacionNegocioException("El rol " + nombre + " no existe"));
    }

    private Set<Rol> resolverRoles(Set<String> nombres) {
        Set<Rol> roles = new LinkedHashSet<>();
        for (String nombre : nombres) {
            roles.add(buscarRolONotFound(nombre));
        }
        return roles;
    }

    private UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNombres(), u.getApellidos(), u.getEmail(), u.getTelefono(),
                u.getActivo(), u.getRoles().stream().map(Rol::getNombre).toList());
    }
}
