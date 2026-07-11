package com.minimarket.dto;

import com.minimarket.entity.Venta;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

@Relation(collectionRelation = "ventas", itemRelation = "venta")
public class VentaResponseDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private Date fecha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioUsername() { return usuarioUsername; }
    public void setUsuarioUsername(String usuarioUsername) { this.usuarioUsername = usuarioUsername; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public static VentaResponseDTO from(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        if (venta.getUsuario() != null) {
            dto.setUsuarioId(venta.getUsuario().getId());
            dto.setUsuarioUsername(venta.getUsuario().getUsername());
        }
        return dto;
    }
}
