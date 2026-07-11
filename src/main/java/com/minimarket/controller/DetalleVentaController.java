package com.minimarket.controller;

import com.minimarket.assembler.DetalleVentaModelAssembler;
import com.minimarket.dto.DetalleVentaResponseDTO;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
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
@RequestMapping("/api/detalle-ventas")
@Tag(name = "Detalle de Ventas", description = "Operaciones CRUD para la gestión de detalles de ventas")
@SecurityRequirement(name = "bearerAuth")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;
    @Autowired
    private DetalleVentaModelAssembler detalleVentaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar detalles de ventas", description = "Retorna una lista paginada con todos los detalles de ventas registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de detalles obtenida correctamente")
    })
    public ResponseEntity<PagedModel<EntityModel<DetalleVentaResponseDTO>>> listarDetalleVentas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DetalleVenta> detalleVentaPage = detalleVentaService.findAll(pageable);

        List<EntityModel<DetalleVentaResponseDTO>> detalleVentaModel = detalleVentaPage.getContent().stream()
                .map(detalleVentaModelAssembler::toModel)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                detalleVentaPage.getSize(),
                detalleVentaPage.getNumber(),
                detalleVentaPage.getTotalElements(),
                detalleVentaPage.getTotalPages()
        );

        PagedModel<EntityModel<DetalleVentaResponseDTO>> pagedModel = PagedModel.of(detalleVentaModel, metadata);
        pagedModel.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas(page, size, sortBy, sortDir)).withSelfRel());
        pagedModel.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas(0, size, sortBy, sortDir)).withRel("first"));
        pagedModel.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas(detalleVentaPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last"));
        if (detalleVentaPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas(detalleVentaPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (detalleVentaPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas(detalleVentaPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un detalle de venta por ID", description = "Retorna un detalle de venta según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle encontrado", content = @Content(schema = @Schema(implementation = DetalleVentaResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    public ResponseEntity<EntityModel<DetalleVentaResponseDTO>> obtenerDetalleVentaPorId(
            @Parameter(description = "ID del detalle de venta", required = true) @PathVariable @Positive Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        return ResponseEntity.ok(detalleVentaModelAssembler.toModel(detalleVenta));
    }

    @PostMapping
    @Operation(summary = "Crear un detalle de venta", description = "Registra un nuevo detalle de venta en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Detalle creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<DetalleVentaResponseDTO>> guardarDetalleVenta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del detalle a crear", required = true)
            @Valid @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta nuevo = detalleVentaService.save(detalleVenta);
        return ResponseEntity.status(HttpStatus.CREATED).body(detalleVentaModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un detalle de venta", description = "Actualiza los datos de un detalle de venta existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<DetalleVentaResponseDTO>> actualizarDetalleVenta(
            @Parameter(description = "ID del detalle de venta", required = true) @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del detalle", required = true)
            @Valid @RequestBody DetalleVenta detalleVenta) {
        detalleVenta.setId(id);
        DetalleVenta actualizado = detalleVentaService.save(detalleVenta);
        return ResponseEntity.ok(detalleVentaModelAssembler.toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un detalle de venta", description = "Elimina un detalle de venta del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarDetalleVenta(
            @Parameter(description = "ID del detalle a eliminar", required = true) @PathVariable @Positive Long id) {
        detalleVentaService.deleteById(id);
        EntityModel<Map<String, String>> response = EntityModel.of(
                Map.of("message", "Detalle de venta eliminado exitosamente"),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas(0, 10, "id", "asc")).withRel("allDetalleVentas"),
                linkTo(methodOn(DetalleVentaController.class).guardarDetalleVenta(null)).withRel("addDetalleVenta")
        );
        return ResponseEntity.ok(response);
    }
}
