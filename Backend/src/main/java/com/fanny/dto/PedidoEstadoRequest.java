package com.fanny.dto;

import com.fanny.model.EstadoPedido;

import jakarta.validation.constraints.NotNull;

public record PedidoEstadoRequest(@NotNull EstadoPedido estado) {
}
