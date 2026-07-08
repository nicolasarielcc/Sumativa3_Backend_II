package com.minimarket.assembler;

import com.minimarket.controller.CategoriaController;
import com.minimarket.entity.Categoria;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CategoriaModelAssembler implements RepresentationModelAssembler<Categoria, EntityModel<Categoria>> {

    @Override
    public EntityModel<Categoria> toModel(Categoria categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("listar"),
                linkTo(methodOn(CategoriaController.class).guardarCategoria(null)).withRel("crear"),
                linkTo(methodOn(CategoriaController.class).actualizarCategoria(categoria.getId(), null)).withRel("editar"),
                linkTo(methodOn(CategoriaController.class).eliminarCategoria(categoria.getId())).withRel("eliminar")
        );
    }
}
