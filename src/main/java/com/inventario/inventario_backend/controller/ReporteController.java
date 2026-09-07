package com.inventario.inventario_backend.controller;

import com.inventario.inventario_backend.dto.reporte.ReporteResponse;
import com.inventario.inventario_backend.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ReporteResponse> generarReporteActivos() {
        return ResponseEntity.ok(reporteService.generarReporteActivos());
    }
}