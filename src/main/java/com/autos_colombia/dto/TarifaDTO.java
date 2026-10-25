package com.autos_colombia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaDTO {

    private Long id;

    @NotBlank(message = "El tipo de vehículo es obligatorio")
    private String tipoVehiculo; // CARRO, MOTO

    @NotNull(message = "El valor de la mensualidad es obligatorio")
    @Positive(message = "La tarifa debe ser mayor a cero")
    private BigDecimal valorMensualidad;
}
