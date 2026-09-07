package com.inventario.inventario_backend.dto.activo;

import com.inventario.inventario_backend.enums.EstadoActivo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CambioEstadoRequest {

    @NotNull
    private EstadoActivo estado;
}