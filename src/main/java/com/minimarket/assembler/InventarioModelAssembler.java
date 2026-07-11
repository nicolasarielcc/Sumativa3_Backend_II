package com.minimarket.assembler;

import com.minimarket.controller.InventarioController;
import com.minimarket.controller.ProductoController;
import com.minimarket.dto.InventarioResponseDTO;
import com.minimarket.entity.Inventario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class InventarioModelAssembler implements RepresentationModelAssembler<Inventario, EntityModel<InventarioResponseDTO>> {

    @Override
    public EntityModel<InventarioResponseDTO> toModel(Inventario inventario) {
        InventarioResponseDTO dto = InventarioResponseDTO.from(inventario);
        return EntityModel.of(dto,
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(inventario.getId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario(0, 10, "fechaMovimiento", "desc")).withRel("listar"),
                linkTo(methodOn(InventarioController.class).registrarMovimiento(null)).withRel("crear"),
                linkTo(methodOn(InventarioController.class).actualizarMovimiento(inventario.getId(), null)).withRel("editar"),
                linkTo(methodOn(InventarioController.class).eliminarMovimiento(inventario.getId())).withRel("eliminar"),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(inventario.getProducto().getId())).withRel("producto")
        );
    }
}
