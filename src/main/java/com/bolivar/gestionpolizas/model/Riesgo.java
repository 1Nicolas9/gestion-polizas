package com.bolivar.gestionpolizas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Riesgo {

    private Long id;
    private Long polizaId;
    private String descripcion;
    private String cobertura;
    private EstadoRiesgo estado;
}
