package com.minimarket.controller;

import com.minimarket.assembler.CarritoModelAssembler;
import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
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
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Operaciones CRUD para la gestión del carrito de compras")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoModelAssembler carritoModelAssembler;

    @GetMapping
    @Operation(summary = "Listar carrito", description = "Retorna todos los items del carrito de compras")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista del carrito obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Carrito>>> listarCarrito() {
        return ResponseEntity.ok(
                carritoModelAssembler.toCollectionModel(carritoService.findAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un item del carrito por ID", description = "Retorna un item del carrito según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item encontrado", content = @Content(schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<EntityModel<Carrito>> obtenerCarritoPorId(
            @Parameter(description = "ID del item en el carrito", required = true) @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(carritoModelAssembler.toModel(carrito));
    }

    @PostMapping
    @Operation(summary = "Agregar producto al carrito", description = "Agrega un nuevo item al carrito de compras")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item agregado al carrito exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Carrito>> agregarProductoAlCarrito(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del item a agregar al carrito", required = true)
            @RequestBody Carrito carrito) {
        Carrito nuevo = carritoService.save(carrito);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(carritoModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un item del carrito", description = "Actualiza la cantidad o datos de un item en el carrito")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Carrito>> actualizarCarrito(
            @Parameter(description = "ID del item en el carrito", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del item", required = true)
            @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente == null) return ResponseEntity.notFound().build();
        carrito.setId(id);
        return ResponseEntity.ok(carritoModelAssembler.toModel(carritoService.save(carrito)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un item del carrito", description = "Elimina un item del carrito de compras")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<Void> eliminarProductoDelCarrito(
            @Parameter(description = "ID del item a eliminar", required = true) @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito == null) return ResponseEntity.notFound().build();
        carritoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
