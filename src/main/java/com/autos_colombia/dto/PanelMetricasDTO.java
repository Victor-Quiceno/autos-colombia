package com.autos_colombia.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanelMetricasDTO {

    private long vehiculosPresentes;
    private long celdasCarroDisponibles;
    private long celdasMotoDisponibles;
    private long novedadesAbiertas;
}
