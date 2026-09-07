package com.inventario.inventario_backend.controller;

import com.inventario.inventario_backend.dto.activo.ActivoRequest;
import com.inventario.inventario_backend.dto.activo.ActivoResponse;
import com.inventario.inventario_backend.dto.activo.CambioEstadoRequest;
import com.inventario.inventario_backend.enums.EstadoActivo;
import com.inventario.inventario_backend.service.ActivoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Activos", description = "Consulta y administración de activos tecnológicos")
public class ActivoController {

    private final ActivoService activoService;

    public ActivoController(ActivoService activoService) {
        this.activoService = activoService;
    }

    @GetMapping
        @Operation(summary = "Consultar activos", description = "Consulta activos con filtros opcionales, paginación y ordenamiento. Roles: ADMIN y USER.")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<ActivoResponse>> buscar(
            @Parameter(description = "Coincidencia parcial por número de serie")
            @RequestParam(required = false) String numeroSerie,
            @Parameter(description = "Coincidencia parcial por marca o modelo")
            @RequestParam(required = false) String marcaModelo,
            @Parameter(description = "ID exacto de la categoría")
            @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "Estado exacto del activo")
            @RequestParam(required = false) EstadoActivo estado,
            @Parameter(description = "Costo de adquisición mínimo")
            @RequestParam(required = false) BigDecimal costoMin,
            @Parameter(description = "Costo de adquisición máximo")
            @RequestParam(required = false) BigDecimal costoMax,
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número de página, comenzando en 0")
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Cantidad de elementos por página")
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Ordenamiento, por ejemplo fechaIngreso,desc")
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
    @Operation(summary = "Consultar activo por ID", description = "Obtiene un activo por su identificador técnico UUID. Roles: ADMIN y USER.")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ActivoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(activoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registrar activo", description = "Registra un activo tecnológico. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActivoResponse> registrar(@Valid @RequestBody ActivoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activoService.registrar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar activo", description = "Actualiza los datos permitidos de un activo. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActivoResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActivoRequest request) {

        return ResponseEntity.ok(activoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de activo", description = "Cambia el estado de un activo. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActivoResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambioEstadoRequest request) {

        return ResponseEntity.ok(activoService.cambiarEstado(id, request.getEstado()));
    }
}