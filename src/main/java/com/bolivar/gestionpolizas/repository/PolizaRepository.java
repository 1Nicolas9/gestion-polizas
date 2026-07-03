package com.bolivar.gestionpolizas.repository;

import com.bolivar.gestionpolizas.model.EstadoPoliza;
import com.bolivar.gestionpolizas.model.Poliza;
import com.bolivar.gestionpolizas.model.TipoPoliza;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PolizaRepository {

    private final Map<Long, Poliza> polizas = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public PolizaRepository() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        Poliza poliza1 = Poliza.builder()
                .id(idGenerator.getAndIncrement())
                .numero("POL-001")
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.ACTIVA)
                .canonMensual(new BigDecimal("150000.00"))
                .prima(new BigDecimal("1800000.00"))
                .fechaInicio(LocalDate.of(2024, 1, 1))
                .fechaFin(LocalDate.of(2025, 1, 1))
                .titular("Juan Carlos Pérez")
                .build();

        Poliza poliza2 = Poliza.builder()
                .id(idGenerator.getAndIncrement())
                .numero("POL-002")
                .tipo(TipoPoliza.COLECTIVA)
                .estado(EstadoPoliza.ACTIVA)
                .canonMensual(new BigDecimal("500000.00"))
                .prima(new BigDecimal("6000000.00"))
                .fechaInicio(LocalDate.of(2024, 3, 15))
                .fechaFin(LocalDate.of(2025, 3, 15))
                .titular("Empresa ABC S.A.S")
                .build();

        Poliza poliza3 = Poliza.builder()
                .id(idGenerator.getAndIncrement())
                .numero("POL-003")
                .tipo(TipoPoliza.INDIVIDUAL)
                .estado(EstadoPoliza.CANCELADA)
                .canonMensual(new BigDecimal("200000.00"))
                .prima(new BigDecimal("2400000.00"))
                .fechaInicio(LocalDate.of(2023, 6, 1))
                .fechaFin(LocalDate.of(2024, 6, 1))
                .titular("María López Rodríguez")
                .build();

        polizas.put(poliza1.getId(), poliza1);
        polizas.put(poliza2.getId(), poliza2);
        polizas.put(poliza3.getId(), poliza3);
    }

    public List<Poliza> findAll() {
        return new ArrayList<>(polizas.values());
    }

    public List<Poliza> findByFiltros(TipoPoliza tipo, EstadoPoliza estado) {
        return polizas.values().stream()
                .filter(p -> tipo == null || p.getTipo() == tipo)
                .filter(p -> estado == null || p.getEstado() == estado)
                .collect(Collectors.toList());
    }

    public Optional<Poliza> findById(Long id) {
        return Optional.ofNullable(polizas.get(id));
    }

    public Poliza save(Poliza poliza) {
        if (poliza.getId() == null) {
            poliza.setId(idGenerator.getAndIncrement());
        }
        polizas.put(poliza.getId(), poliza);
        return poliza;
    }
}
