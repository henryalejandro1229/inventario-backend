package com.inventario.inventario_backend.service;

import com.inventario.inventario_backend.dto.auth.LoginRequest;
import com.inventario.inventario_backend.dto.auth.LoginResponse;
import com.inventario.inventario_backend.entity.Usuario;
import com.inventario.inventario_backend.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generarToken(usuario.getUsername(), usuario.getRol().name()));
        response.setUsername(usuario.getUsername());
        response.setRol(usuario.getRol());
        return response;
    }
}