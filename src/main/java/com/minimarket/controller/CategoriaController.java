package com.minimarket.controller;

import com.minimarket.assembler.CategoriaModelAssembler;
import com.minimarket.dto.CategoriaResponseDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Operaciones CRUD para la gestión de categorías")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private CategoriaModelAssembler categoriaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todas las categorías", description = "Retorna una lista paginada con todas las categorías registradas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida correctamente")
    })
    public ResponseEntity<PagedModel<EntityModel<CategoriaResponseDTO>>> listarCategorias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Categoria> categoriasPage = categoriaService.findAll(pageable);

        List<EntityModel<CategoriaResponseDTO>> categoriasModel = categoriasPage.getContent().stream()
                .map(categoriaModelAssembler::toModel)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                categoriasPage.getSize(),
                categoriasPage.getNumber(),
                categoriasPage.getTotalElements(),
                categoriasPage.getTotalPages()
        );

        PagedModel<EntityModel<CategoriaResponseDTO>> pagedModel = PagedModel.of(categoriasModel, metadata);
        pagedModel.add(linkTo(methodOn(CategoriaController.class).listarCategorias(page, size, sortBy, sortDir)).withSelfRel());
        pagedModel.add(linkTo(methodOn(CategoriaController.class).listarCategorias(0, size, sortBy, sortDir)).withRel("first"));
        pagedModel.add(linkTo(methodOn(CategoriaController.class).listarCategorias(categoriasPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last"));
        if (categoriasPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(CategoriaController.class).listarCategorias(categoriasPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (categoriasPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(CategoriaController.class).listarCategorias(categoriasPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría por ID", description = "Retorna una categoría según su ID", operationId = "obtenerCategoriaPorId",
        responses = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(schema = @Schema(implementation = CategoriaResponseDTO.class)), links = {
                @Link(name = "self", description = "Enlace a la categoría", operationId = "obtenerCategoriaPorId")
            }),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<EntityModel<CategoriaResponseDTO>> obtenerCategoriaPorId(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable @Positive Long id) {
        Categoria categoria = categoriaService.findById(id);
        return ResponseEntity.ok(categoriaModelAssembler.toModel(categoria));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva categoría", description = "Registra una nueva categoría en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<CategoriaResponseDTO>> guardarCategoria(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la categoría a crear", required = true)
            @Valid @RequestBody Categoria categoria) {
        Categoria nueva = categoriaService.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaModelAssembler.toModel(nueva));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría", description = "Actualiza los datos de una categoría existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<CategoriaResponseDTO>> actualizarCategoria(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la categoría a actualizar", required = true)
            @Valid @RequestBody Categoria categoria) {
        categoria.setId(id);
        Categoria actualizada = categoriaService.save(categoria);
        return ResponseEntity.ok(categoriaModelAssembler.toModel(actualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría", description = "Elimina una categoría del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarCategoria(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable @Positive Long id) {
        categoriaService.deleteById(id);
        EntityModel<Map<String, String>> response = EntityModel.of(
                Map.of("message", "Categoría eliminada exitosamente"),
                linkTo(methodOn(CategoriaController.class).listarCategorias(0, 10, "nombre", "asc")).withRel("allCategorias"),
                linkTo(methodOn(CategoriaController.class).guardarCategoria(null)).withRel("addCategoria")
        );
        return ResponseEntity.ok(response);
    }
}
