package com.inventario.inventario_backend.config;

import com.inventario.inventario_backend.entity.Usuario;
import com.inventario.inventario_backend.enums.Rol;
import com.inventario.inventario_backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner inicializarUsuarios(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, "admin", "admin123", Rol.ADMIN);
            crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, "user", "user123", Rol.USER);
        };
    }

    private void crearUsuarioSiNoExiste(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            Rol rol) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }
}
