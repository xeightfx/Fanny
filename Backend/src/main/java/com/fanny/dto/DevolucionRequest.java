package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record DevolucionRequest(
        @NotNull Long pedidoId,
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad,
        @NotBlank @Size(max = 255) String motivo) {
}
