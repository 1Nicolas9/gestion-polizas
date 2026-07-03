package com.bolivar.gestionpolizas.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenovarRequest {

    @NotNull(message = "El campo 'ipc' es obligatorio")
    @Positive(message = "El campo 'ipc' debe ser un valor positivo")
    private BigDecimal ipc;
}
