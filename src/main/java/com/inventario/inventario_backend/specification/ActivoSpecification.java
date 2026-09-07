package com.inventario.inventario_backend.specification;

import com.inventario.inventario_backend.entity.Activo;
import com.inventario.inventario_backend.entity.Categoria;
import com.inventario.inventario_backend.enums.EstadoActivo;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ActivoSpecification {

    private ActivoSpecification() {
    }

    public static Specification<Activo> conFiltros(
            String numeroSerie,
            String marcaModelo,
            Long categoriaId,
            EstadoActivo estado,
            BigDecimal costoMin,
            BigDecimal costoMax) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (numeroSerie != null && !numeroSerie.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.<String>get("numeroSerie")),
                        likePattern(numeroSerie)
                ));
            }

            if (marcaModelo != null && !marcaModelo.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.<String>get("marcaModelo")),
                        likePattern(marcaModelo)
                ));
            }

            if (categoriaId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("categoria", JoinType.INNER).<Long>get("id"),
                        categoriaId
                ));
            }

            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.<EstadoActivo>get("estado"), estado));
            }

            if (costoMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<BigDecimal>get("costoAdquisicion"),
                        costoMin
                ));
            }

            if (costoMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<BigDecimal>get("costoAdquisicion"),
                        costoMax
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String likePattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}