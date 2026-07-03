package com.bolivar.gestionpolizas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CoreMockService {

    private static final Logger log = LoggerFactory.getLogger(CoreMockService.class);

    public void notificarEvento(String evento, Long polizaId) {
        log.info("[CORE-MOCK] Evento '{}' enviado al CORE para la póliza con ID: {}. Operación registrada exitosamente.", evento, polizaId);
    }
}
