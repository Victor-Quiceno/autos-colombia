package com.autos_colombia.service;

import com.autos_colombia.dto.PagoDTO;
import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Pago;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.DuenoRepository;
import com.autos_colombia.repository.PagoRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final DuenoRepository duenoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TarifaService tarifaService;
    private final PasarelaPagoService pasarela;

    public PagoService(PagoRepository pagoRepository,
                       DuenoRepository duenoRepository,
                       VehiculoRepository vehiculoRepository,
                       TarifaService tarifaService,
                       PasarelaPagoService pasarela) {
        this.pagoRepository = pagoRepository;
        this.duenoRepository = duenoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.tarifaService = tarifaService;
        this.pasarela = pasarela;
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAllByOrderByFechaPagoDesc();
    }

    public record LiquidacionMensualidad(
            Dueno dueno,
            long cantidadCarros,
            long cantidadMotos,
            BigDecimal tarifaCarro,
            BigDecimal tarifaMoto,
            BigDecimal subtotalCarros,
            BigDecimal subtotalMotos,
            BigDecimal totalPagar,
            int totalVehiculos
    ) {}

    @Transactional(readOnly = true)
    public LiquidacionMensualidad calcularLiquidacion(Long idDueno) {
        Dueno dueno = duenoRepository.findById(idDueno)
                .orElseThrow(() -> new IllegalArgumentException("Dueño no encontrado con ID: " + idDueno));

        List<Vehiculo> vehiculos = vehiculoRepository.findByDuenoId(idDueno);

        long carros = vehiculos.stream().filter(v -> "CARRO".equalsIgnoreCase(v.getTipoVehiculo())).count();
        long motos = vehiculos.stream().filter(v -> "MOTO".equalsIgnoreCase(v.getTipoVehiculo())).count();

        BigDecimal tarifaCarro = tarifaService.obtenerValorTarifa("CARRO");
        BigDecimal tarifaMoto = tarifaService.obtenerValorTarifa("MOTO");

        BigDecimal subtotalCarros = tarifaCarro.multiply(BigDecimal.valueOf(carros));
        BigDecimal subtotalMotos = tarifaMoto.multiply(BigDecimal.valueOf(motos));
        BigDecimal total = subtotalCarros.add(subtotalMotos);

        return new LiquidacionMensualidad(
                dueno, carros, motos, tarifaCarro, tarifaMoto, subtotalCarros, subtotalMotos, total, vehiculos.size()
        );
    }

    /**
     * Genera una referencia de transacción única con prefijo según resultado.
     * PAGO-yyyyMMdd-XXXXXX (aprobado) · RECH-yyyyMMdd-XXXXXX (rechazado)
     */
    private String generarReferencia(String estado) {
        String prefijo = "RECHAZADO".equals(estado) ? "RECH" : "PAGO";
        return prefijo + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Guarda el intento rechazado en su propia transacción independiente
     * para que no sufra rollback si la transacción padre falla.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pago guardarIntento(Dueno dueno, BigDecimal monto, int cantVehiculos,
                               String metodoPago, String estado) {
        Pago pago = Pago.builder()
                .dueno(dueno)
                .fechaPago(LocalDateTime.now())
                .montoTotal(monto)
                .cantidadVehiculos(cantVehiculos)
                .referenciaTransaccion(generarReferencia(estado))
                .metodoPago(metodoPago.toUpperCase())
                .estadoPago(estado)
                .build();
        return pagoRepository.save(pago);
    }

    public Pago procesarPago(PagoDTO dto) {
        LiquidacionMensualidad liq = calcularLiquidacion(dto.getIdDueno());
        Dueno dueno = liq.dueno();

        if (liq.totalVehiculos() == 0) {
            throw new IllegalStateException("El cliente no tiene vehículos vinculados para liquidar mensualidad.");
        }

        // Consultar pasarela simulada con el resultado elegido por el operador
        String estadoPago = pasarela.procesarTransaccion(
                liq.totalPagar(),
                dto.getMetodoPago(),
                dto.getResultadoSimulado()
        );

        if ("RECHAZADO".equals(estadoPago)) {
            // Guardar intento en transacción independiente (no hace rollback)
            // No tocar fechaCorte, estado del dueño ni de los vehículos
            Pago intento = guardarIntento(dueno, liq.totalPagar(), liq.totalVehiculos(),
                    dto.getMetodoPago(), "RECHAZADO");
            throw new PagoRechazadoException(
                    "Pago rechazado por la pasarela. Referencia del intento: " + intento.getReferenciaTransaccion(),
                    intento
            );
        }

        // --- APROBADO: comportamiento original ---
        String ref = generarReferencia("APROBADO");

        // HU08: sumar 1 mes a fecha_corte
        dueno.setFechaCorte(dueno.getFechaCorte().plusMonths(1));

        // HU08: reactivar dueño e vehículos si estaban inactivos
        if ("INACTIVO".equalsIgnoreCase(dueno.getEstado())) {
            dueno.setEstado("ACTIVO");
            List<Vehiculo> vehiculos = vehiculoRepository.findByDuenoId(dueno.getId());
            for (Vehiculo v : vehiculos) {
                v.setEstado("ACTIVO");
                vehiculoRepository.save(v);
            }
        }
        duenoRepository.save(dueno);

        Pago pago = Pago.builder()
                .dueno(dueno)
                .fechaPago(LocalDateTime.now())
                .montoTotal(liq.totalPagar())
                .cantidadVehiculos(liq.totalVehiculos())
                .referenciaTransaccion(ref)
                .metodoPago(dto.getMetodoPago().toUpperCase())
                .estadoPago("APROBADO")
                .build();

        return pagoRepository.save(pago);
    }

    // ── Excepción interna que lleva el Pago rechazado ya guardado ──────────────
    public static class PagoRechazadoException extends RuntimeException {
        private final Pago pago;
        public PagoRechazadoException(String msg, Pago pago) {
            super(msg);
            this.pago = pago;
        }
        public Pago getPago() { return pago; }
    }
}
