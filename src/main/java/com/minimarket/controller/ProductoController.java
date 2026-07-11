package com.minimarket.controller;

import com.minimarket.assembler.CategoriaModelAssembler;
import com.minimarket.assembler.ProductoModelAssembler;
import com.minimarket.dto.AsignarCategoriaRequest;
import com.minimarket.dto.ProductoResponseDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.ProductoService;
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
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para la gestión de productos")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Listar todos los productos", description = "Retorna una lista paginada con todos los productos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente", links = {
            @Link(name = "self", description = "Enlace a esta página", operationId = "listarProductos")
        })
    })
    public ResponseEntity<PagedModel<EntityModel<ProductoResponseDTO>>> listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Producto> productosPage = productoService.findAll(pageable);

        List<EntityModel<ProductoResponseDTO>> productosModel = productosPage.getContent().stream()
                .map(productoModelAssembler::toModel)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                productosPage.getSize(),
                productosPage.getNumber(),
                productosPage.getTotalElements(),
                productosPage.getTotalPages()
        );

        PagedModel<EntityModel<ProductoResponseDTO>> pagedModel = PagedModel.of(productosModel, metadata);
        pagedModel.add(linkTo(methodOn(ProductoController.class).listarProductos(page, size, sortBy, sortDir)).withSelfRel());
        pagedModel.add(linkTo(methodOn(ProductoController.class).listarProductos(0, size, sortBy, sortDir)).withRel("first"));
        pagedModel.add(linkTo(methodOn(ProductoController.class).listarProductos(productosPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last"));
        if (productosPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(ProductoController.class).listarProductos(productosPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (productosPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(ProductoController.class).listarProductos(productosPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID", description = "Retorna un producto según su ID", operationId = "obtenerProductoPorId",
        responses = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(schema = @Schema(implementation = ProductoResponseDTO.class)), links = {
                @Link(name = "self", description = "Enlace al producto", operationId = "obtenerProductoPorId"),
                @Link(name = "allProductos", description = "Enlace a todos los productos", operationId = "listarProductos")
            }),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<ProductoResponseDTO>> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable @Positive Long id) {
        Producto producto = productoService.findById(id);
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto", description = "Registra un nuevo producto en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<ProductoResponseDTO>> guardarProducto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del producto a crear", required = true)
            @Valid @RequestBody Producto producto) {
        Producto nuevo = productoService.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<ProductoResponseDTO>> actualizarProducto(
            @Parameter(description = "ID del producto", required = true) @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del producto a actualizar", required = true)
            @Valid @RequestBody Producto producto) {
        producto.setId(id);
        Producto actualizado = productoService.save(producto);
        return ResponseEntity.ok(productoModelAssembler.toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto", description = "Elimina un producto del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarProducto(
            @Parameter(description = "ID del producto", required = true) @PathVariable @Positive Long id) {
        productoService.deleteById(id);
        EntityModel<Map<String, String>> response = EntityModel.of(
                Map.of("message", "Producto eliminado exitosamente"),
                linkTo(methodOn(ProductoController.class).listarProductos(0, 10, "nombre", "asc")).withRel("allProductos"),
                linkTo(methodOn(ProductoController.class).guardarProducto(null)).withRel("addProducto")
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/categoria")
    @Operation(summary = "Obtener categoría de un producto", description = "Retorna la categoría asociada a un producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado o no tiene categoría")
    })
    public ResponseEntity<EntityModel<?>> obtenerCategoria(
            @Parameter(description = "ID del producto", required = true) @PathVariable @Positive Long id) {
        Producto producto = productoService.findById(id);
        if (producto.getCategoria() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(categoriaModelAssembler.toModel(producto.getCategoria()));
    }

    @PostMapping("/{id}/categoria")
    @Operation(summary = "Asignar categoría a un producto", description = "Asigna una categoría existente a un producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría asignada correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrados"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<ProductoResponseDTO>> asignarCategoria(
            @Parameter(description = "ID del producto", required = true) @PathVariable @Positive Long id,
            @Valid @RequestBody AsignarCategoriaRequest request) {
        Producto producto = productoService.asignarCategoria(id, request.getCategoriaId());
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }
}
