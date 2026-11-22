package com.autos_colombia.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Adaptador de pasarela de pagos simulada (sandbox).
 * No almacena datos de tarjeta. Recibe el resultado elegido por el operador
 * y devuelve "APROBADO" o "RECHAZADO" para registrar en el historial.
 */
@Service
public class PasarelaPagoService {

    /**
     * Procesa la transacción en modo sandbox.
     *
     * @param monto          Monto total de la mensualidad
     * @param metodoPago     Método seleccionado (PSE, TARJETA_CREDITO, etc.)
     * @param resultadoSimulado "APROBADO" o "RECHAZADO" elegido en el formulario
     * @return               El mismo resultado normalizado a mayúsculas
     */
    public String procesarTransaccion(BigDecimal monto, String metodoPago, String resultadoSimulado) {
        String resultado = (resultadoSimulado != null) ? resultadoSimulado.toUpperCase().trim() : "APROBADO";
        return "RECHAZADO".equals(resultado) ? "RECHAZADO" : "APROBADO";
    }
}
