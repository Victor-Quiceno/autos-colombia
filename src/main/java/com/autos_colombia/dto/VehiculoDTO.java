package com.autos_colombia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoDTO {

    private Long id;

    @NotBlank(message = "La placa es obligatoria")
    private String placa;

    @NotBlank(message = "El tipo de vehículo es obligatorio")
    private String tipoVehiculo; // CARRO, MOTO

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotNull(message = "El modelo (año) es obligatorio")
    private Integer modelo;

    @NotBlank(message = "El color es obligatorio")
    private String color;

    @NotNull(message = "Debe asociar un dueño")
    private Long idDueno;

    @NotNull(message = "Debe asignar una celda")
    private Long idCelda;

    private LocalDate fechaVinculacion;
    private String estado;
    private String estadoPresencia;
}
