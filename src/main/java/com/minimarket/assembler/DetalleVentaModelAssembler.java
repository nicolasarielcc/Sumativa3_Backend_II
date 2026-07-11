package com.minimarket.assembler;

import com.minimarket.controller.DetalleVentaController;
import com.minimarket.controller.ProductoController;
import com.minimarket.controller.VentaController;
import com.minimarket.dto.DetalleVentaResponseDTO;
import com.minimarket.entity.DetalleVenta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DetalleVentaModelAssembler implements RepresentationModelAssembler<DetalleVenta, EntityModel<DetalleVentaResponseDTO>> {

    @Override
    public EntityModel<DetalleVentaResponseDTO> toModel(DetalleVenta detalleVenta) {
        DetalleVentaResponseDTO dto = DetalleVentaResponseDTO.from(detalleVenta);
        return EntityModel.of(dto,
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalleVenta.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas(0, 10, "id", "asc")).withRel("listar"),
                linkTo(methodOn(DetalleVentaController.class).guardarDetalleVenta(null)).withRel("crear"),
                linkTo(methodOn(DetalleVentaController.class).actualizarDetalleVenta(detalleVenta.getId(), null)).withRel("editar"),
                linkTo(methodOn(DetalleVentaController.class).eliminarDetalleVenta(detalleVenta.getId())).withRel("eliminar"),
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(detalleVenta.getVenta().getId())).withRel("venta"),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(detalleVenta.getProducto().getId())).withRel("producto")
        );
    }
}
