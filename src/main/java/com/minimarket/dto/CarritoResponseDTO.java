package com.minimarket.dto;

import com.minimarket.entity.Carrito;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "carritos", itemRelation = "carrito")
public class CarritoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioUsername() { return usuarioUsername; }
    public void setUsuarioUsername(String usuarioUsername) { this.usuarioUsername = usuarioUsername; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public static CarritoResponseDTO from(Carrito carrito) {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        dto.setCantidad(carrito.getCantidad());
        if (carrito.getUsuario() != null) {
            dto.setUsuarioId(carrito.getUsuario().getId());
            dto.setUsuarioUsername(carrito.getUsuario().getUsername());
        }
        if (carrito.getProducto() != null) {
            dto.setProductoId(carrito.getProducto().getId());
            dto.setProductoNombre(carrito.getProducto().getNombre());
        }
        return dto;
    }
}
