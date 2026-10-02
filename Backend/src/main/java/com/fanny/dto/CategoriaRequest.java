package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record CategoriaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 255) String descripcion) {
}
