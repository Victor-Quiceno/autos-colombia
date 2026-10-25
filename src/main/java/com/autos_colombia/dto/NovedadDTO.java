package com.autos_colombia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadDTO {

    private Long id;

    @NotBlank(message = "La placa del vehículo es obligatoria")
    private String placa;

    @NotBlank(message = "El tipo de novedad es obligatorio")
    private String tipoNovedad; // RAYON, GOLPE, FUGA, OBJETO_OLVIDADO, OTRO

    @NotBlank(message = "La descripción de la novedad es obligatoria")
    private String descripcion;

    @NotBlank(message = "El nivel de severidad es obligatorio")
    private String nivelSeveridad; // BAJA, MEDIA, ALTA

    private String estadoNovedad;
}
