package com.inventario.inventario_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ActivoServiceTest {

    @Mock
    private ActivoRepository activoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private ActivoService activoService;
    private Categoria categoria;
    private ActivoRequest request;

    @BeforeEach
    void setUp() {
        activoService = new ActivoService(activoRepository, categoriaRepository);

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Laptop");
        categoria.setCodigoPrefijo("LAP");

        request = new ActivoRequest();
        request.setNumeroSerie("SERIE-001");
        request.setMarcaModelo("Dell Latitude");
        request.setEstado(EstadoActivo.DISPONIBLE);
        request.setCostoAdquisicion(new BigDecimal("15000.00"));
        request.setCategoriaId(categoria.getId());
    }

    @Test
    void debeRegistrarActivoConFolioGenerado() {
        when(categoriaRepository.findByIdForUpdate(categoria.getId())).thenReturn(Optional.of(categoria));
        when(activoRepository.findByNumeroSerie(request.getNumeroSerie())).thenReturn(Optional.empty());
        when(activoRepository.findFoliosPorCategoriaYAnio(
                eq(categoria.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(activoRepository.save(any(Activo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivoResponse response = activoService.registrar(request);

        assertThat(response.getIdentificadorTecnico()).isNotNull();
        assertThat(response.getFolioInventario()).matches("LAP-\\d{4}-001");
        assertThat(response.getFechaIngreso()).isNotNull();
        assertThat(response.getCategoriaId()).isEqualTo(categoria.getId());
        verify(activoRepository).save(any(Activo.class));
    }

    @Test
    void debeRechazarRegistroCuandoLaCategoriaNoExiste() {
        when(categoriaRepository.findByIdForUpdate(categoria.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activoService.registrar(request))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeRechazarRegistroConNumeroDeSerieDuplicado() {
        Activo existente = activoConId(UUID.randomUUID());
        when(categoriaRepository.findByIdForUpdate(categoria.getId())).thenReturn(Optional.of(categoria));
        when(activoRepository.findByNumeroSerie(request.getNumeroSerie())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> activoService.registrar(request))
                .isInstanceOf(NumeroSerieDuplicadoException.class);
    }

    @Test
    void debeRechazarConsultaDeActivoInexistente() {
        UUID id = UUID.randomUUID();
        when(activoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activoService.obtenerPorId(id))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeActualizarActivoSinCambiarDatosAdministrados() {
        UUID id = UUID.randomUUID();
        Activo activo = activoConId(id);
        String folioOriginal = activo.getFolioInventario();
        LocalDateTime fechaOriginal = activo.getFechaIngreso();
        when(activoRepository.findById(id)).thenReturn(Optional.of(activo));
        when(categoriaRepository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
        when(activoRepository.findByNumeroSerie(request.getNumeroSerie())).thenReturn(Optional.of(activo));
        when(activoRepository.save(activo)).thenReturn(activo);

        ActivoResponse response = activoService.actualizar(id, request);

        assertThat(response.getIdentificadorTecnico()).isEqualTo(id);
        assertThat(response.getFolioInventario()).isEqualTo(folioOriginal);
        assertThat(response.getFechaIngreso()).isEqualTo(fechaOriginal);
        assertThat(activo.getMarcaModelo()).isEqualTo(request.getMarcaModelo());
    }

    @Test
    void debeRechazarActualizacionConNumeroDeSerieDeOtroActivo() {
        UUID id = UUID.randomUUID();
        Activo otroActivo = activoConId(UUID.randomUUID());
        when(activoRepository.findById(id)).thenReturn(Optional.of(activoConId(id)));
        when(categoriaRepository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
        when(activoRepository.findByNumeroSerie(request.getNumeroSerie())).thenReturn(Optional.of(otroActivo));

        assertThatThrownBy(() -> activoService.actualizar(id, request))
                .isInstanceOf(NumeroSerieDuplicadoException.class);
    }

    @Test
    void debeCambiarEstadoCorrectamente() {
        UUID id = UUID.randomUUID();
        Activo activo = activoConId(id);
        when(activoRepository.findById(id)).thenReturn(Optional.of(activo));
        when(activoRepository.save(activo)).thenReturn(activo);

        ActivoResponse response = activoService.cambiarEstado(id, EstadoActivo.ASIGNADO);

        assertThat(response.getEstado()).isEqualTo(EstadoActivo.ASIGNADO);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoActivo.class, names = {"DISPONIBLE", "ASIGNADO", "EN_MANTENIMIENTO"})
    void noDebePermitirRegresarDesdeBaja(EstadoActivo nuevoEstado) {
        UUID id = UUID.randomUUID();
        Activo activo = activoConId(id);
        activo.setEstado(EstadoActivo.BAJA);
        when(activoRepository.findById(id)).thenReturn(Optional.of(activo));

        assertThatThrownBy(() -> activoService.cambiarEstado(id, nuevoEstado))
                .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    void debeBuscarConFiltrosYPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Activo activo = activoConId(UUID.randomUUID());
        Page<Activo> page = new PageImpl<>(List.of(activo), pageable, 1);
        when(activoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ActivoResponse> response = activoService.buscar(
                "SERIE",
                "Dell",
                categoria.getId(),
                EstadoActivo.DISPONIBLE,
                BigDecimal.ZERO,
                new BigDecimal("20000"),
                pageable
        );

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        verify(activoRepository).findAll(any(Specification.class), eq(pageable));
    }

    private Activo activoConId(UUID id) {
        Activo activo = new Activo();
        activo.setIdentificadorTecnico(id);
        activo.setFolioInventario("LAP-2026-001");
        activo.setNumeroSerie(request.getNumeroSerie());
        activo.setMarcaModelo(request.getMarcaModelo());
        activo.setEstado(EstadoActivo.DISPONIBLE);
        activo.setCostoAdquisicion(request.getCostoAdquisicion());
        activo.setFechaIngreso(LocalDateTime.of(2026, 1, 1, 10, 0));
        activo.setCategoria(categoria);
        return activo;
    }
}