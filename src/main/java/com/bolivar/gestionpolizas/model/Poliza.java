package com.bolivar.gestionpolizas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Poliza {

    private Long id;
    private String numero;
    private TipoPoliza tipo;
    private EstadoPoliza estado;
    private BigDecimal canonMensual;
    private BigDecimal prima;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String titular;
}
