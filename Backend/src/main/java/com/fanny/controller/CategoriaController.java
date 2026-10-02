package com.fanny.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.CategoriaRequest;
import com.fanny.dto.CategoriaResponse;
import com.fanny.service.CategoriaService;

import jakarta.validation.Valid;

/**
 * GET es publico (cualquiera puede ver las categorias del catalogo).
 * POST/PUT/DELETE exigen rol ADMIN o VENDEDOR (regla en SecurityConfig).
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listarActivas();
    }

    @GetMapping("/todas")
    public List<CategoriaResponse> listarTodas() {
        return categoriaService.listarTodas();
    }

    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return categoriaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest req) {
        return categoriaService.crear(req);
    }

    @PutMapping("/{id}")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        categoriaService.desactivar(id);
    }

    @PostMapping("/{id}/activar")
    public void activar(@PathVariable Long id) {
        categoriaService.activar(id);
    }
}
