package com.inventario.inventario_backend.service;

import com.inventario.inventario_backend.dto.activo.ActivoRequest;
import com.inventario.inventario_backend.dto.activo.ActivoResponse;
import com.inventario.inventario_backend.entity.Activo;
import com.inventario.inventario_backend.entity.Categoria;
import com.inventario.inventario_backend.enums.EstadoActivo;
import com.inventario.inventario_backend.exception.NumeroSerieDuplicadoException;
import com.inventario.inventario_backend.exception.RecursoNoEncontradoException;
import com.inventario.inventario_backend.exception.TransicionEstadoInvalidaException;
import com.inventario.inventario_backend.repository.ActivoRepository;
import com.inventario.inventario_backend.repository.CategoriaRepository;
import com.inventario.inventario_backend.specification.ActivoSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivoService {

    private final ActivoRepository activoRepository;
    private final CategoriaRepository categoriaRepository;

    public ActivoService(ActivoRepository activoRepository, CategoriaRepository categoriaRepository) {
        this.activoRepository = activoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public ActivoResponse registrar(ActivoRequest request) {
        Categoria categoria = categoriaRepository.findByIdForUpdate(request.getCategoriaId())
                .orElseThrow(() -> categoriaNoEncontrada(request.getCategoriaId()));
        validarNumeroSerieDisponible(request.getNumeroSerie(), null);

        LocalDateTime fechaIngreso = LocalDateTime.now();
        Activo activo = new Activo();
        activo.setIdentificadorTecnico(UUID.randomUUID());
        activo.setFolioInventario(generarFolio(categoria, fechaIngreso));
        activo.setNumeroSerie(request.getNumeroSerie());
        activo.setMarcaModelo(request.getMarcaModelo());
        activo.setEstado(request.getEstado() == null ? EstadoActivo.DISPONIBLE : request.getEstado());
        activo.setCostoAdquisicion(request.getCostoAdquisicion());
        activo.setFechaIngreso(fechaIngreso);
        activo.setCategoria(categoria);

        return toResponse(activoRepository.save(activo));
    }

    @Transactional(readOnly = true)
    public Page<ActivoResponse> buscar(
            String numeroSerie,
            String marcaModelo,
            Long categoriaId,
            EstadoActivo estado,
            BigDecimal costoMin,
            BigDecimal costoMax,
            Pageable pageable) {

        return activoRepository.findAll(
                        ActivoSpecification.conFiltros(
                                numeroSerie,
                                marcaModelo,
                                categoriaId,
                                estado,
                                costoMin,
                                costoMax
                        ),
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ActivoResponse obtenerPorId(UUID identificadorTecnico) {
        return toResponse(buscarActivo(identificadorTecnico));
    }

    @Transactional
    public ActivoResponse actualizar(UUID identificadorTecnico, ActivoRequest request) {
        Activo activo = buscarActivo(identificadorTecnico);
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> categoriaNoEncontrada(request.getCategoriaId()));
        validarNumeroSerieDisponible(request.getNumeroSerie(), identificadorTecnico);

        activo.setNumeroSerie(request.getNumeroSerie());
        activo.setMarcaModelo(request.getMarcaModelo());
        if (request.getEstado() != null) {
            validarTransicion(activo.getEstado(), request.getEstado());
            activo.setEstado(request.getEstado());
        }
        activo.setCostoAdquisicion(request.getCostoAdquisicion());
        activo.setCategoria(categoria);

        return toResponse(activoRepository.save(activo));
    }

    @Transactional
    public ActivoResponse cambiarEstado(UUID identificadorTecnico, EstadoActivo nuevoEstado) {
        Activo activo = buscarActivo(identificadorTecnico);
        validarTransicion(activo.getEstado(), nuevoEstado);
        activo.setEstado(nuevoEstado);

        return toResponse(activoRepository.save(activo));
    }

    private Activo buscarActivo(UUID identificadorTecnico) {
        return activoRepository.findById(identificadorTecnico)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un activo con identificador técnico: " + identificadorTecnico
                ));
    }

    private void validarNumeroSerieDisponible(String numeroSerie, UUID identificadorTecnicoActual) {
        activoRepository.findByNumeroSerie(numeroSerie)
                .filter(activo -> !activo.getIdentificadorTecnico().equals(identificadorTecnicoActual))
                .ifPresent(activo -> {
                    throw new NumeroSerieDuplicadoException(numeroSerie);
                });
    }

    private void validarTransicion(EstadoActivo estadoActual, EstadoActivo estadoNuevo) {
        if (estadoActual == EstadoActivo.BAJA && estadoNuevo != EstadoActivo.BAJA) {
            throw new TransicionEstadoInvalidaException(estadoActual, estadoNuevo);
        }
    }

    private String generarFolio(Categoria categoria, LocalDateTime fechaIngreso) {
        int anio = fechaIngreso.getYear();
        LocalDateTime inicioAnio = LocalDateTime.of(LocalDate.of(anio, 1, 1), LocalTime.MIN);
        LocalDateTime inicioSiguienteAnio = inicioAnio.plusYears(1);
        List<Activo> activosDelAnio = activoRepository
            .findFoliosPorCategoriaYAnio(
                        categoria.getId(),
                        inicioAnio,
                        inicioSiguienteAnio
                );

        int siguienteConsecutivo = activosDelAnio.stream()
                .map(Activo::getFolioInventario)
                .mapToInt(folio -> extraerConsecutivo(folio, categoria.getCodigoPrefijo(), anio))
                .max()
                .orElse(0) + 1;

        return String.format(
                Locale.ROOT,
                "%s-%d-%03d",
                categoria.getCodigoPrefijo(),
                anio,
                siguienteConsecutivo
        );
    }

    private int extraerConsecutivo(String folio, String prefijo, int anio) {
        if (folio == null) {
            return 0;
        }

        String[] partes = folio.split("-");
        if (partes.length != 3 || !partes[0].equals(prefijo) || !partes[1].equals(String.valueOf(anio))) {
            return 0;
        }

        try {
            return Integer.parseInt(partes[2]);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private RecursoNoEncontradoException categoriaNoEncontrada(Long categoriaId) {
        return new RecursoNoEncontradoException("No existe una categoría con ID: " + categoriaId);
    }

    private ActivoResponse toResponse(Activo activo) {
        ActivoResponse response = new ActivoResponse();
        response.setIdentificadorTecnico(activo.getIdentificadorTecnico());
        response.setFolioInventario(activo.getFolioInventario());
        response.setNumeroSerie(activo.getNumeroSerie());
        response.setMarcaModelo(activo.getMarcaModelo());
        response.setEstado(activo.getEstado());
        response.setCostoAdquisicion(activo.getCostoAdquisicion());
        response.setFechaIngreso(activo.getFechaIngreso());
        response.setCategoriaId(activo.getCategoria().getId());
        response.setCategoriaNombre(activo.getCategoria().getNombre());
        return response;
    }
}