package com.bolivar.gestionpolizas.controller;

import com.bolivar.gestionpolizas.model.Riesgo;
import com.bolivar.gestionpolizas.service.RiesgoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/riesgos")
@Tag(name = "Riesgos", description = "Endpoints para la gestión de riesgos")
public class RiesgoController {

    private final RiesgoService riesgoService;

    public RiesgoController(RiesgoService riesgoService) {
        this.riesgoService = riesgoService;
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar riesgo", description = "Cancela un riesgo específico cambiando su estado a CANCELADO")
    public ResponseEntity<Riesgo> cancelarRiesgo(
            @Parameter(description = "ID del riesgo a cancelar")
            @PathVariable Long id) {

        Riesgo riesgoCancelado = riesgoService.cancelarRiesgo(id);
        return ResponseEntity.ok(riesgoCancelado);
    }
}
