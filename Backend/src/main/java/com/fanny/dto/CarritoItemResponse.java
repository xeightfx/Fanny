package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record CarritoItemResponse(
        Long productoId,
        String nombre,
        String marca,
        String talla,
        String color,
        String imagenUrl,
        BigDecimal precio,
        Integer cantidad,
        BigDecimal subtotal,
        Integer stockDisponible) {
}
