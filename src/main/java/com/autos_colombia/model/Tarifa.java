package com.autos_colombia.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tarifas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_vehiculo", nullable = false, unique = true, length = 10)
    private String tipoVehiculo; // CARRO, MOTO

    @Column(name = "valor_mensualidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensualidad;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDate fechaActualizacion;
}
