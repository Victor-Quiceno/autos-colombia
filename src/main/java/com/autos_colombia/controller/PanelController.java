package com.autos_colombia.controller;

import com.autos_colombia.service.PanelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/panel")
public class PanelController {

    private final PanelService panelService;

    public PanelController(PanelService panelService) {
        this.panelService = panelService;
    }

    @GetMapping("/operativo")
    public String panelOperativo(Model model) {
        model.addAttribute("metricas", panelService.obtenerMetricas());
        model.addAttribute("movimientos", panelService.obtenerUltimosMovimientos());
        model.addAttribute("moduloActivo", "panel");
        return "panel/operativo";
    }

    @GetMapping("/administrativo")
    public String panelAdministrativo(Model model) {
        model.addAttribute("metricas", panelService.obtenerMetricas());
        model.addAttribute("movimientos", panelService.obtenerUltimosMovimientos());
        model.addAttribute("novedades", panelService.obtenerNovedadesRecientes());
        model.addAttribute("moduloActivo", "panel");
        return "panel/administrativo";
    }
}
