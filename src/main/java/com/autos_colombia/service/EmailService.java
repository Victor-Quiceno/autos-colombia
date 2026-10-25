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

    public void enviarRecordatorioCobro(Dueno dueno, BigDecimal montoTotal) {
        String asunto = "Aviso de pago próximo a vencer - Parqueadero Autos Colombia";
        String mensaje = String.format(
                "Estimado(a) %s,\n\n" +
                "Le recordamos que su mensualidad en el Parqueadero Autos Colombia vencerá el próximo %s.\n" +
                "El valor total a cancelar por sus vehículos vinculados es de: $%s COP.\n\n" +
                "Por favor realice su pago oportunamente a través de nuestra pasarela para evitar la suspensión del servicio.\n\n" +
                "Cordialmente,\nEquipo Autos Colombia.",
                dueno.getNombreCompleto(),
                dueno.getFechaCorte(),
                montoTotal
        );

        log.info("[SMTP SIMULADO] Enviando correo a: {} | Asunto: {}", dueno.getCorreo(), asunto);
        ultimosCorreos.add(0, new NotificacionCorreo(dueno.getCorreo(), asunto, mensaje, LocalDateTime.now()));
    }

    public List<NotificacionCorreo> getUltimosCorreos() {
        return Collections.unmodifiableList(ultimosCorreos);
    }
}
