package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record DespachoRequest(
        @NotBlank @Size(max = 100) String empresaTransporte,
        @NotBlank @Size(max = 50) String codigoSeguimiento) {
}
