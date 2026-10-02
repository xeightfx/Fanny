package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record DevolucionResponse(
        Long id,
        Long pedidoId,
        String numeroPedido,
        Long productoId,
        String producto,
        Long clienteId,
        String cliente,
        Integer cantidad,
        String motivo,
        String estado,
        OffsetDateTime fechaSolicitud,
        OffsetDateTime fechaResolucion) {
}
