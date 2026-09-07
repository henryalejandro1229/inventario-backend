package com.inventario.inventario_backend.dto.categoria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoriaResponse {

    private Long id;
    private String nombre;
    private String codigoPrefijo;
}