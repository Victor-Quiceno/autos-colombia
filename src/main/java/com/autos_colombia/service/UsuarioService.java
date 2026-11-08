package com.autos_colombia.service;

import com.autos_colombia.dto.UsuarioCrearDTO;
import com.autos_colombia.dto.UsuarioEditarDTO;
import com.autos_colombia.dto.UsuarioPasswordDTO;
import com.autos_colombia.model.Usuario;
import com.autos_colombia.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerPorEstado(String estado) {
        return usuarioRepository.findByEstado(estado.toUpperCase());
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    public Usuario crearUsuario(UsuarioCrearDTO dto) {
        String username = dto.getUsername().trim();

        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario '" + username + "' ya existe en el sistema.");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("La confirmación de la contraseña no coincide.");
        }

        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombreCompleto(dto.getNombreCompleto().trim())
                .rol(dto.getRol())
                .estado("ACTIVO")
                .build();

        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(Long id, UsuarioEditarDTO dto, String usernameEnSesion) {
        Usuario usuario = obtenerPorId(id);

        // HU13 Criterio 7: No quitar el rol al último administrador activo
        boolean eraAdminActivo = "ROLE_ADMIN".equalsIgnoreCase(usuario.getRol()) && "ACTIVO".equalsIgnoreCase(usuario.getEstado());
        boolean cambiaAOperario = "ROLE_OPERARIO".equalsIgnoreCase(dto.getRol());

        if (eraAdminActivo && cambiaAOperario) {
            long totalAdminsActivos = usuarioRepository.countByRolAndEstado("ROLE_ADMIN", "ACTIVO");
            if (totalAdminsActivos <= 1) {
                throw new IllegalStateException("No se puede quitar el rol de administrador al único administrador activo del sistema.");
            }
        }

        usuario.setNombreCompleto(dto.getNombreCompleto().trim());
        usuario.setRol(dto.getRol());

        return usuarioRepository.save(usuario);
    }

    public void restablecerPassword(Long id, UsuarioPasswordDTO dto) {
        Usuario usuario = obtenerPorId(id);

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("La confirmación de la contraseña no coincide.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioRepository.save(usuario);
    }

    public void inhabilitarUsuario(Long id, String usernameEnSesion) {
        Usuario usuario = obtenerPorId(id);

        // HU13 Criterio 7 / T6: Un administrador no puede inhabilitarse a sí mismo
        if (usuario.getUsername().equalsIgnoreCase(usernameEnSesion)) {
            throw new IllegalStateException("No puedes inhabilitar tu propio usuario en sesión.");
        }

        // HU13 Criterio 7 / T6: No inhabilitar al último administrador activo
        if ("ROLE_ADMIN".equalsIgnoreCase(usuario.getRol()) && "ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            long totalAdminsActivos = usuarioRepository.countByRolAndEstado("ROLE_ADMIN", "ACTIVO");
            if (totalAdminsActivos <= 1) {
                throw new IllegalStateException("No se puede inhabilitar al único administrador activo del sistema.");
            }
        }

        usuario.setEstado("INACTIVO");
        usuarioRepository.save(usuario);
    }

    public void activarUsuario(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);
    }
}
