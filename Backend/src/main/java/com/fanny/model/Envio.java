package com.fanny.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "envios")
@Getter @Setter @NoArgsConstructor
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvio estado = EstadoEnvio.PREPARADO;

    @Column(name = "empresa_transporte", length = 100)
    private String empresaTransporte;

    @Column(name = "codigo_seguimiento", length = 50)
    private String codigoSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(name = "fecha_despacho")
    private OffsetDateTime fechaDespacho;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;
}
