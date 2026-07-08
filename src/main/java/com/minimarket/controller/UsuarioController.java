package com.minimarket.controller;

import com.minimarket.assembler.UsuarioModelAssembler;
import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones CRUD para la gestión de usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioModelAssembler usuarioModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "Retorna una lista con todos los usuarios registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listarUsuarios() {
        return ResponseEntity.ok(
                usuarioModelAssembler.toCollectionModel(usuarioService.findAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por ID", description = "Retorna un usuario según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<EntityModel<Usuario>> obtenerUsuarioPorId(
            @Parameter(description = "ID del usuario", required = true) @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return usuario.map(u -> ResponseEntity.ok(usuarioModelAssembler.toModel(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Usuario>> guardarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del usuario a crear", required = true)
            @RequestBody Usuario usuario) {
        Usuario nuevo = usuarioService.save(usuario);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Usuario>> actualizarUsuario(
            @Parameter(description = "ID del usuario", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del usuario", required = true)
            @RequestBody Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isEmpty()) return ResponseEntity.notFound().build();
        usuario.setId(id);
        return ResponseEntity.ok(usuarioModelAssembler.toModel(usuarioService.save(usuario)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", required = true) @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isEmpty()) return ResponseEntity.notFound().build();
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
