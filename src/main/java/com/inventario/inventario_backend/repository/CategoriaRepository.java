package com.inventario.inventario_backend.repository;

import com.inventario.inventario_backend.entity.Categoria;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Categoria c where c.id = :id")
	Optional<Categoria> findByIdForUpdate(@Param("id") Long id);
}