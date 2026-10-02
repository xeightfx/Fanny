package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record DireccionRequest(
        @NotBlank @Size(max = 200) String direccion,
        @NotBlank @Size(max = 100) String distrito,
        @NotBlank @Size(max = 100) String ciudad,
        @Size(max = 200) String referencia,
        Boolean principal) {
}
