package com.bolivar.gestionpolizas.repository;

import com.bolivar.gestionpolizas.model.EstadoRiesgo;
import com.bolivar.gestionpolizas.model.Riesgo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class RiesgoRepository {

    private final Map<Long, Riesgo> riesgos = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public RiesgoRepository() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        Riesgo riesgo1 = Riesgo.builder()
                .id(idGenerator.getAndIncrement())
                .polizaId(1L)
                .descripcion("Riesgo de incendio en propiedad")
                .cobertura("Daños materiales por incendio")
                .estado(EstadoRiesgo.ACTIVO)
                .build();

        Riesgo riesgo2 = Riesgo.builder()
                .id(idGenerator.getAndIncrement())
                .polizaId(2L)
                .descripcion("Riesgo de accidente laboral")
                .cobertura("Accidentes en lugar de trabajo")
                .estado(EstadoRiesgo.ACTIVO)
                .build();

        Riesgo riesgo3 = Riesgo.builder()
                .id(idGenerator.getAndIncrement())
                .polizaId(2L)
                .descripcion("Riesgo de enfermedad profesional")
                .cobertura("Enfermedades derivadas del trabajo")
                .estado(EstadoRiesgo.ACTIVO)
                .build();

        riesgos.put(riesgo1.getId(), riesgo1);
        riesgos.put(riesgo2.getId(), riesgo2);
        riesgos.put(riesgo3.getId(), riesgo3);
    }

    public List<Riesgo> findByPolizaId(Long polizaId) {
        return riesgos.values().stream()
                .filter(r -> r.getPolizaId().equals(polizaId))
                .collect(Collectors.toList());
    }

    public Optional<Riesgo> findById(Long id) {
        return Optional.ofNullable(riesgos.get(id));
    }

    public Riesgo save(Riesgo riesgo) {
        if (riesgo.getId() == null) {
            riesgo.setId(idGenerator.getAndIncrement());
        }
        riesgos.put(riesgo.getId(), riesgo);
        return riesgo;
    }

    public List<Riesgo> saveAll(List<Riesgo> riesgosList) {
        riesgosList.forEach(this::save);
        return riesgosList;
    }

    public long countByPolizaId(Long polizaId) {
        return riesgos.values().stream()
                .filter(r -> r.getPolizaId().equals(polizaId))
                .count();
    }
}
