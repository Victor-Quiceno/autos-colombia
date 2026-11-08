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
public class UsuarioCrearDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El usuario debe tener entre 3 y 30 caracteres")
    private String username;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "ROLE_ADMIN|ROLE_OPERARIO", message = "El rol debe ser Administrador u Operario")
    private String rol;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmPassword;
}
