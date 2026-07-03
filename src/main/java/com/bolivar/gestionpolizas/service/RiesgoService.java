package com.bolivar.gestionpolizas.service;

import com.bolivar.gestionpolizas.config.exception.NegocioException;
import com.bolivar.gestionpolizas.config.exception.RecursoNoEncontradoException;
import com.bolivar.gestionpolizas.model.EstadoRiesgo;
import com.bolivar.gestionpolizas.model.Riesgo;
import com.bolivar.gestionpolizas.repository.RiesgoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiesgoService {

    private static final Logger log = LoggerFactory.getLogger(RiesgoService.class);

    private final RiesgoRepository riesgoRepository;

    public RiesgoService(RiesgoRepository riesgoRepository) {
        this.riesgoRepository = riesgoRepository;
    }

    public Riesgo cancelarRiesgo(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Riesgo no encontrado con ID: " + riesgoId));

        if (riesgo.getEstado() == EstadoRiesgo.CANCELADO) {
            throw new NegocioException("El riesgo ya se encuentra en estado CANCELADO");
        }

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        riesgoRepository.save(riesgo);

        log.info("Riesgo ID {} cancelado exitosamente", riesgoId);

        return riesgo;
    }
}
