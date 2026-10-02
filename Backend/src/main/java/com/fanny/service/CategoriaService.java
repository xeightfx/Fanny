package com.fanny.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.CategoriaRequest;
import com.fanny.dto.CategoriaResponse;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.exception.ValidacionNegocioException;
import com.fanny.model.Categoria;
import com.fanny.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByActivaTrueOrderByNombreAsc().stream().map(this::aResponse).toList();
    }

    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream().map(this::aResponse).toList();
    }

    public CategoriaResponse obtener(Long id) {
        return aResponse(buscarONotFound(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        if (categoriaRepository.existsByNombreIgnoreCase(req.nombre())) {
            throw new ValidacionNegocioException("Ya existe una categoria con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(req.nombre());
        categoria.setDescripcion(req.descripcion());
        return aResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria categoria = buscarONotFound(id);
        if (!categoria.getNombre().equalsIgnoreCase(req.nombre())
                && categoriaRepository.existsByNombreIgnoreCase(req.nombre())) {
            throw new ValidacionNegocioException("Ya existe una categoria con ese nombre");
        }
        categoria.setNombre(req.nombre());
        categoria.setDescripcion(req.descripcion());
        return aResponse(categoria);
    }

    @Transactional
    public void desactivar(Long id) {
        Categoria categoria = buscarONotFound(id);
        categoria.setActiva(false);
    }

    @Transactional
    public void activar(Long id) {
        Categoria categoria = buscarONotFound(id);
        categoria.setActiva(true);
    }

    public Categoria buscarONotFound(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la categoria con id " + id));
    }

    private CategoriaResponse aResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getActiva());
    }
}
