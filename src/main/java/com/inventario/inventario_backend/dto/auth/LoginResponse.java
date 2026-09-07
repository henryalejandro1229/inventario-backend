package com.inventario.inventario_backend.dto.auth;

import com.inventario.inventario_backend.enums.Rol;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private Rol rol;
}