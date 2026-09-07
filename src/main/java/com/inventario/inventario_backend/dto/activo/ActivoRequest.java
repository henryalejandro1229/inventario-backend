package com.inventario.inventario_backend.dto.activo;

import com.inventario.inventario_backend.enums.EstadoActivo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivoRequest {

    @NotBlank
    @Size(max = 100)
    private String numeroSerie;

    @NotBlank
    @Size(max = 150)
    private String marcaModelo;

    private EstadoActivo estado;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costoAdquisicion;

    @NotNull
    private Long categoriaId;
}