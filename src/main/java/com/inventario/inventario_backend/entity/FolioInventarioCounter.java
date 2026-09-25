package com.inventario.inventario_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "folio_inventario_contadores",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_folio_contador_categoria_anio",
                        columnNames = {"categoria_id", "anio"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class FolioInventarioCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @NotNull
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @NotNull
    @Min(0)
    @Column(name = "ultimo_consecutivo", nullable = false)
    private Integer ultimoConsecutivo = 0;
}
