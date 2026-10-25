package com.autos_colombia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeldaDTO {

    private Long id;

    @NotBlank(message = "El código de la celda es obligatorio")
    private String codigoCelda;

    @NotBlank(message = "El tipo permitido es obligatorio")
    private String tipoPermitido; // CARRO, MOTO

    private String estadoCelda; // DISPONIBLE, ASIGNADA, MANTENIMIENTO
}
