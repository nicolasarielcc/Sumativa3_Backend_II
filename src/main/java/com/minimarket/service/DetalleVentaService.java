package com.minimarket.service;

import com.minimarket.entity.DetalleVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DetalleVentaService {
    List<DetalleVenta> findAll();
    Page<DetalleVenta> findAll(Pageable pageable);
    DetalleVenta findById(Long id);
    DetalleVenta save(DetalleVenta detalleVenta);
    void deleteById(Long id);
    List<DetalleVenta> findByVentaId(Long ventaId);
}
