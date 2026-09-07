package com.inventario.inventario_backend.entity;

import com.inventario.inventario_backend.enums.EstadoActivo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "activos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_activo_folio_inventario", columnNames = "folio_inventario"),
                @UniqueConstraint(name = "uk_activo_numero_serie", columnNames = "numero_serie")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Activo {

    @Id
    @Column(name = "identificador_tecnico", nullable = false, updatable = false)
    private UUID identificadorTecnico;

    @Column(name = "folio_inventario", unique = true)
    private String folioInventario;

    @NotBlank
    @Column(name = "numero_serie", nullable = false, unique = true)
    private String numeroSerie;

    @NotBlank
    @Column(name = "marca_modelo", nullable = false)
    private String marcaModelo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoActivo estado;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "costo_adquisicion", nullable = false, precision = 19, scale = 2)
    private BigDecimal costoAdquisicion;

    @NotNull
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}