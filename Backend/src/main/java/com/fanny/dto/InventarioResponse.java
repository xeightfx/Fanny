package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record InventarioResponse(
        Long productoId,
        String producto,
        String marca,
        Integer stock,
        Integer stockMinimo,
        String ubicacion,
        boolean stockBajo) {
}
