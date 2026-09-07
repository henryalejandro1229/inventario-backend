package com.inventario.inventario_backend.controller;

import com.inventario.inventario_backend.dto.reporte.ReporteResponse;
import com.inventario.inventario_backend.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Generación de reportes de activos")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/activos")
    @Operation(summary = "Generar reporte de activos", description = "Genera un ZIP con el Excel de activos y la auditoría. Roles: ADMIN y USER.")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ReporteResponse> generarReporteActivos() {
        return ResponseEntity.ok(reporteService.generarReporteActivos());
    }
}