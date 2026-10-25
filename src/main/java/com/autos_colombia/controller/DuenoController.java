package com.autos_colombia.controller;

import com.autos_colombia.dto.DuenoDTO;
import com.autos_colombia.model.Dueno;
import com.autos_colombia.service.DuenoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/duenos")
public class DuenoController {

    private final DuenoService duenoService;

    public DuenoController(DuenoService duenoService) {
        this.duenoService = duenoService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "filtro", defaultValue = "TODOS") String filtro, Model model) {
        if ("ACTIVO".equalsIgnoreCase(filtro)) {
            model.addAttribute("duenos", duenoService.obtenerPorEstado("ACTIVO"));
        } else if ("INACTIVO".equalsIgnoreCase(filtro)) {
            model.addAttribute("duenos", duenoService.obtenerPorEstado("INACTIVO"));
        } else {
            model.addAttribute("duenos", duenoService.obtenerTodos());
        }
        model.addAttribute("filtroActual", filtro.toUpperCase());
        model.addAttribute("moduloActivo", "duenos");
        return "duenos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("duenoDTO", new DuenoDTO());
        model.addAttribute("moduloActivo", "duenos");
        return "duenos/registro";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("duenoDTO") DuenoDTO dto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("moduloActivo", "duenos");
            return "duenos/registro";
        }

        try {
            duenoService.registrarDueno(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Dueño registrado exitosamente con estado Activo.");
            return "redirect:/duenos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("moduloActivo", "duenos");
            return "duenos/registro";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Dueno dueno = duenoService.obtenerPorId(id);
        DuenoDTO dto = DuenoDTO.builder()
                .id(dueno.getId())
                .tipoDocumento(dueno.getTipoDocumento())
                .numeroDocumento(dueno.getNumeroDocumento())
                .nombres(dueno.getNombres())
                .apellidos(dueno.getApellidos())
                .telefono(dueno.getTelefono())
                .correo(dueno.getCorreo())
                .fechaVinculacion(dueno.getFechaVinculacion())
                .fechaCorte(dueno.getFechaCorte())
                .estado(dueno.getEstado())
                .build();

        model.addAttribute("duenoDTO", dto);
        model.addAttribute("moduloActivo", "duenos");
        return "duenos/editar";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("duenoDTO") DuenoDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("moduloActivo", "duenos");
            return "duenos/editar";
        }

        try {
            duenoService.actualizarDueno(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Datos del dueño actualizados correctamente.");
            return "redirect:/duenos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("moduloActivo", "duenos");
            return "duenos/editar";
        }
    }

    @PostMapping("/inhabilitar/{id}")
    public String inhabilitar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            duenoService.inhabilitarDueno(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Dueño y vehículos asociados inhabilitados mediante borrado lógico.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/duenos";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            duenoService.activarDueno(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Dueño y vehículos asociados reactivados exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/duenos";
    }
}
