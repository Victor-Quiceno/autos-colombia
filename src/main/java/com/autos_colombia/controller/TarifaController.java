package com.autos_colombia.controller;

import com.autos_colombia.dto.TarifaDTO;
import com.autos_colombia.service.TarifaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tarifas")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    public String gestion(Model model) {
        model.addAttribute("tarifas", tarifaService.obtenerTodas());
        model.addAttribute("tarifaDTO", new TarifaDTO());
        model.addAttribute("moduloActivo", "tarifas");
        return "tarifas/gestion";
    }

    @PostMapping("/actualizar")
    public String actualizar(@Valid @ModelAttribute("tarifaDTO") TarifaDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tarifas", tarifaService.obtenerTodas());
            model.addAttribute("moduloActivo", "tarifas");
            return "tarifas/gestion";
        }

        try {
            tarifaService.actualizarTarifa(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tarifa de " + dto.getTipoVehiculo() + " actualizada exitosamente.");
            return "redirect:/tarifas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/tarifas";
        }
    }
}
