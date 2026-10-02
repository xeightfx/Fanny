package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record PedidoResponse(
        Long id,
        String numeroPedido,
        Long clienteId,
        String cliente,
        String direccion,
        String metodoEntrega,
        String estado,
        BigDecimal total,
        OffsetDateTime fechaPedido,
        List<DetallePedidoResponse> detalles) {
}
