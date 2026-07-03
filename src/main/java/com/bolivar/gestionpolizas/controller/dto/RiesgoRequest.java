package com.bolivar.gestionpolizas.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiesgoRequest {

    @NotBlank(message = "La descripción del riesgo es obligatoria")
    private String descripcion;

    @NotBlank(message = "La cobertura del riesgo es obligatoria")
    private String cobertura;
}
