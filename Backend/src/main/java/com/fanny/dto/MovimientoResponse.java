package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record MovimientoResponse(
        Long id,
        Long productoId,
        String producto,
        String tipo,
        Integer cantidad,
        OffsetDateTime fecha,
        String responsable,
        String pedido,
        String observacion) {
}
