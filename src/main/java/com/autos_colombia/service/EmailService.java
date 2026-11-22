package com.autos_colombia.service;

import com.autos_colombia.model.Dueno;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    // Almacenamiento en memoria de las notificaciones enviadas para trazabilidad y pruebas
    public record NotificacionCorreo(String destinatario, String asunto, String mensaje, LocalDateTime fechaEnvio) {}
    private final List<NotificacionCorreo> ultimosCorreos = Collections.synchronizedList(new ArrayList<>());

    /**
     * Envía (simulado) un recordatorio de cobro próximo a vencer.
     * Incluye nombre del dueño, fecha de vencimiento, nº de vehículos y monto a cancelar.
     *
     * @param dueno         Dueño destinatario
     * @param montoTotal    Monto calculado con vehículos facturables (T11)
     * @param nVehiculos    Número de vehículos facturables incluidos en el monto
     */
    public void enviarRecordatorioCobro(Dueno dueno, BigDecimal montoTotal, int nVehiculos) {
        String asunto = "Aviso de pago próximo a vencer - Parqueadero Autos Colombia";
        String mensaje = String.format(
                "Estimado(a) %s,%n%n" +
                "Le recordamos que su mensualidad en el Parqueadero Autos Colombia vencerá el %s.%n" +
                "Vehículos incluidos en la liquidación: %d%n" +
                "Valor total a cancelar: $%s COP.%n%n" +
                "Realice su pago oportunamente a través de nuestra pasarela para evitar la suspensión del servicio.%n%n" +
                "Cordialmente,%nEquipo Autos Colombia.",
                dueno.getNombreCompleto(),
                dueno.getFechaCorte(),
                nVehiculos,
                montoTotal
        );

        log.info("[SMTP SIMULADO] Correo a: {} | Asunto: {}", dueno.getCorreo(), asunto);
        ultimosCorreos.add(0, new NotificacionCorreo(dueno.getCorreo(), asunto, mensaje, LocalDateTime.now()));
    }

    public List<NotificacionCorreo> getUltimosCorreos() {
        return Collections.unmodifiableList(ultimosCorreos);
    }
}
