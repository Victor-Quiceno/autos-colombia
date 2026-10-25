package com.autos_colombia.controller;

import com.autos_colombia.dto.PagoDTO;
import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Pago;
import com.autos_colombia.service.DuenoService;
import com.autos_colombia.service.EmailService;
import com.autos_colombia.service.PagoService;
import com.autos_colombia.service.ScheduledTasksService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final DuenoService duenoService;
    private final EmailService emailService;
    private final ScheduledTasksService scheduledTasksService;

    public PagoController(PagoService pagoService,
                          DuenoService duenoService,
                          EmailService emailService,
                          ScheduledTasksService scheduledTasksService) {
        this.pagoService = pagoService;
        this.duenoService = duenoService;
        this.emailService = emailService;
        this.scheduledTasksService = scheduledTasksService;
    }

    @GetMapping
    public String pagoMensualidad(@RequestParam(value = "idDueno", required = false) Long idDueno, Model model) {
        List<Dueno> duenos = duenoService.obtenerTodos();
        model.addAttribute("duenos", duenos);
        model.addAttribute("pagoDTO", new PagoDTO());

        Long seleccionado = idDueno != null ? idDueno : (duenos.isEmpty() ? null : duenos.get(0).getId());
        model.addAttribute("idDuenoSeleccionado", seleccionado);

        if (seleccionado != null) {
            try {
                var liquidacion = pagoService.calcularLiquidacion(seleccionado);
                model.addAttribute("liq", liquidacion);
            } catch (Exception e) {
                model.addAttribute("errorLiquidacion", e.getMessage());
            }
        }

        model.addAttribute("moduloActivo", "pagos");
        return "pagos/registro";
    }

    @PostMapping("/procesar")
    public String procesarPago(@Valid @ModelAttribute("pagoDTO") PagoDTO dto,
                               BindingResult bindingResult,
                               Model model) {
        model.addAttribute("duenos", duenoService.obtenerTodos());
        model.addAttribute("idDuenoSeleccionado", dto.getIdDueno());
        model.addAttribute("moduloActivo", "pagos");

        if (bindingResult.hasErrors()) {
            return "pagos/registro";
        }

        try {
            Pago pago = pagoService.procesarPago(dto);
            model.addAttribute("comprobante", pago);
            model.addAttribute("mensajeExito", "Pago aprobado: dueño y vehículos reactivados.");
            model.addAttribute("liq", pagoService.calcularLiquidacion(dto.getIdDueno()));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            try {
                model.addAttribute("liq", pagoService.calcularLiquidacion(dto.getIdDueno()));
            } catch (Exception ignored) {}
        }

        return "pagos/registro";
    }

    @GetMapping("/historial")
    public String historial(Model model) {
        model.addAttribute("pagos", pagoService.obtenerTodos());
        model.addAttribute("moduloActivo", "pagos");
        return "pagos/historial";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Model model) {
        model.addAttribute("correos", emailService.getUltimosCorreos());
        model.addAttribute("moduloActivo", "pagos");
        return "pagos/notificaciones";
    }

    @PostMapping("/ejecutar-cron")
    public String ejecutarCron(RedirectAttributes redirectAttributes) {
        try {
            scheduledTasksService.ejecutarVerificacionesDiarias();
            redirectAttributes.addFlashAttribute("mensajeExito", "Verificación diaria ejecutada: se despacharon recordatorios de pago y se actualizaron estados por mora.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/pagos/notificaciones";
    }
}
