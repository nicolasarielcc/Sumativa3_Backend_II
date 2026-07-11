package com.minimarket.assembler;

import com.minimarket.controller.CarritoController;
import com.minimarket.controller.ProductoController;
import com.minimarket.controller.UsuarioController;
import com.minimarket.dto.CarritoResponseDTO;
import com.minimarket.entity.Carrito;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CarritoModelAssembler implements RepresentationModelAssembler<Carrito, EntityModel<CarritoResponseDTO>> {

    @Override
    public EntityModel<CarritoResponseDTO> toModel(Carrito carrito) {
        CarritoResponseDTO dto = CarritoResponseDTO.from(carrito);
        return EntityModel.of(dto,
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarCarrito(0, 10, "id", "asc")).withRel("listar"),
                linkTo(methodOn(CarritoController.class).agregarProductoAlCarrito(null)).withRel("crear"),
                linkTo(methodOn(CarritoController.class).actualizarCarrito(carrito.getId(), null)).withRel("editar"),
                linkTo(methodOn(CarritoController.class).eliminarProductoDelCarrito(carrito.getId())).withRel("eliminar"),
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(carrito.getUsuario().getId())).withRel("usuario"),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(carrito.getProducto().getId())).withRel("producto")
        );
    }
}
