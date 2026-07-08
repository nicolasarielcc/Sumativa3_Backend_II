package com.minimarket.assembler;

import com.minimarket.controller.UsuarioController;
import com.minimarket.entity.Usuario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>> {

    @Override
    public EntityModel<Usuario> toModel(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("listar"),
                linkTo(methodOn(UsuarioController.class).guardarUsuario(null)).withRel("crear"),
                linkTo(methodOn(UsuarioController.class).actualizarUsuario(usuario.getId(), null)).withRel("editar"),
                linkTo(methodOn(UsuarioController.class).eliminarUsuario(usuario.getId())).withRel("eliminar")
        );
    }
}
