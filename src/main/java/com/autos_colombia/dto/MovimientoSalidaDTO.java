package com.autos_colombia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoSalidaDTO {

    @NotBlank(message = "Debe ingresar o seleccionar una placa")
    private String placa;
}
