package com.minimarket.assembler;

import com.minimarket.controller.CategoriaController;
import com.minimarket.dto.CategoriaResponseDTO;
import com.minimarket.entity.Categoria;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CategoriaModelAssembler implements RepresentationModelAssembler<Categoria, EntityModel<CategoriaResponseDTO>> {

    @Override
    public EntityModel<CategoriaResponseDTO> toModel(Categoria categoria) {
        CategoriaResponseDTO dto = CategoriaResponseDTO.from(categoria);
        return EntityModel.of(dto,
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias(0, 10, "nombre", "asc")).withRel("listar"),
                linkTo(methodOn(CategoriaController.class).guardarCategoria(null)).withRel("crear"),
                linkTo(methodOn(CategoriaController.class).actualizarCategoria(categoria.getId(), null)).withRel("editar"),
                linkTo(methodOn(CategoriaController.class).eliminarCategoria(categoria.getId())).withRel("eliminar")
        );
    }
}
