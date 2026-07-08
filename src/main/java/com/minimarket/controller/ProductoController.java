package com.minimarket.controller;

import com.minimarket.assembler.CategoriaModelAssembler;
import com.minimarket.assembler.ProductoModelAssembler;
import com.minimarket.dto.AsignarCategoriaRequest;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para la gestión de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductoModelAssembler productoModelAssembler;

    @Autowired
    private CategoriaModelAssembler categoriaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Retorna una lista con todos los productos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listarProductos() {
        return ResponseEntity.ok(
                productoModelAssembler.toCollectionModel(productoService.findAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID", description = "Retorna un producto según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto", description = "Registra un nuevo producto en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Producto>> guardarProducto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del producto a crear", required = true)
            @RequestBody Producto producto) {
        Producto nuevo = productoService.save(producto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del producto a actualizar", required = true)
            @RequestBody Producto producto) {
        Producto productoExistente = productoService.findById(id);
        if (productoExistente == null) return ResponseEntity.notFound().build();
        producto.setId(id);
        return ResponseEntity.ok(productoModelAssembler.toModel(productoService.save(producto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto", description = "Elimina un producto del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/categoria")
    @Operation(summary = "Obtener categoría de un producto", description = "Retorna la categoría asociada a un producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado o no tiene categoría")
    })
    public ResponseEntity<EntityModel<Categoria>> obtenerCategoria(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null || producto.getCategoria() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(categoriaModelAssembler.toModel(producto.getCategoria()));
    }

    @PostMapping("/{id}/categoria")
    @Operation(summary = "Asignar categoría a un producto", description = "Asigna una categoría existente a un producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría asignada correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrados"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Producto>> asignarCategoria(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id,
            @Valid @RequestBody AsignarCategoriaRequest request) {
        Producto producto = productoService.asignarCategoria(id, request.getCategoriaId());
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }
}
