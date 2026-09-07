package com.inventario.inventario_backend.controller;

import com.inventario.inventario_backend.dto.activo.ActivoRequest;
import com.inventario.inventario_backend.dto.activo.ActivoResponse;
import com.inventario.inventario_backend.dto.activo.CambioEstadoRequest;
import com.inventario.inventario_backend.enums.EstadoActivo;
import com.inventario.inventario_backend.service.ActivoService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activos")
public class ActivoController {

    private final ActivoService activoService;

    public ActivoController(ActivoService activoService) {
        this.activoService = activoService;
    }

    @GetMapping
    public ResponseEntity<Page<ActivoResponse>> buscar(
            @RequestParam(required = false) String numeroSerie,
            @RequestParam(required = false) String marcaModelo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoActivo estado,
            @RequestParam(required = false) BigDecimal costoMin,
            @RequestParam(required = false) BigDecimal costoMax,
            Pageable pageable) {

        return ResponseEntity.ok(activoService.buscar(
                numeroSerie,
                marcaModelo,
                categoriaId,
                estado,
                costoMin,
                costoMax,
                pageable
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(activoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ActivoResponse> registrar(@Valid @RequestBody ActivoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activoService.registrar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivoResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActivoRequest request) {

        return ResponseEntity.ok(activoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ActivoResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambioEstadoRequest request) {

        return ResponseEntity.ok(activoService.cambiarEstado(id, request.getEstado()));
    }
}