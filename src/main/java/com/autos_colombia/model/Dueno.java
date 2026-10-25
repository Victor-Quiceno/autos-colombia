package com.autos_colombia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "duenos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dueno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento; // CC, CE, NIT, PASAPORTE

    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(nullable = false, length = 60)
    private String nombres;

    @Column(nullable = false, length = 60)
    private String apellidos;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "fecha_vinculacion", nullable = false)
    private LocalDate fechaVinculacion;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(nullable = false, length = 15)
    @Builder.Default
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO

    @OneToMany(mappedBy = "dueno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehiculo> vehiculos = new ArrayList<>();

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
