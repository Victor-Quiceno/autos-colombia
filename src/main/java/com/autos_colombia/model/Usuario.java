package com.autos_colombia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "nombre_completo", nullable = false, length = 80)
    private String nombreCompleto;

    @Column(nullable = false, length = 20)
    private String rol; // ROLE_ADMIN, ROLE_OPERARIO

    @Column(nullable = false, length = 15)
    @Builder.Default
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO
}
