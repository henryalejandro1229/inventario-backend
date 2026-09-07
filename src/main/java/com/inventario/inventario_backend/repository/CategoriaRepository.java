package com.inventario.inventario_backend.repository;

import com.inventario.inventario_backend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}