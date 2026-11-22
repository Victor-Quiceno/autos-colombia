package com.autos_colombia.service;

import com.autos_colombia.model.Dueno;
import com.autos_colombia.repository.DuenoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de tareas programadas (HU09).
 * El "hoy" se obtiene de un Clock inyectado, lo que permite sobrescribirlo en tests
 * sin modificar el código de producción.
 */
@Service
public class ScheduledTasksService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksService.class);

    private final DuenoRepository duenoRepository;
    private final PagoService pagoService;
    private final EmailService emailService;
    private final MoraProcessorService moraProcessor;
    private final Clock clock;

    public ScheduledTasksService(DuenoRepository duenoRepository,
                                 PagoService pagoService,
                                 EmailService emailService,
                                 MoraProcessorService moraProcessor,
                                 Clock clock) {
        this.duenoRepository = duenoRepository;
        this.pagoService = pagoService;
        this.emailService = emailService;
        this.moraProcessor = moraProcessor;
        this.clock = clock;
    }

    // ── Punto de entrada del cron y del endpoint manual ───────────────────────

    /**
     * Ejecutado diariamente a medianoche (00:00:00).
     * También invocable manualmente desde POST /pagos/ejecutar-cron.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void ejecutarVerificacionesDiarias() {
        LocalDate hoy = LocalDate.now(clock);
        log.info("[CRON] Iniciando verificaciones diarias para fecha: {}", hoy);
        verificarRecordatoriosCobro(hoy);
        verificarSuspensionPorMora(hoy);
        log.info("[CRON] Proceso automatizado completado.");
    }

    // ── Recordatorio (HU09 — 3 días antes del corte) ─────────────────────────

    /**
     * Envía recordatorio a dueños ACTIVO cuya fechaCorte == hoy + 3 días
     * y que tengan al menos un vehículo facturable.
     * El correo incluye nombre, fecha de vencimiento, nº de vehículos y monto a cancelar.
     */
    public void verificarRecordatoriosCobro(LocalDate hoy) {
        LocalDate fechaObjetivo = hoy.plusDays(3);
        List<Dueno> porVencer = duenoRepository.findByFechaCorte(fechaObjetivo);

        for (Dueno dueno : porVencer) {
            if (!"ACTIVO".equalsIgnoreCase(dueno.getEstado())) continue;
            try {
                var liq = pagoService.calcularLiquidacion(dueno.getId());
                // calcularLiquidacion lanza si no hay facturables → se captura abajo
                emailService.enviarRecordatorioCobro(
                        dueno,
                        liq.totalPagar(),
                        liq.totalVehiculos()
                );
            } catch (Exception e) {
                log.error("[RECORDATORIO] Error con {}: {}", dueno.getCorreo(), e.getMessage());
            }
        }
    }

    // ── Mora (HU09 — fechaCorte vencida) ─────────────────────────────────────

    /**
     * Inactiva a los dueños ACTIVO con fechaCorte anterior a hoy.
     * - Si el dueño tiene vehículo(s) PRESENTE → se omite y se reintenta mañana.
     * - Cada dueño se procesa en su propia transacción (MoraProcessorService):
     *   el fallo de uno no detiene el lote.
     */
    public void verificarSuspensionPorMora(LocalDate hoy) {
        List<Dueno> morosos = duenoRepository.findByFechaCorteBeforeAndEstado(hoy, "ACTIVO");
        log.info("[MORA] Dueños a evaluar: {}", morosos.size());

        for (Dueno dueno : morosos) {
            try {
                moraProcessor.inactivarPorMora(dueno);
            } catch (Exception e) {
                // Captura tanto el "tiene PRESENTE" como cualquier error técnico inesperado
                log.warn("[MORA] Omitido {}: {}", dueno.getNombreCompleto(), e.getMessage());
            }
        }
    }

    // ── Sobrecarga sin parámetro para compatibilidad con código existente ─────
    public void verificarRecordatoriosCobro() {
        verificarRecordatoriosCobro(LocalDate.now(clock));
    }

    public void verificarSuspensionPorMora() {
        verificarSuspensionPorMora(LocalDate.now(clock));
    }
}
