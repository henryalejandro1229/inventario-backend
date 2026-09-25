package com.inventario.inventario_backend.repository;

import com.inventario.inventario_backend.entity.Activo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivoRepository extends JpaRepository<Activo, UUID>, JpaSpecificationExecutor<Activo> {

    Optional<Activo> findByNumeroSerie(String numeroSerie);
}