package com.minimarket.assembler;

import com.minimarket.controller.UsuarioController;
import com.minimarket.controller.VentaController;
import com.minimarket.dto.VentaResponseDTO;
import com.minimarket.entity.Venta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class VentaModelAssembler implements RepresentationModelAssembler<Venta, EntityModel<VentaResponseDTO>> {

    @Override
    public EntityModel<VentaResponseDTO> toModel(Venta venta) {
        VentaResponseDTO dto = VentaResponseDTO.from(venta);
        return EntityModel.of(dto,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas(0, 10, "fecha", "desc")).withRel("listar"),
                linkTo(methodOn(VentaController.class).guardarVenta(null)).withRel("crear"),
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(venta.getUsuario().getId())).withRel("usuario")
        );
    }
}
