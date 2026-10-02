package com.fanny.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.DespachoRequest;
import com.fanny.dto.EnvioEstadoRequest;
import com.fanny.dto.EnvioResponse;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.model.Envio;
import com.fanny.model.EstadoEnvio;
import com.fanny.model.EstadoPedido;
import com.fanny.model.Pedido;
import com.fanny.model.Usuario;
import com.fanny.repository.EnvioRepository;
import com.fanny.repository.PedidoRepository;

/**
 * Reglas de negocio de envios y despachos del almacen.
 */
@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final PedidoRepository pedidoRepository;

    public EnvioService(EnvioRepository envioRepository, PedidoRepository pedidoRepository) {
        this.envioRepository = envioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<EnvioResponse> listar() {
        return envioRepository.findAllByOrderByIdDesc().stream().map(this::aResponse).toList();
    }

    @Transactional
    public EnvioResponse registrarDespacho(Usuario responsable, Long pedidoId, DespachoRequest req) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el pedido"));

        Envio envio = envioRepository.findByPedidoId(pedidoId).orElseGet(() -> {
            Envio nuevo = new Envio();
            nuevo.setPedido(pedido);
            return nuevo;
        });
        envio.setEmpresaTransporte(req.empresaTransporte());
        envio.setCodigoSeguimiento(req.codigoSeguimiento());
        envio.setResponsable(responsable);
        envio.setEstado(EstadoEnvio.DESPACHADO);
        envio.setFechaDespacho(OffsetDateTime.now());
        envio = envioRepository.save(envio);

        pedido.setEstado(EstadoPedido.DESPACHADO);
        return aResponse(envio);
    }

    @Transactional
    public EnvioResponse actualizarEstado(Long pedidoId, EnvioEstadoRequest req) {
        Envio envio = envioRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Este pedido aun no tiene envio registrado"));

        envio.setEstado(req.estado());
        if (req.estado() == EstadoEnvio.ENTREGADO) {
            envio.setFechaEntrega(OffsetDateTime.now());
        }

        // El estado del envio mueve tambien el estado general del pedido
        Pedido pedido = envio.getPedido();
        pedido.setEstado(switch (req.estado()) {
            case PREPARADO -> EstadoPedido.PREPARADO;
            case DESPACHADO -> EstadoPedido.DESPACHADO;
            case EN_TRANSITO -> EstadoPedido.EN_TRANSITO;
            case ENTREGADO -> EstadoPedido.ENTREGADO;
        });

        return aResponse(envio);
    }

    private EnvioResponse aResponse(Envio e) {
        return new EnvioResponse(e.getId(), e.getPedido().getId(), e.getPedido().getNumeroPedido(),
                e.getEstado().name(), e.getEmpresaTransporte(), e.getCodigoSeguimiento(),
                e.getResponsable() != null ? e.getResponsable().getNombres() + " " + e.getResponsable().getApellidos() : null,
                e.getFechaDespacho(), e.getFechaEntrega());
    }
}
