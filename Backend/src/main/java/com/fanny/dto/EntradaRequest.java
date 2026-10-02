package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record EntradaRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad,
        @Size(max = 255) String observacion) {
}
