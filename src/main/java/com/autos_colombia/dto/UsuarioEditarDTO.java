package com.autos_colombia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEditarDTO {

    private Long id;

    private String username;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "ROLE_ADMIN|ROLE_OPERARIO", message = "El rol debe ser Administrador u Operario")
    private String rol;

    private String estado;
}

