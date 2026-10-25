package com.autos_colombia.controller;

import com.autos_colombia.dto.VehiculoDTO;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.service.CeldaService;
import com.autos_colombia.service.DuenoService;
import com.autos_colombia.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;
    private final DuenoService duenoService;
    private final CeldaService celdaService;

    public VehiculoController(VehiculoService vehiculoService,
                              DuenoService duenoService,
                              CeldaService celdaService) {
        this.vehiculoService = vehiculoService;
        this.duenoService = duenoService;
        this.celdaService = celdaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "filtro", defaultValue = "TODOS") String filtro, Model model) {
        if ("ACTIVO".equalsIgnoreCase(filtro)) {
            model.addAttribute("vehiculos", vehiculoService.obtenerPorEstado("ACTIVO"));
        } else if ("INACTIVO".equalsIgnoreCase(filtro)) {
            model.addAttribute("vehiculos", vehiculoService.obtenerPorEstado("INACTIVO"));
        } else {
            model.addAttribute("vehiculos", vehiculoService.obtenerTodos());
        }
        model.addAttribute("filtroActual", filtro.toUpperCase());
        model.addAttribute("moduloActivo", "vehiculos");
        return "vehiculos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("vehiculoDTO", new VehiculoDTO());
        model.addAttribute("duenosActivos", duenoService.obtenerPorEstado("ACTIVO"));
        model.addAttribute("celdasCarro", celdaService.obtenerDisponiblesPorTipo("CARRO"));
        model.addAttribute("celdasMoto", celdaService.obtenerDisponiblesPorTipo("MOTO"));
        model.addAttribute("moduloActivo", "vehiculos");
        return "vehiculos/registro";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("vehiculoDTO") VehiculoDTO dto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("duenosActivos", duenoService.obtenerPorEstado("ACTIVO"));
            model.addAttribute("celdasCarro", celdaService.obtenerDisponiblesPorTipo("CARRO"));
            model.addAttribute("celdasMoto", celdaService.obtenerDisponiblesPorTipo("MOTO"));
            model.addAttribute("moduloActivo", "vehiculos");
            return "vehiculos/registro";
        }

        try {
            vehiculoService.registrarVehiculo(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Vehículo registrado exitosamente con celda exclusiva asignada.");
            return "redirect:/vehiculos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("duenosActivos", duenoService.obtenerPorEstado("ACTIVO"));
            model.addAttribute("celdasCarro", celdaService.obtenerDisponiblesPorTipo("CARRO"));
            model.addAttribute("celdasMoto", celdaService.obtenerDisponiblesPorTipo("MOTO"));
            model.addAttribute("moduloActivo", "vehiculos");
            return "vehiculos/registro";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Vehiculo vehiculo = vehiculoService.obtenerPorId(id);
        VehiculoDTO dto = VehiculoDTO.builder()
                .id(vehiculo.getId())
                .placa(vehiculo.getPlaca())
                .tipoVehiculo(vehiculo.getTipoVehiculo())
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .color(vehiculo.getColor())
                .idDueno(vehiculo.getDueno().getId())
                .idCelda(vehiculo.getCelda() != null ? vehiculo.getCelda().getId() : null)
                .fechaVinculacion(vehiculo.getFechaVinculacion())
                .estado(vehiculo.getEstado())
                .estadoPresencia(vehiculo.getEstadoPresencia())
                .build();

        model.addAttribute("vehiculoDTO", dto);
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("moduloActivo", "vehiculos");
        return "vehiculos/editar";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("vehiculoDTO") VehiculoDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vehiculo", vehiculoService.obtenerPorId(id));
            model.addAttribute("moduloActivo", "vehiculos");
            return "vehiculos/editar";
        }

        try {
            vehiculoService.actualizarVehiculo(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Vehículo actualizado exitosamente.");
            return "redirect:/vehiculos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vehiculo", vehiculoService.obtenerPorId(id));
            model.addAttribute("moduloActivo", "vehiculos");
            return "vehiculos/editar";
        }
    }

    @PostMapping("/inhabilitar/{id}")
    public String inhabilitar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vehiculoService.inhabilitarVehiculo(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Vehículo inhabilitado. La celda ha sido liberada a Disponible.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/vehiculos";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vehiculoService.activarVehiculo(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Vehículo reactivado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/vehiculos";
    }
}
