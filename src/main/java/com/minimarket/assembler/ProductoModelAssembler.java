package com.minimarket.assembler;

import com.minimarket.controller.ProductoController;
import com.minimarket.dto.ProductoResponseDTO;
import com.minimarket.entity.Producto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<Producto, EntityModel<ProductoResponseDTO>> {

    @Override
    public EntityModel<ProductoResponseDTO> toModel(Producto producto) {
        ProductoResponseDTO dto = ProductoResponseDTO.from(producto);
        return EntityModel.of(dto,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos(0, 10, "nombre", "asc")).withRel("listar"),
                linkTo(methodOn(ProductoController.class).guardarProducto(null)).withRel("crear"),
                linkTo(methodOn(ProductoController.class).actualizarProducto(producto.getId(), null)).withRel("editar"),
                linkTo(methodOn(ProductoController.class).eliminarProducto(producto.getId())).withRel("eliminar"),
                linkTo(methodOn(ProductoController.class).obtenerCategoria(producto.getId())).withRel("categoria"),
                linkTo(methodOn(ProductoController.class).asignarCategoria(producto.getId(), null)).withRel("asignar-categoria")
        );
    }
}
