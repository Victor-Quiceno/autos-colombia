package com.autos_colombia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "novedades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_novedad", nullable = false, length = 40)
    private String tipoNovedad; // RAYON, GOLPE, FUGA, OBJETO_OLVIDADO, OTRO

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "nivel_severidad", nullable = false, length = 15)
    private String nivelSeveridad; // BAJA, MEDIA, ALTA

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estado_novedad", nullable = false, length = 15)
    @Builder.Default
    private String estadoNovedad = "ABIERTA"; // ABIERTA, ATENDIDA, CERRADA
}
