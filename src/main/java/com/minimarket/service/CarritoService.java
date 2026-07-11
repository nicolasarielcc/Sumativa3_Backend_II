package com.minimarket.service;

import com.minimarket.entity.Carrito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarritoService {
    List<Carrito> findAll();
    Page<Carrito> findAll(Pageable pageable);
    Carrito findById(Long id);
    Carrito save(Carrito carrito);
    void deleteById(Long id);
    List<Carrito> findByUsuarioId(Long usuarioId);
}
