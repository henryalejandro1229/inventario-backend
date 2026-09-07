package com.inventario.inventario_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.inventario.inventario_backend.dto.reporte.ReporteResponse;
import com.inventario.inventario_backend.entity.Activo;
import com.inventario.inventario_backend.entity.Categoria;
import com.inventario.inventario_backend.enums.EstadoActivo;
import com.inventario.inventario_backend.repository.ActivoRepository;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ActivoRepository activoRepository;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(activoRepository);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, List.of())
        );
    }

    @Test
    void debeGenerarZipConExcelYAuditoria() throws Exception {
        when(activoRepository.findAll()).thenReturn(List.of(activo()));

        ReporteResponse response = reporteService.generarReporteActivos();
        byte[] zipBytes = Base64.getDecoder().decode(response.getFileBase64());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Reporte generado correctamente");
        assertThat(response.getFileName()).matches("reporte-activos-\\d{4}-\\d{2}-\\d{2}\\.zip");

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry excelEntry = zip.getNextEntry();
            assertThat(excelEntry.getName()).isEqualTo("activos.xlsx");
            byte[] excelBytes = zip.readAllBytes();
            assertThat(excelBytes).isNotEmpty();
            zip.closeEntry();

            ZipEntry auditEntry = zip.getNextEntry();
            assertThat(auditEntry.getName()).isEqualTo("auditoria.txt");
            String audit = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(audit).contains("Fecha de generación:");
            assertThat(audit).contains("Usuario solicitante: admin");
            assertThat(audit).contains("Cantidad de activos: 1");
        }
    }

    @Test
    void debeGenerarExcelAbribleConHojaYDatos() throws Exception {
        when(activoRepository.findAll()).thenReturn(List.of(activo()));

        ReporteResponse response = reporteService.generarReporteActivos();
        byte[] zipBytes = Base64.getDecoder().decode(response.getFileBase64());

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            zip.getNextEntry();
            byte[] excelBytes = zip.readAllBytes();
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
                assertThat(workbook.getSheet("Activos")).isNotNull();
                assertThat(workbook.getSheet("Activos").getRow(0).getCell(0).getStringCellValue())
                        .isEqualTo("Identificador técnico");
                assertThat(workbook.getSheet("Activos").getRow(1).getCell(2).getStringCellValue())
                        .isEqualTo("SERIE-001");
            }
        }
    }

    private Activo activo() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Laptop");
        categoria.setCodigoPrefijo("LAP");

        Activo activo = new Activo();
        activo.setIdentificadorTecnico(UUID.randomUUID());
        activo.setFolioInventario("LAP-2026-001");
        activo.setNumeroSerie("SERIE-001");
        activo.setMarcaModelo("Dell Latitude");
        activo.setEstado(EstadoActivo.DISPONIBLE);
        activo.setCostoAdquisicion(new BigDecimal("15000.00"));
        activo.setFechaIngreso(LocalDateTime.of(2026, 1, 1, 10, 0));
        activo.setCategoria(categoria);
        return activo;
    }
}