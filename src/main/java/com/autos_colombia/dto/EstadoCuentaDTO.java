package com.autos_colombia.dto;

import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Pago;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EstadoCuentaDTO {
    private Dueno dueno;
    private LocalDate fechaCorte;
    private long diasRestantes;
    private BigDecimal montoPagar;
    private int vehiculosFacturables;
    private Pago ultimoPagoAprobado;
    private String estadoCuenta; // "Al día", "Por vencer", "En mora", "Inactivo"
    private String estadoClaseBadge; // "ok", "wr", "er", "no"
}
