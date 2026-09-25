package com.inventario.inventario_backend.repository;

import com.inventario.inventario_backend.entity.Categoria;
import com.inventario.inventario_backend.entity.FolioInventarioCounter;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface FolioInventarioCounterRepository extends JpaRepository<FolioInventarioCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FolioInventarioCounter> findByCategoriaAndAnio(Categoria categoria, Integer anio);
}
