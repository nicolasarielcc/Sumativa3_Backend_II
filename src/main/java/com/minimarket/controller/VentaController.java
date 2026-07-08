package com.minimarket.controller;

import com.minimarket.assembler.VentaModelAssembler;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
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
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Operaciones para la gestión de ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaModelAssembler ventaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todas las ventas", description = "Retorna una lista con todas las ventas registradas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> listarVentas() {
        return ResponseEntity.ok(
                ventaModelAssembler.toCollectionModel(ventaService.findAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por ID", description = "Retorna una venta según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venta encontrada", content = @Content(schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<EntityModel<Venta>> obtenerVentaPorId(
            @Parameter(description = "ID de la venta", required = true) @PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        if (venta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ventaModelAssembler.toModel(venta));
    }

    @PostMapping
    @Operation(summary = "Registrar una venta", description = "Registra una nueva venta en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Venta registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Venta>> guardarVenta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la venta a registrar", required = true)
            @RequestBody Venta venta) {
        Venta nueva = ventaService.save(venta);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ventaModelAssembler.toModel(nueva));
    }
}
