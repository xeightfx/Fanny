package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record AjusteRequest(
        @NotNull Long productoId,
        @NotNull @Min(0) Integer nuevoStock,
        @Size(max = 255) String observacion) {
}
