package com.bolivar.gestionpolizas.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreMockRequest {

    @NotBlank(message = "El campo 'evento' es obligatorio")
    private String evento;

    @NotNull(message = "El campo 'polizaId' es obligatorio")
    private Long polizaId;
}
