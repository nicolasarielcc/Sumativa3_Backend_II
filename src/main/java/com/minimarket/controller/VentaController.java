package com.minimarket.controller;

import com.minimarket.assembler.VentaModelAssembler;
import com.minimarket.dto.VentaResponseDTO;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Operaciones para la gestión de ventas")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {

    @Autowired
    private VentaService ventaService;
    @Autowired
    private VentaModelAssembler ventaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todas las ventas", description = "Retorna una lista paginada con todas las ventas registradas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida correctamente")
    })
    public ResponseEntity<PagedModel<EntityModel<VentaResponseDTO>>> listarVentas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Venta> ventasPage = ventaService.findAll(pageable);

        List<EntityModel<VentaResponseDTO>> ventasModel = ventasPage.getContent().stream()
                .map(ventaModelAssembler::toModel)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                ventasPage.getSize(),
                ventasPage.getNumber(),
                ventasPage.getTotalElements(),
                ventasPage.getTotalPages()
        );

        PagedModel<EntityModel<VentaResponseDTO>> pagedModel = PagedModel.of(ventasModel, metadata);
        pagedModel.add(linkTo(methodOn(VentaController.class).listarVentas(page, size, sortBy, sortDir)).withSelfRel());
        pagedModel.add(linkTo(methodOn(VentaController.class).listarVentas(0, size, sortBy, sortDir)).withRel("first"));
        pagedModel.add(linkTo(methodOn(VentaController.class).listarVentas(ventasPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last"));
        if (ventasPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(VentaController.class).listarVentas(ventasPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (ventasPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(VentaController.class).listarVentas(ventasPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por ID", description = "Retorna una venta según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venta encontrada", content = @Content(schema = @Schema(implementation = VentaResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<EntityModel<VentaResponseDTO>> obtenerVentaPorId(
            @Parameter(description = "ID de la venta", required = true) @PathVariable @Positive Long id) {
        Venta venta = ventaService.findById(id);
        return ResponseEntity.ok(ventaModelAssembler.toModel(venta));
    }

    @PostMapping
    @Operation(summary = "Registrar una venta", description = "Registra una nueva venta en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Venta registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<VentaResponseDTO>> guardarVenta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la venta a registrar", required = true)
            @Valid @RequestBody Venta venta) {
        Venta nueva = ventaService.save(venta);
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaModelAssembler.toModel(nueva));
    }
}
