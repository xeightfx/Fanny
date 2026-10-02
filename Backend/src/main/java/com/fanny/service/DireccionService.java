package com.fanny.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.DireccionRequest;
import com.fanny.dto.DireccionResponse;
import com.fanny.exception.AccesoNoAutorizadoException;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.model.Direccion;
import com.fanny.model.Usuario;
import com.fanny.repository.DireccionRepository;

@Service
public class DireccionService {

    private final DireccionRepository direccionRepository;

    public DireccionService(DireccionRepository direccionRepository) {
        this.direccionRepository = direccionRepository;
    }

    public List<DireccionResponse> misDirecciones(Long usuarioId) {
        return direccionRepository.findByUsuarioIdOrderByPrincipalDescIdAsc(usuarioId)
                .stream().map(this::aResponse).toList();
    }

    @Transactional
    public DireccionResponse crear(Usuario usuario, DireccionRequest req) {
        Direccion direccion = new Direccion();
        direccion.setUsuario(usuario);
        direccion.setDireccion(req.direccion());
        direccion.setDistrito(req.distrito());
        direccion.setCiudad(req.ciudad());
        direccion.setReferencia(req.referencia());
        direccion.setPrincipal(Boolean.TRUE.equals(req.principal()));

        if (direccion.getPrincipal()) {
            quitarPrincipalAnterior(usuario.getId());
        }
        return aResponse(direccionRepository.save(direccion));
    }

    @Transactional
    public void eliminar(Usuario usuario, Long direccionId) {
        Direccion direccion = buscarDelUsuario(usuario.getId(), direccionId);
        direccionRepository.delete(direccion);
    }

    public Direccion buscarDelUsuario(Long usuarioId, Long direccionId) {
        Direccion direccion = direccionRepository.findById(direccionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la direccion"));
        if (!direccion.getUsuario().getId().equals(usuarioId)) {
            throw new AccesoNoAutorizadoException("Esta direccion no te pertenece");
        }
        return direccion;
    }

    private void quitarPrincipalAnterior(Long usuarioId) {
        direccionRepository.findByUsuarioIdOrderByPrincipalDescIdAsc(usuarioId).stream()
                .filter(Direccion::getPrincipal)
                .forEach(d -> d.setPrincipal(false));
    }

    private DireccionResponse aResponse(Direccion d) {
        return new DireccionResponse(d.getId(), d.getDireccion(), d.getDistrito(), d.getCiudad(),
                d.getReferencia(), d.getPrincipal());
    }
}
