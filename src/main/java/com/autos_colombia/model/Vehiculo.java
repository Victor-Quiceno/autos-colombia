package com.autos_colombia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(name = "tipo_vehiculo", nullable = false, length = 10)
    private String tipoVehiculo; // CARRO, MOTO

    @Column(nullable = false, length = 40)
    private String marca;

    @Column(nullable = false)
    private Integer modelo;

    @Column(nullable = false, length = 30)
    private String color;

    @Column(name = "fecha_vinculacion", nullable = false)
    private LocalDate fechaVinculacion;

    @Column(nullable = false, length = 15)
    @Builder.Default
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO

    @Column(name = "estado_presencia", nullable = false, length = 15)
    @Builder.Default
    private String estadoPresencia = "AUSENTE"; // PRESENTE, AUSENTE

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_dueno", nullable = false)
    private Dueno dueno;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_celda", nullable = false, unique = true)
    private Celda celda;
}
