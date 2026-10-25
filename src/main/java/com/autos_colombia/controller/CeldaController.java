package com.autos_colombia.controller;

import com.autos_colombia.dto.CeldaDTO;
import com.autos_colombia.service.CeldaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/celdas")
public class CeldaController {

    private final CeldaService celdaService;

    public CeldaController(CeldaService celdaService) {
        this.celdaService = celdaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "tipo", defaultValue = "TODOS") String tipo,
                         @RequestParam(value = "estado", defaultValue = "TODOS") String estado,
                         Model model) {
        var celdas = celdaService.obtenerTodas().stream()
                .filter(c -> "TODOS".equalsIgnoreCase(tipo) || c.getTipoPermitido().equalsIgnoreCase(tipo))
                .filter(c -> "TODOS".equalsIgnoreCase(estado) || c.getEstadoCelda().equalsIgnoreCase(estado))
                .toList();

        model.addAttribute("celdas", celdas);
        model.addAttribute("celdaDTO", new CeldaDTO());
        model.addAttribute("filtroTipo", tipo.toUpperCase());
        model.addAttribute("filtroEstado", estado.toUpperCase());
        model.addAttribute("moduloActivo", "celdas");
        return "celdas/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("celdaDTO") CeldaDTO dto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("celdas", celdaService.obtenerTodas());
            model.addAttribute("filtroTipo", "TODOS");
            model.addAttribute("filtroEstado", "TODOS");
            model.addAttribute("moduloActivo", "celdas");
            return "celdas/lista";
        }

        try {
            celdaService.crearCelda(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Celda creada exitosamente con estado Disponible.");
            return "redirect:/celdas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/celdas";
        }
    }

    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam("nuevoEstado") String nuevoEstado,
                                RedirectAttributes redirectAttributes) {
        try {
            celdaService.cambiarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado de la celda actualizado correctamente a " + nuevoEstado + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/celdas";
    }
}
