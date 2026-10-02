package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        String marca,
        BigDecimal precio,
        BigDecimal precioAnterior,
        BigDecimal descuento,
        Integer stock,
        String talla,
        String color,
        String estado,
        Long categoriaId,
        String categoria,
        Long vendedorId,
        String vendedor,
        String imagenUrl,
        List<String> imagenes) {
}
