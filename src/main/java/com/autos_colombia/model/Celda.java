package com.autos_colombia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "celdas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Celda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_celda", nullable = false, unique = true, length = 10)
    private String codigoCelda;

    @Column(name = "tipo_permitido", nullable = false, length = 10)
    private String tipoPermitido; // CARRO, MOTO

    @Column(name = "estado_celda", nullable = false, length = 20)
    @Builder.Default
    private String estadoCelda = "DISPONIBLE"; // DISPONIBLE, ASIGNADA, MANTENIMIENTO
}
