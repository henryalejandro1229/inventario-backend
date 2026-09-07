package com.inventario.inventario_backend.dto.reporte;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReporteResponse {

    private Integer status;
    private String message;
    private String fileName;
    private String fileBase64;
}