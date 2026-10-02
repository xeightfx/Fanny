package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record ProductoRequest(
        @NotBlank @Size(max = 150) String nombre,
        String descripcion,
        @Size(max = 100) String marca,
        @NotNull @DecimalMin("0.0") BigDecimal precio,
        @DecimalMin("0.0") BigDecimal precioAnterior,
        @NotNull @Min(0) Integer stock,
        @Size(max = 20) String talla,
        @Size(max = 50) String color,
        @NotNull Long categoriaId,
        @Size(max = 500) String imagenUrl) {
}
