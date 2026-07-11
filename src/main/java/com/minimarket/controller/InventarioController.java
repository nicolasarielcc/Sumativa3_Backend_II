package com.minimarket.controller;

import com.minimarket.assembler.InventarioModelAssembler;
import com.minimarket.dto.InventarioResponseDTO;
import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
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
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Operaciones CRUD para la gestión de movimientos de inventario")
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;
    @Autowired
    private InventarioModelAssembler inventarioModelAssembler;

    @GetMapping
    @Operation(summary = "Listar movimientos de inventario", description = "Retorna todos los movimientos registrados en el inventario de forma paginada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida correctamente")
    })
    public ResponseEntity<PagedModel<EntityModel<InventarioResponseDTO>>> listarMovimientosDeInventario(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaMovimiento") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Inventario> inventarioPage = inventarioService.findAll(pageable);

        List<EntityModel<InventarioResponseDTO>> inventarioModel = inventarioPage.getContent().stream()
                .map(inventarioModelAssembler::toModel)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                inventarioPage.getSize(),
                inventarioPage.getNumber(),
                inventarioPage.getTotalElements(),
                inventarioPage.getTotalPages()
        );

        PagedModel<EntityModel<InventarioResponseDTO>> pagedModel = PagedModel.of(inventarioModel, metadata);
        pagedModel.add(linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario(page, size, sortBy, sortDir)).withSelfRel());
        pagedModel.add(linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario(0, size, sortBy, sortDir)).withRel("first"));
        pagedModel.add(linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario(inventarioPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last"));
        if (inventarioPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario(inventarioPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (inventarioPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario(inventarioPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un movimiento por ID", description = "Retorna un movimiento de inventario según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado", content = @Content(schema = @Schema(implementation = InventarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    public ResponseEntity<EntityModel<InventarioResponseDTO>> obtenerMovimientoPorId(
            @Parameter(description = "ID del movimiento", required = true) @PathVariable @Positive Long id) {
        Inventario inventario = inventarioService.findById(id);
        return ResponseEntity.ok(inventarioModelAssembler.toModel(inventario));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo movimiento", description = "Registra un nuevo movimiento de inventario en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<InventarioResponseDTO>> registrarMovimiento(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del movimiento a registrar", required = true)
            @Valid @RequestBody Inventario inventario) {
        Inventario nuevo = inventarioService.save(inventario);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un movimiento de inventario", description = "Actualiza los datos de un movimiento existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimiento actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<InventarioResponseDTO>> actualizarMovimiento(
            @Parameter(description = "ID del movimiento", required = true) @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del movimiento", required = true)
            @Valid @RequestBody Inventario inventario) {
        inventario.setId(id);
        Inventario actualizado = inventarioService.save(inventario);
        return ResponseEntity.ok(inventarioModelAssembler.toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un movimiento de inventario", description = "Elimina un movimiento del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimiento eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarMovimiento(
            @Parameter(description = "ID del movimiento a eliminar", required = true) @PathVariable @Positive Long id) {
        inventarioService.deleteById(id);
        EntityModel<Map<String, String>> response = EntityModel.of(
                Map.of("message", "Movimiento eliminado exitosamente"),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario(0, 10, "fechaMovimiento", "desc")).withRel("allInventario"),
                linkTo(methodOn(InventarioController.class).registrarMovimiento(null)).withRel("addMovimiento")
        );
        return ResponseEntity.ok(response);
    }
}
