package com.inventario.inventario_backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventario.inventario_backend.entity.Categoria;
import com.inventario.inventario_backend.entity.Usuario;
import com.inventario.inventario_backend.enums.Rol;
import com.inventario.inventario_backend.repository.ActivoRepository;
import com.inventario.inventario_backend.repository.CategoriaRepository;
import com.inventario.inventario_backend.repository.UsuarioRepository;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InventarioApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ActivoRepository activoRepository;

    private Categoria categoria;

    @BeforeEach
    void prepararDatos() {
        activoRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();
        categoriaRepository.deleteAllInBatch();

        categoria = new Categoria();
        categoria.setNombre("Laptop de integración");
        categoria.setCodigoPrefijo("TST");
        categoria = categoriaRepository.saveAndFlush(categoria);

        guardarUsuario("admin-test", "admin-password", Rol.ADMIN);
        guardarUsuario("user-test", "user-password", Rol.USER);
    }

    @Test
    void loginCorrectoDevuelveJwt() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin-test", "admin-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin-test"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void loginConUsuarioInexistenteDevuelve401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("missing", "password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginConPasswordIncorrectoDevuelve401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin-test", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void endpointsProtegidosSinJwtDevuelven401() throws Exception {
        mockMvc.perform(get("/api/activos"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/reportes/activos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioPuedeConsultarCategorias() throws Exception {
        mockMvc.perform(get("/api/categorias").header("Authorization", bearer("user-test", "user-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoria.getId().intValue()))
                .andExpect(jsonPath("$[0].nombre").value("Laptop de integración"))
                .andExpect(jsonPath("$[0].codigoPrefijo").value("TST"));
    }

    @Test
    void usuarioNoPuedeRegistrarActualizarNiCambiarEstado() throws Exception {
        String token = bearer("user-test", "user-password");
        String request = activoJson("USER-SERIE", "Monitor", "DISPONIBLE", "100.00");

        mockMvc.perform(post("/api/activos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden());

        String id = "00000000-0000-0000-0000-000000000001";
        mockMvc.perform(put("/api/activos/{id}", id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/activos/{id}/estado", id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"BAJA\"}"))
                .andExpect(status().isForbidden());
    }

            @Test
            void adminPuedeRegistrarActivo() throws Exception {
            mockMvc.perform(post("/api/activos")
                    .header("Authorization", bearer("admin-test", "admin-password"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activoJson("ADMIN-SERIE", "Laptop", "DISPONIBLE", "1200.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificadorTecnico").isNotEmpty())
                .andExpect(jsonPath("$.folioInventario").value(org.hamcrest.Matchers.matchesRegex("TST-\\d{4}-001")))
                .andExpect(jsonPath("$.fechaIngreso").isNotEmpty())
                .andExpect(jsonPath("$.categoriaId").value(categoria.getId().intValue()))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
            }

            @Test
            void adminPuedeConsultarActivosConPaginacionFiltrosYOrdenamiento() throws Exception {
            mockMvc.perform(post("/api/activos")
                    .header("Authorization", bearer("admin-test", "admin-password"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activoJson("FILTER-SERIE", "Laptop", "DISPONIBLE", "1200.00")))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/api/activos")
                    .header("Authorization", bearer("user-test", "user-password"))
                    .param("numeroSerie", "FILTER")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "fechaIngreso,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
            }

            @Test
            void adminPuedeActualizarActivoYConservarIdentidadFolioYFecha() throws Exception {
            String createResponse = mockMvc.perform(post("/api/activos")
                    .header("Authorization", bearer("admin-test", "admin-password"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activoJson("UPDATE-SERIE", "Laptop", "DISPONIBLE", "1200.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
            JsonNode created = objectMapper.readTree(createResponse);

            mockMvc.perform(put("/api/activos/{id}", created.get("identificadorTecnico").asText())
                    .header("Authorization", bearer("admin-test", "admin-password"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activoJson("UPDATE-SERIE", "Laptop actualizada", "DISPONIBLE", "1300.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificadorTecnico").value(created.get("identificadorTecnico").asText()))
                .andExpect(jsonPath("$.folioInventario").value(created.get("folioInventario").asText()))
                .andExpect(jsonPath("$.fechaIngreso").value(created.get("fechaIngreso").asText()));
            }

            @Test
            void adminPuedeCambiarEstadoYBajaNoPuedeRegresar() throws Exception {
            String createResponse = mockMvc.perform(post("/api/activos")
                    .header("Authorization", bearer("admin-test", "admin-password"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activoJson("STATE-SERIE", "Laptop", "DISPONIBLE", "1200.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
            String id = objectMapper.readTree(createResponse).get("identificadorTecnico").asText();

            mockMvc.perform(patch("/api/activos/{id}/estado", id)
                    .header("Authorization", bearer("admin-test", "admin-password"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"estado\":\"BAJA\"}"))
                .andExpect(status().isOk());

            mockMvc.perform(patch("/api/activos/{id}/estado", id)
                    .header("Authorization", bearer("admin-test", "admin-password"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"estado\":\"DISPONIBLE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
            }

            @Test
            void activoInexistenteDevuelve404() throws Exception {
            mockMvc.perform(get("/api/activos/{id}", "00000000-0000-0000-0000-000000000001")
                    .header("Authorization", bearer("user-test", "user-password")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
            }

            @Test
            void numeroDeSerieDuplicadoDevuelve409() throws Exception {
            String token = bearer("admin-test", "admin-password");
            String request = activoJson("DUPLICATE-SERIE", "Laptop", "DISPONIBLE", "1200.00");
            mockMvc.perform(post("/api/activos").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());

            mockMvc.perform(post("/api/activos").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
            }

    @Test
    void usuarioPuedeGenerarReporteYZipContieneArchivosRequeridos() throws Exception {
        String response = mockMvc.perform(get("/api/reportes/activos")
                        .header("Authorization", bearer("user-test", "user-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileBase64").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        byte[] zipBytes = Base64.getDecoder().decode(body.get("fileBase64").asText());
        Set<String> entries = new HashSet<>();
        try (ZipInputStream zip = new ZipInputStream(new java.io.ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        org.assertj.core.api.Assertions.assertThat(entries)
                .containsExactlyInAnyOrder("activos.xlsx", "auditoria.txt");
    }

    @Test
    void requestDeActivoInvalidoDevuelve400Uniforme() throws Exception {
        mockMvc.perform(post("/api/activos")
                        .header("Authorization", bearer("admin-test", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroSerie\":\"\",\"costoAdquisicion\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isString());
    }

    private void guardarUsuario(String username, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuarioRepository.saveAndFlush(usuario);
    }

    private String bearer(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return "Bearer " + objectMapper.readTree(response).get("token").asText();
    }

    private String loginJson(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String activoJson(String numeroSerie, String marcaModelo, String estado, String costo) {
        return "{\"numeroSerie\":\"" + numeroSerie + "\","
                + "\"marcaModelo\":\"" + marcaModelo + "\","
                + "\"estado\":\"" + estado + "\","
                + "\"costoAdquisicion\":" + costo + ","
                + "\"categoriaId\":" + categoria.getId() + "}";
    }
}