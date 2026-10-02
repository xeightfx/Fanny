package com.fanny.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fanny.dto.ConsultaRequest;
import com.fanny.dto.ConsultaResponse;
import com.fanny.dto.RespuestaRequest;
import com.fanny.dto.RespuestaResponse;
import com.fanny.exception.AccesoNoAutorizadoException;
import com.fanny.exception.RecursoNoEncontradoException;
import com.fanny.model.Consulta;
import com.fanny.model.Producto;
import com.fanny.model.RespuestaConsulta;
import com.fanny.model.Usuario;
import com.fanny.repository.ConsultaRepository;
import com.fanny.repository.ProductoRepository;
import com.fanny.repository.RespuestaConsultaRepository;

/**
 * Preguntas de clientes sobre un producto y respuestas del vendedor dueño de ese producto.
 */
@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final RespuestaConsultaRepository respuestaConsultaRepository;
    private final ProductoRepository productoRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
            RespuestaConsultaRepository respuestaConsultaRepository, ProductoRepository productoRepository) {
        this.consultaRepository = consultaRepository;
        this.respuestaConsultaRepository = respuestaConsultaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public ConsultaResponse crear(Usuario cliente, ConsultaRequest req) {
        Producto producto = productoRepository.findById(req.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el producto"));

        Consulta consulta = new Consulta();
        consulta.setProducto(producto);
        consulta.setCliente(cliente);
        consulta.setMensaje(req.mensaje());
        return aResponse(consultaRepository.save(consulta));
    }

    public List<ConsultaResponse> deProducto(Long productoId) {
        return consultaRepository.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(this::aResponse).toList();
    }

    public List<ConsultaResponse> deVendedor(Long vendedorId) {
        return consultaRepository.deVendedor(vendedorId).stream().map(this::aResponse).toList();
    }

    @Transactional
    public RespuestaResponse responder(Usuario vendedor, Long consultaId, RespuestaRequest req) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la consulta"));

        boolean esAdmin = vendedor.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));
        if (!esAdmin && !consulta.getProducto().getVendedor().getId().equals(vendedor.getId())) {
            throw new AccesoNoAutorizadoException("Esta consulta es de un producto que no es tuyo");
        }

        RespuestaConsulta respuesta = new RespuestaConsulta();
        respuesta.setConsulta(consulta);
        respuesta.setVendedor(vendedor);
        respuesta.setRespuesta(req.respuesta());
        respuesta = respuestaConsultaRepository.save(respuesta);
        return respuestaAResponse(respuesta);
    }

    private ConsultaResponse aResponse(Consulta c) {
        List<RespuestaResponse> respuestas = c.getRespuestas().stream().map(this::respuestaAResponse).toList();
        return new ConsultaResponse(c.getId(), c.getProducto().getId(), c.getProducto().getNombre(),
                c.getCliente().getNombres() + " " + c.getCliente().getApellidos(), c.getMensaje(), c.getFecha(),
                respuestas);
    }

    private RespuestaResponse respuestaAResponse(RespuestaConsulta r) {
        return new RespuestaResponse(r.getId(), r.getVendedor().getNombres() + " " + r.getVendedor().getApellidos(),
                r.getRespuesta(), r.getFecha());
    }
}
