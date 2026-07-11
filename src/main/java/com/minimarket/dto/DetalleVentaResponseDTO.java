package com.minimarket.dto;

import com.minimarket.entity.DetalleVenta;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "detalleVentas", itemRelation = "detalleVenta")
public class DetalleVentaResponseDTO {

    private Long id;
    private Long ventaId;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private Double precio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public static DetalleVentaResponseDTO from(DetalleVenta detalleVenta) {
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        dto.setId(detalleVenta.getId());
        dto.setCantidad(detalleVenta.getCantidad());
        dto.setPrecio(detalleVenta.getPrecio());
        if (detalleVenta.getVenta() != null) {
            dto.setVentaId(detalleVenta.getVenta().getId());
        }
        if (detalleVenta.getProducto() != null) {
            dto.setProductoId(detalleVenta.getProducto().getId());
            dto.setProductoNombre(detalleVenta.getProducto().getNombre());
        }
        return dto;
    }
}
