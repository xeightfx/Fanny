package com.fanny.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fanny.dto.DashboardResponse;
import com.fanny.dto.ReporteItem;
import com.fanny.service.ReporteService;

/** Reportes del administrador (seccion 19): ventas, productos mas vendidos, estados, etc. */
@RestController
@RequestMapping("/api/admin/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return reporteService.dashboardAdmin();
    }

    @GetMapping("/ventas-por-fecha")
    public List<ReporteItem> ventasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta) {
        return reporteService.ventasPorFecha(desde, hasta);
    }

    @GetMapping("/ventas-por-vendedor")
    public List<ReporteItem> ventasPorVendedor() {
        return reporteService.ventasPorVendedor();
    }

    @GetMapping("/productos-mas-vendidos")
    public List<ReporteItem> productosMasVendidos(@RequestParam(defaultValue = "10") int top) {
        return reporteService.productosMasVendidos(top);
    }

    @GetMapping("/pedidos-por-estado")
    public List<ReporteItem> pedidosPorEstado() {
        return reporteService.pedidosPorEstado();
    }

    @GetMapping("/devoluciones")
    public List<ReporteItem> devoluciones() {
        return reporteService.devolucionesPorEstado();
    }
}
