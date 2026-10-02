package com.fanny.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fanny.model.*;

import jakarta.validation.constraints.*;

public record SeguimientoResponse(
        String numeroPedido,
        String estadoActual,
        List<PasoSeguimiento> pasos,
        EnvioResponse envio) {
}
