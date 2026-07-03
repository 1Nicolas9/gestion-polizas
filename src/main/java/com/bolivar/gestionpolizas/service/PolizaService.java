package com.bolivar.gestionpolizas.service;

import com.bolivar.gestionpolizas.config.exception.NegocioException;
import com.bolivar.gestionpolizas.config.exception.RecursoNoEncontradoException;
import com.bolivar.gestionpolizas.model.*;
import com.bolivar.gestionpolizas.repository.PolizaRepository;
import com.bolivar.gestionpolizas.repository.RiesgoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PolizaService {

    private static final Logger log = LoggerFactory.getLogger(PolizaService.class);

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CoreMockService coreMockService;

    public PolizaService(PolizaRepository polizaRepository,
                         RiesgoRepository riesgoRepository,
                         CoreMockService coreMockService) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.coreMockService = coreMockService;
    }

    public List<Poliza> listarPolizas(String tipo, String estado) {
        TipoPoliza tipoPoliza = null;
        EstadoPoliza estadoPoliza = null;

        if (tipo != null && !tipo.isBlank()) {
            try {
                tipoPoliza = TipoPoliza.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new NegocioException("Tipo de póliza inválido: " + tipo + ". Valores permitidos: INDIVIDUAL, COLECTIVA");
            }
        }

        if (estado != null && !estado.isBlank()) {
            try {
                estadoPoliza = EstadoPoliza.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new NegocioException("Estado de póliza inválido: " + estado + ". Valores permitidos: ACTIVA, RENOVADA, CANCELADA");
            }
        }

        return polizaRepository.findByFiltros(tipoPoliza, estadoPoliza);
    }

    public List<Riesgo> obtenerRiesgosDePoliza(Long polizaId) {
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza no encontrada con ID: " + polizaId));

        return riesgoRepository.findByPolizaId(polizaId);
    }

    public Poliza renovarPoliza(Long polizaId, BigDecimal ipc) {
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza no encontrada con ID: " + polizaId));

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new NegocioException("No se puede renovar una póliza que se encuentra en estado CANCELADA");
        }

        if (ipc == null || ipc.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El valor del IPC debe ser mayor a 0");
        }

        // Incrementar canon mensual y prima según el IPC
        BigDecimal incremento = BigDecimal.ONE.add(ipc);
        poliza.setCanonMensual(poliza.getCanonMensual().multiply(incremento));
        poliza.setPrima(poliza.getPrima().multiply(incremento));
        poliza.setEstado(EstadoPoliza.RENOVADA);

        Poliza polizaRenovada = polizaRepository.save(poliza);

        // Notificar al CORE
        coreMockService.notificarEvento("ACTUALIZACION", polizaId);

        log.info("Póliza ID {} renovada exitosamente con IPC: {}%", polizaId, ipc.multiply(new BigDecimal("100")));

        return polizaRenovada;
    }

    public Poliza cancelarPoliza(Long polizaId) {
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza no encontrada con ID: " + polizaId));

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new NegocioException("La póliza ya se encuentra en estado CANCELADA");
        }

        poliza.setEstado(EstadoPoliza.CANCELADA);
        polizaRepository.save(poliza);

        // Cancelar todos los riesgos asociados
        List<Riesgo> riesgos = riesgoRepository.findByPolizaId(polizaId);
        riesgos.forEach(riesgo -> {
            riesgo.setEstado(EstadoRiesgo.CANCELADO);
            riesgoRepository.save(riesgo);
        });

        // Notificar al CORE
        coreMockService.notificarEvento("ACTUALIZACION", polizaId);

        log.info("Póliza ID {} cancelada. {} riesgos asociados cancelados.", polizaId, riesgos.size());

        return poliza;
    }

    public Riesgo agregarRiesgoAPoliza(Long polizaId, Riesgo riesgo) {
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza no encontrada con ID: " + polizaId));

        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new NegocioException("Solo se pueden agregar riesgos a pólizas de tipo COLECTIVA. La póliza ID " + polizaId + " es de tipo " + poliza.getTipo());
        }

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new NegocioException("No se pueden agregar riesgos a una póliza en estado CANCELADA");
        }

        riesgo.setPolizaId(polizaId);
        riesgo.setEstado(EstadoRiesgo.ACTIVO);

        Riesgo riesgoGuardado = riesgoRepository.save(riesgo);

        log.info("Riesgo ID {} agregado a la póliza ID {}", riesgoGuardado.getId(), polizaId);

        return riesgoGuardado;
    }
}
