package com.autos_colombia.controller;

import com.autos_colombia.dto.UsuarioCrearDTO;
import com.autos_colombia.dto.UsuarioEditarDTO;
import com.autos_colombia.dto.UsuarioPasswordDTO;
import com.autos_colombia.model.Usuario;
import com.autos_colombia.service.AuthService;
import com.autos_colombia.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "filtro", defaultValue = "ACTIVO") String filtro, Model model) {
        List<Usuario> usuarios;
        if ("ACTIVO".equalsIgnoreCase(filtro)) {
            usuarios = usuarioService.obtenerPorEstado("ACTIVO");
        } else if ("INACTIVO".equalsIgnoreCase(filtro)) {
            usuarios = usuarioService.obtenerPorEstado("INACTIVO");
        } else {
            usuarios = usuarioService.obtenerTodos();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("filtroActual", filtro.toUpperCase());
        model.addAttribute("usernameActual", authService.getUsernameAutenticado());
        model.addAttribute("moduloActivo", "usuarios");
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioCrearDTO());
        model.addAttribute("moduloActivo", "usuarios");
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuarioDTO") UsuarioCrearDTO dto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("moduloActivo", "usuarios");
            return "usuarios/formulario";
        }

        try {
            usuarioService.crearUsuario(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario '" + dto.getUsername() + "' creado exitosamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("moduloActivo", "usuarios");
            return "usuarios/formulario";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        UsuarioEditarDTO dto = UsuarioEditarDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .build();

        model.addAttribute("usuarioDTO", dto);
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "usuarios");
        return "usuarios/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("usuarioDTO") UsuarioEditarDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuarioService.obtenerPorId(id));
            model.addAttribute("moduloActivo", "usuarios");
            return "usuarios/formulario";
        }

        try {
            String usernameSesion = authService.getUsernameAutenticado();
            usuarioService.actualizarUsuario(id, dto, usernameSesion);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuarioService.obtenerPorId(id));
            model.addAttribute("moduloActivo", "usuarios");
            return "usuarios/formulario";
        }
    }

    @GetMapping("/restablecer/{id}")
    public String restablecerFormulario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        UsuarioPasswordDTO dto = UsuarioPasswordDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .build();

        model.addAttribute("passwordDTO", dto);
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "usuarios");
        return "usuarios/restablecer";
    }

    @PostMapping("/restablecer/{id}")
    public String restablecerGuardar(@PathVariable Long id,
                                    @Valid @ModelAttribute("passwordDTO") UsuarioPasswordDTO dto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuarioService.obtenerPorId(id));
            model.addAttribute("moduloActivo", "usuarios");
            return "usuarios/restablecer";
        }

        try {
            usuarioService.restablecerPassword(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Contraseña restablecida exitosamente para el usuario '" + dto.getUsername() + "'.");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuarioService.obtenerPorId(id));
            model.addAttribute("moduloActivo", "usuarios");
            return "usuarios/restablecer";
        }
    }

    @PostMapping("/inhabilitar/{id}")
    public String inhabilitar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String usernameSesion = authService.getUsernameAutenticado();
            usuarioService.inhabilitarUsuario(id, usernameSesion);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario inhabilitado mediante borrado lógico.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.activarUsuario(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario reactivado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
