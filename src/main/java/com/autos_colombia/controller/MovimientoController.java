package com.autos_colombia.controller;

import com.autos_colombia.dto.MovimientoEntradaDTO;
import com.autos_colombia.dto.MovimientoSalidaDTO;
import com.autos_colombia.model.Movimiento;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.service.AuthService;
import com.autos_colombia.service.MovimientoService;
import com.autos_colombia.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final VehiculoService vehiculoService;
    private final AuthService authService;

    public MovimientoController(MovimientoService movimientoService,
                                VehiculoService vehiculoService,
                                AuthService authService) {
        this.movimientoService = movimientoService;
        this.vehiculoService = vehiculoService;
        this.authService = authService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("movimientos", movimientoService.obtenerTodos());
        model.addAttribute("moduloActivo", "movimientos");
        return "movimientos/lista";
    }

    @GetMapping("/entrada")
    public String formularioEntrada(Model model) {
        model.addAttribute("entradaDTO", new MovimientoEntradaDTO());
        model.addAttribute("moduloActivo", "entrada");
        return "movimientos/entrada";
    }

    @PostMapping("/entrada/confirmar")
    public String confirmarEntrada(@Valid @ModelAttribute("entradaDTO") MovimientoEntradaDTO dto,
                                   BindingResult bindingResult,
                                   Model model) {
        model.addAttribute("moduloActivo", "entrada");
        if (bindingResult.hasErrors()) {
            return "movimientos/entrada";
        }

        try {
            String operario = authService.getUsernameAutenticado();
            Movimiento mov = movimientoService.registrarEntrada(dto, operario);
            model.addAttribute("movimientoGenerado", mov);
            model.addAttribute("mensajeExito", "Ingreso confirmado exitosamente.");
            model.addAttribute("vehiculo", mov.getVehiculo());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // Intenta cargar el vehículo si existe para mostrar la caja de validaciones
            try {
                Vehiculo v = vehiculoService.obtenerPorPlaca(dto.getPlaca());
                model.addAttribute("vehiculoConsultado", v);
            } catch (Exception ignored) {}
        }

        return "movimientos/entrada";
    }

    @GetMapping("/salida")
    public String formularioSalida(Model model) {
        model.addAttribute("salidaDTO", new MovimientoSalidaDTO());
        model.addAttribute("moduloActivo", "salida");
        return "movimientos/salida";
    }

    @PostMapping("/salida/confirmar")
    public String confirmarSalida(@Valid @ModelAttribute("salidaDTO") MovimientoSalidaDTO dto,
                                  BindingResult bindingResult,
                                  Model model) {
        model.addAttribute("moduloActivo", "salida");
        if (bindingResult.hasErrors()) {
            return "movimientos/salida";
        }

        try {
            String operario = authService.getUsernameAutenticado();
            Movimiento mov = movimientoService.registrarSalida(dto, operario);
            model.addAttribute("movimientoGenerado", mov);
            model.addAttribute("mensajeExito", "Salida confirmada exitosamente.");
            model.addAttribute("vehiculo", mov.getVehiculo());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            try {
                Vehiculo v = vehiculoService.obtenerPorPlaca(dto.getPlaca());
                model.addAttribute("vehiculoConsultado", v);
            } catch (Exception ignored) {}
        }

        return "movimientos/salida";
    }
}
