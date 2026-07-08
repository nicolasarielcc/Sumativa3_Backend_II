package com.minimarket.controller;

import com.minimarket.assembler.CategoriaModelAssembler;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
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

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Operaciones CRUD para la gestión de categorías")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaModelAssembler categoriaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todas las categorías", description = "Retorna una lista con todas las categorías registradas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Categoria>>> listarCategorias() {
        return ResponseEntity.ok(
                categoriaModelAssembler.toCollectionModel(categoriaService.findAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría por ID", description = "Retorna una categoría según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<EntityModel<Categoria>> obtenerCategoriaPorId(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(categoriaModelAssembler.toModel(categoria));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva categoría", description = "Registra una nueva categoría en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Categoria>> guardarCategoria(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la categoría a crear", required = true)
            @RequestBody Categoria categoria) {
        Categoria nueva = categoriaService.save(categoria);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaModelAssembler.toModel(nueva));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría", description = "Actualiza los datos de una categoría existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la categoría a actualizar", required = true)
            @RequestBody Categoria categoria) {
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente == null) return ResponseEntity.notFound().build();
        categoria.setId(id);
        return ResponseEntity.ok(categoriaModelAssembler.toModel(categoriaService.save(categoria)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría", description = "Elimina una categoría del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) return ResponseEntity.notFound().build();
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
