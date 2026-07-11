package com.minimarket.controller;

import com.minimarket.assembler.UsuarioModelAssembler;
import com.minimarket.dto.UsuarioResponseDTO;
import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones CRUD para la gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioModelAssembler usuarioModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "Retorna una lista paginada con todos los usuarios registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente")
    })
    public ResponseEntity<PagedModel<EntityModel<UsuarioResponseDTO>>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Usuario> usuariosPage = usuarioService.findAll(pageable);

        List<EntityModel<UsuarioResponseDTO>> usuariosModel = usuariosPage.getContent().stream()
                .map(usuarioModelAssembler::toModel)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                usuariosPage.getSize(),
                usuariosPage.getNumber(),
                usuariosPage.getTotalElements(),
                usuariosPage.getTotalPages()
        );

        PagedModel<EntityModel<UsuarioResponseDTO>> pagedModel = PagedModel.of(usuariosModel, metadata);
        pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(page, size, sortBy, sortDir)).withSelfRel());
        pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(0, size, sortBy, sortDir)).withRel("first"));
        pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(usuariosPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last"));
        if (usuariosPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(usuariosPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (usuariosPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(usuariosPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por ID", description = "Retorna un usuario según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> obtenerUsuarioPorId(
            @Parameter(description = "ID del usuario", required = true) @PathVariable @Positive Long id) {
        Usuario usuario = usuarioService.getUsuarioByIdOrThrow(id);
        return ResponseEntity.ok(usuarioModelAssembler.toModel(usuario));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> guardarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del usuario a crear", required = true)
            @Valid @RequestBody Usuario usuario) {
        Usuario nuevo = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> actualizarUsuario(
            @Parameter(description = "ID del usuario", required = true) @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del usuario", required = true)
            @Valid @RequestBody Usuario usuario) {
        Usuario actualizado = usuarioService.updateUsuario(id, usuario);
        return ResponseEntity.ok(usuarioModelAssembler.toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", required = true) @PathVariable @Positive Long id) {
        usuarioService.deleteById(id);
        EntityModel<Map<String, String>> response = EntityModel.of(
                Map.of("message", "Usuario eliminado exitosamente"),
                linkTo(methodOn(UsuarioController.class).listarUsuarios(0, 10, "username", "asc")).withRel("allUsuarios"),
                linkTo(methodOn(UsuarioController.class).guardarUsuario(null)).withRel("addUsuario")
        );
        return ResponseEntity.ok(response);
    }
}
