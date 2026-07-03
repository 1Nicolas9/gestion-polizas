package com.bolivar.gestionpolizas.controller;

import com.bolivar.gestionpolizas.controller.dto.RenovarRequest;
import com.bolivar.gestionpolizas.controller.dto.RiesgoRequest;
import com.bolivar.gestionpolizas.model.Poliza;
import com.bolivar.gestionpolizas.model.Riesgo;
import com.bolivar.gestionpolizas.service.PolizaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/polizas")
@Tag(name = "Pólizas", description = "Endpoints para la gestión de pólizas de seguros")
public class PolizaController {

    private final PolizaService polizaService;

    public PolizaController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    @GetMapping
    @Operation(summary = "Listar pólizas", description = "Lista todas las pólizas con filtros opcionales por tipo y estado")
    public ResponseEntity<List<Poliza>> listarPolizas(
            @Parameter(description = "Tipo de póliza: INDIVIDUAL o COLECTIVA")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Estado de póliza: ACTIVA, RENOVADA o CANCELADA")
            @RequestParam(required = false) String estado) {

        List<Poliza> polizas = polizaService.listarPolizas(tipo, estado);
        return ResponseEntity.ok(polizas);
    }

    @GetMapping("/{id}/riesgos")
    @Operation(summary = "Obtener riesgos de una póliza", description = "Retorna la lista de riesgos asociados a una póliza específica")
    public ResponseEntity<List<Riesgo>> obtenerRiesgos(
            @Parameter(description = "ID de la póliza")
            @PathVariable Long id) {

        List<Riesgo> riesgos = polizaService.obtenerRiesgosDePoliza(id);
        return ResponseEntity.ok(riesgos);
    }

    @PostMapping("/{id}/renovar")
    @Operation(summary = "Renovar póliza", description = "Incrementa canon y prima según IPC y cambia estado a RENOVADA")
    public ResponseEntity<Poliza> renovarPoliza(
            @Parameter(description = "ID de la póliza a renovar")
            @PathVariable Long id,
            @Valid @RequestBody RenovarRequest request) {

        Poliza polizaRenovada = polizaService.renovarPoliza(id, request.getIpc());
        return ResponseEntity.ok(polizaRenovada);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar póliza", description = "Cancela la póliza y todos sus riesgos asociados")
    public ResponseEntity<Poliza> cancelarPoliza(
            @Parameter(description = "ID de la póliza a cancelar")
            @PathVariable Long id) {

        Poliza polizaCancelada = polizaService.cancelarPoliza(id);
        return ResponseEntity.ok(polizaCancelada);
    }

    @PostMapping("/{id}/riesgos")
    @Operation(summary = "Agregar riesgo a póliza", description = "Agrega un nuevo riesgo a una póliza de tipo COLECTIVA")
    public ResponseEntity<Riesgo> agregarRiesgo(
            @Parameter(description = "ID de la póliza")
            @PathVariable Long id,
            @Valid @RequestBody RiesgoRequest request) {

        Riesgo riesgo = Riesgo.builder()
                .descripcion(request.getDescripcion())
                .cobertura(request.getCobertura())
                .build();

        Riesgo riesgoCreado = polizaService.agregarRiesgoAPoliza(id, riesgo);
        return new ResponseEntity<>(riesgoCreado, HttpStatus.CREATED);
    }
}
