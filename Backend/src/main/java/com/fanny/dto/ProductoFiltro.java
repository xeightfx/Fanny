package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record ProductoFiltro(
        String q,
        Long categoriaId,
        String marca,
        BigDecimal precioMin,
        BigDecimal precioMax,
        String talla,
        String color,
        Boolean soloDisponibles,
        Boolean soloOfertas,
        String orden) {
}
