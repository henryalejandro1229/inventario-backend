package com.inventario.inventario_backend.service;

import com.inventario.inventario_backend.dto.reporte.ReporteResponse;
import com.inventario.inventario_backend.entity.Activo;
import com.inventario.inventario_backend.repository.ActivoRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter AUDIT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ActivoRepository activoRepository;

    public ReporteService(ActivoRepository activoRepository) {
        this.activoRepository = activoRepository;
    }

    @Transactional(readOnly = true)
    public ReporteResponse generarReporteActivos() {
        List<Activo> activos = activoRepository.findAll();
        LocalDateTime fechaGeneracion = LocalDateTime.now();
        String fileName = "reporte-activos-" + fechaGeneracion.format(FILE_DATE_FORMAT) + ".zip";
        String username = usuarioAutenticado();

        try (ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(zipOutput)) {
            zip.putNextEntry(new ZipEntry("activos.xlsx"));
            zip.write(generarExcel(activos));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("auditoria.txt"));
            zip.write(generarAuditoria(fechaGeneracion, username, activos.size()));
            zip.closeEntry();
            zip.finish();

            ReporteResponse response = new ReporteResponse();
            response.setStatus(200);
            response.setMessage("Reporte generado correctamente");
            response.setFileName(fileName);
            response.setFileBase64(Base64.getEncoder().encodeToString(zipOutput.toByteArray()));
            return response;
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el reporte", exception);
        }
    }

    private byte[] generarExcel(List<Activo> activos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream excelOutput = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Activos");
            String[] headers = {
                    "Identificador técnico",
                    "Folio",
                    "Número de serie",
                    "Marca/Modelo",
                    "Estado",
                    "Costo de adquisición",
                    "Fecha de ingreso",
                    "Categoría",
                    "Nombre de categoría"
            };

            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
            }

            for (int index = 0; index < activos.size(); index++) {
                escribirActivo(sheet.createRow(index + 1), activos.get(index));
            }

            for (int index = 0; index < headers.length; index++) {
                sheet.autoSizeColumn(index);
            }

            workbook.write(excelOutput);
            return excelOutput.toByteArray();
        }
    }

    private void escribirActivo(Row row, Activo activo) {
        escribirTexto(row, 0, activo.getIdentificadorTecnico().toString());
        escribirTexto(row, 1, activo.getFolioInventario());
        escribirTexto(row, 2, activo.getNumeroSerie());
        escribirTexto(row, 3, activo.getMarcaModelo());
        escribirTexto(row, 4, activo.getEstado().name());
        row.createCell(5).setCellValue(activo.getCostoAdquisicion().doubleValue());
        escribirTexto(row, 6, activo.getFechaIngreso().toString());
        escribirTexto(row, 7, activo.getCategoria().getId().toString());
        escribirTexto(row, 8, activo.getCategoria().getNombre());
    }

    private void escribirTexto(Row row, int column, String value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
    }

    private byte[] generarAuditoria(LocalDateTime fechaGeneracion, String username, int cantidadActivos) {
        String auditoria = "Reporte de activos\n"
                + "==================\n\n"
                + "Fecha de generación: " + fechaGeneracion.format(AUDIT_DATE_FORMAT) + "\n"
                + "Usuario solicitante: " + username + "\n"
                + "Cantidad de activos: " + cantidadActivos + "\n";
        return auditoria.getBytes(StandardCharsets.UTF_8);
    }

    private String usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "desconocido" : authentication.getName();
    }
}