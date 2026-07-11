package com.minimarket.dto;

import com.minimarket.entity.Categoria;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "categorias", itemRelation = "categoria")
public class CategoriaResponseDTO {

    private Long id;
    private String nombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public static CategoriaResponseDTO from(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        return dto;
    }
}
