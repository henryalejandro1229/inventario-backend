package com.inventario.inventario_backend.service;

import com.inventario.inventario_backend.dto.categoria.CategoriaResponse;
import com.inventario.inventario_backend.entity.Categoria;
import com.inventario.inventario_backend.repository.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setId(categoria.getId());
        response.setNombre(categoria.getNombre());
        response.setCodigoPrefijo(categoria.getCodigoPrefijo());
        return response;
    }
}