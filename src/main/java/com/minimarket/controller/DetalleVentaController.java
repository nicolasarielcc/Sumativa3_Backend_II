package com.minimarket.controller;

import com.minimarket.assembler.DetalleVentaModelAssembler;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
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
@RequestMapping("/api/detalle-ventas")
@Tag(name = "Detalle de Ventas", description = "Operaciones CRUD para la gestión de detalles de ventas")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private DetalleVentaModelAssembler detalleVentaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar detalles de ventas", description = "Retorna una lista con todos los detalles de ventas registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de detalles obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<DetalleVenta>>> listarDetalleVentas() {
        return ResponseEntity.ok(
                detalleVentaModelAssembler.toCollectionModel(detalleVentaService.findAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un detalle de venta por ID", description = "Retorna un detalle de venta según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle encontrado", content = @Content(schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    public ResponseEntity<EntityModel<DetalleVenta>> obtenerDetalleVentaPorId(
            @Parameter(description = "ID del detalle de venta", required = true) @PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detalleVentaModelAssembler.toModel(detalleVenta));
    }

    @PostMapping
    @Operation(summary = "Crear un detalle de venta", description = "Registra un nuevo detalle de venta en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Detalle creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<DetalleVenta>> guardarDetalleVenta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del detalle a crear", required = true)
            @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta nuevo = detalleVentaService.save(detalleVenta);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(detalleVentaModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un detalle de venta", description = "Actualiza los datos de un detalle de venta existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<DetalleVenta>> actualizarDetalleVenta(
            @Parameter(description = "ID del detalle de venta", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del detalle", required = true)
            @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente == null) return ResponseEntity.notFound().build();
        detalleVenta.setId(id);
        return ResponseEntity.ok(detalleVentaModelAssembler.toModel(detalleVentaService.save(detalleVenta)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un detalle de venta", description = "Elimina un detalle de venta del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Detalle eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    public ResponseEntity<Void> eliminarDetalleVenta(
            @Parameter(description = "ID del detalle a eliminar", required = true) @PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) return ResponseEntity.notFound().build();
        detalleVentaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
