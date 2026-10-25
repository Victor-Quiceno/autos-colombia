package com.autos_colombia.controller;

import com.autos_colombia.dto.NovedadDTO;
import com.autos_colombia.service.AuthService;
import com.autos_colombia.service.NovedadService;
import com.autos_colombia.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/novedades")
public class NovedadController {

    private final NovedadService novedadService;
    private final VehiculoService vehiculoService;
    private final AuthService authService;

    public NovedadController(NovedadService novedadService,
                             VehiculoService vehiculoService,
                             AuthService authService) {
        this.novedadService = novedadService;
        this.vehiculoService = vehiculoService;
        this.authService = authService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("novedades", novedadService.obtenerTodas());
        model.addAttribute("moduloActivo", "novedades");
        return "novedades/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("novedadDTO", new NovedadDTO());
        model.addAttribute("vehiculosPresentes", vehiculoService.obtenerTodos().stream()
                .filter(v -> "PRESENTE".equalsIgnoreCase(v.getEstadoPresencia()))
                .toList());
        model.addAttribute("moduloActivo", "novedades");
        return "novedades/registro";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("novedadDTO") NovedadDTO dto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vehiculosPresentes", vehiculoService.obtenerTodos().stream()
                    .filter(v -> "PRESENTE".equalsIgnoreCase(v.getEstadoPresencia()))
                    .toList());
            model.addAttribute("moduloActivo", "novedades");
            return "novedades/registro";
        }

        try {
            String operario = authService.getUsernameAutenticado();
            novedadService.registrarNovedad(dto, operario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Novedad registrada exitosamente.");
            return "redirect:/novedades";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vehiculosPresentes", vehiculoService.obtenerTodos().stream()
                    .filter(v -> "PRESENTE".equalsIgnoreCase(v.getEstadoPresencia()))
                    .toList());
            model.addAttribute("moduloActivo", "novedades");
            return "novedades/registro";
        }
    }

    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam("nuevoEstado") String nuevoEstado,
                                RedirectAttributes redirectAttributes) {
        try {
            novedadService.cambiarEstadoNovedad(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado de novedad actualizado a " + nuevoEstado + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/novedades";
    }
}
