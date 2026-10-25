package com.autos_colombia.service;

import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.DuenoRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ScheduledTasksService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksService.class);

    private final DuenoRepository duenoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PagoService pagoService;
    private final EmailService emailService;

    public ScheduledTasksService(DuenoRepository duenoRepository,
                                 VehiculoRepository vehiculoRepository,
                                 PagoService pagoService,
                                 EmailService emailService) {
        this.duenoRepository = duenoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.pagoService = pagoService;
        this.emailService = emailService;
    }

    /**
     * Tarea programada que se ejecuta diariamente a la medianoche (00:00:00)
     * También puede invocarse manualmente para demostración académica.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void ejecutarVerificacionesDiarias() {
        log.info("[CRON] Iniciando proceso automatizado de verificación de cortes y vencimientos...");
        verificarRecordatoriosCobro();
        verificarSuspensionPorMora();
        log.info("[CRON] Proceso automatizado completado con éxito.");
    }

    public void verificarRecordatoriosCobro() {
        LocalDate fechaObjetivo = LocalDate.now().plusDays(3); // 3 días calendario antes
        List<Dueno> dueñosPorVencer = duenoRepository.findByFechaCorte(fechaObjetivo);

        for (Dueno dueno : dueñosPorVencer) {
            if ("ACTIVO".equalsIgnoreCase(dueno.getEstado())) {
                try {
                    var liq = pagoService.calcularLiquidacion(dueno.getId());
                    if (liq.totalVehiculos() > 0) {
                        emailService.enviarRecordatorioCobro(dueno, liq.totalPagar());
                    }
                } catch (Exception e) {
                    log.error("Error al despachar recordatorio a {}: {}", dueno.getCorreo(), e.getMessage());
                }
            }
        }
    }

    public void verificarSuspensionPorMora() {
        LocalDate hoy = LocalDate.now();
        List<Dueno> morosos = duenoRepository.findByFechaCorteBeforeAndEstado(hoy, "ACTIVO");

        for (Dueno dueno : morosos) {
            log.warn("[MORA] Inactivando cliente por vencimiento de mensualidad: {}", dueno.getNombreCompleto());
            dueno.setEstado("INACTIVO");
            duenoRepository.save(dueno);

            List<Vehiculo> vehiculos = vehiculoRepository.findByDuenoId(dueno.getId());
            for (Vehiculo v : vehiculos) {
                v.setEstado("INACTIVO");
                vehiculoRepository.save(v);
            }
        }
    }
}
