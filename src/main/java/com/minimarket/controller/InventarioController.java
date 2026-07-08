package com.minimarket.controller;

import com.minimarket.assembler.InventarioModelAssembler;
import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
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
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Operaciones CRUD para la gestión de movimientos de inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioModelAssembler inventarioModelAssembler;

    @GetMapping
    @Operation(summary = "Listar movimientos de inventario", description = "Retorna todos los movimientos registrados en el inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Inventario>>> listarMovimientosDeInventario() {
        return ResponseEntity.ok(
                inventarioModelAssembler.toCollectionModel(inventarioService.findAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un movimiento por ID", description = "Retorna un movimiento de inventario según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado", content = @Content(schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    public ResponseEntity<EntityModel<Inventario>> obtenerMovimientoPorId(
            @Parameter(description = "ID del movimiento", required = true) @PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(inventarioModelAssembler.toModel(inventario));
    }

    @PostMapping
    @Operation(summary = "Registrar un movimiento", description = "Registra un nuevo movimiento de inventario (entrada o salida)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Inventario>> registrarMovimiento(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del movimiento a registrar", required = true)
            @RequestBody Inventario inventario) {
        Inventario nuevo = inventarioService.save(inventario);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventarioModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un movimiento", description = "Actualiza los datos de un movimiento de inventario existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimiento actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Inventario>> actualizarMovimiento(
            @Parameter(description = "ID del movimiento", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del movimiento", required = true)
            @RequestBody Inventario inventario) {
        Inventario existente = inventarioService.findById(id);
        if (existente == null) return ResponseEntity.notFound().build();
        inventario.setId(id);
        return ResponseEntity.ok(inventarioModelAssembler.toModel(inventarioService.save(inventario)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un movimiento", description = "Elimina un movimiento de inventario del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Movimiento eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(description = "ID del movimiento a eliminar", required = true) @PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) return ResponseEntity.notFound().build();
        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
