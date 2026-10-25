package com.autos_colombia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {

    @NotNull(message = "Debe seleccionar un dueño")
    private Long idDueno;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago; // PSE, TARJETA_CREDITO, TARJETA_DEBITO, EFECTIVO
}
