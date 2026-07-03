package com.bolivar.gestionpolizas.controller;

import com.bolivar.gestionpolizas.controller.dto.CoreMockRequest;
import com.bolivar.gestionpolizas.service.CoreMockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/core-mock")
@Tag(name = "Core Mock", description = "Endpoint de simulación de comunicación con el CORE")
public class CoreMockController {

    private final CoreMockService coreMockService;

    public CoreMockController(CoreMockService coreMockService) {
        this.coreMockService = coreMockService;
    }

    @PostMapping("/evento")
    @Operation(summary = "Simular evento al CORE", description = "Registra un evento simulado que se enviaría al sistema CORE")
    public ResponseEntity<Map<String, String>> registrarEvento(@Valid @RequestBody CoreMockRequest request) {

        coreMockService.notificarEvento(request.getEvento(), request.getPolizaId());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Evento '" + request.getEvento() + "' registrado exitosamente para póliza ID: " + request.getPolizaId(),
                "estado", "OK"
        ));
    }
}
