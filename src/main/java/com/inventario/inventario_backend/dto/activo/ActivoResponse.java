package com.inventario.inventario_backend.dto.activo;

import com.inventario.inventario_backend.enums.EstadoActivo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivoResponse {

    private UUID identificadorTecnico;
    private String folioInventario;
    private String numeroSerie;
    private String marcaModelo;
    private EstadoActivo estado;
    private BigDecimal costoAdquisicion;
    private LocalDateTime fechaIngreso;
    private Long categoriaId;
    private String categoriaNombre;
}