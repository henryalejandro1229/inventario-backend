package com.inventario.inventario_backend.repository;

import com.inventario.inventario_backend.entity.Activo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface ActivoRepository extends JpaRepository<Activo, UUID>, JpaSpecificationExecutor<Activo> {

	Optional<Activo> findByNumeroSerie(String numeroSerie);

	    @Query("""
		    SELECT a
		    FROM Activo a
		    WHERE a.categoria.id = :categoriaId
		      AND a.fechaIngreso >= :inicioAnio
		      AND a.fechaIngreso < :inicioSiguienteAnio
		    ORDER BY a.folioInventario DESC
		    """)
	    List<Activo> findFoliosPorCategoriaYAnio(
		    @Param("categoriaId") Long categoriaId,
		    @Param("inicioAnio") LocalDateTime inicioAnio,
		    @Param("inicioSiguienteAnio") LocalDateTime inicioSiguienteAnio
	    );
}