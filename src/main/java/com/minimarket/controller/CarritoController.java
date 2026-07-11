package com.minimarket.controller;

import com.minimarket.assembler.CarritoModelAssembler;
import com.minimarket.dto.CarritoResponseDTO;
import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
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
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Operaciones CRUD para la gestión del carrito de compras")
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    @Autowired
    private CarritoModelAssembler carritoModelAssembler;

    @GetMapping
    @Operation(summary = "Listar carrito", description = "Retorna todos los items del carrito de compras de forma paginada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista del carrito obtenida correctamente")
    })
    public ResponseEntity<PagedModel<EntityModel<CarritoResponseDTO>>> listarCarrito(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Carrito> carritoPage = carritoService.findAll(pageable);

        List<EntityModel<CarritoResponseDTO>> carritoModel = carritoPage.getContent().stream()
                .map(carritoModelAssembler::toModel)
                .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                carritoPage.getSize(),
                carritoPage.getNumber(),
                carritoPage.getTotalElements(),
                carritoPage.getTotalPages()
        );

        PagedModel<EntityModel<CarritoResponseDTO>> pagedModel = PagedModel.of(carritoModel, metadata);
        pagedModel.add(linkTo(methodOn(CarritoController.class).listarCarrito(page, size, sortBy, sortDir)).withSelfRel());
        pagedModel.add(linkTo(methodOn(CarritoController.class).listarCarrito(0, size, sortBy, sortDir)).withRel("first"));
        pagedModel.add(linkTo(methodOn(CarritoController.class).listarCarrito(carritoPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last"));
        if (carritoPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(CarritoController.class).listarCarrito(carritoPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (carritoPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(CarritoController.class).listarCarrito(carritoPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un item del carrito por ID", description = "Retorna un item del carrito según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item encontrado", content = @Content(schema = @Schema(implementation = CarritoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<EntityModel<CarritoResponseDTO>> obtenerCarritoPorId(
            @Parameter(description = "ID del item en el carrito", required = true) @PathVariable @Positive Long id) {
        Carrito carrito = carritoService.findById(id);
        return ResponseEntity.ok(carritoModelAssembler.toModel(carrito));
    }

    @PostMapping
    @Operation(summary = "Agregar producto al carrito", description = "Agrega un nuevo item al carrito de compras")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item agregado al carrito exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<CarritoResponseDTO>> agregarProductoAlCarrito(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del item a agregar al carrito", required = true)
            @Valid @RequestBody Carrito carrito) {
        Carrito nuevo = carritoService.save(carrito);
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un item del carrito", description = "Actualiza los datos de un item en el carrito")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<CarritoResponseDTO>> actualizarCarrito(
            @Parameter(description = "ID del item en el carrito", required = true) @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del item", required = true)
            @Valid @RequestBody Carrito carrito) {
        carrito.setId(id);
        Carrito actualizado = carritoService.save(carrito);
        return ResponseEntity.ok(carritoModelAssembler.toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un item del carrito", description = "Elimina un item del carrito de compras")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarProductoDelCarrito(
            @Parameter(description = "ID del item a eliminar", required = true) @PathVariable @Positive Long id) {
        carritoService.deleteById(id);
        EntityModel<Map<String, String>> response = EntityModel.of(
                Map.of("message", "Item eliminado exitosamente"),
                linkTo(methodOn(CarritoController.class).listarCarrito(0, 10, "id", "asc")).withRel("allCarrito"),
                linkTo(methodOn(CarritoController.class).agregarProductoAlCarrito(null)).withRel("addItem")
        );
        return ResponseEntity.ok(response);
    }
}
