package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para asignar una categoría a un producto")
public class AsignarCategoriaRequest {

    @NotNull(message = "El ID de la categoría es obligatorio")
    @Schema(description = "ID de la categoría a asignar", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoriaId;

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}
