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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<Pago> filtrarHistorial(Long idDueno, String estadoPago, LocalDate fechaInicio, LocalDate fechaFin) {
        return pagoRepository.findAllByOrderByFechaPagoDesc().stream()
                .filter(p -> idDueno == null || (p.getDueno() != null && idDueno.equals(p.getDueno().getId())))
                .filter(p -> estadoPago == null || estadoPago.isBlank() || estadoPago.equalsIgnoreCase(p.getEstadoPago()))
                .filter(p -> fechaInicio == null || !p.getFechaPago().toLocalDate().isBefore(fechaInicio))
                .filter(p -> fechaFin == null || !p.getFechaPago().toLocalDate().isAfter(fechaFin))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.autos_colombia.dto.EstadoCuentaDTO> obtenerEstadosCuenta() {
        LocalDate hoy = LocalDate.now();
        List<Dueno> duenos = duenoRepository.findAll();

        return duenos.stream().map(d -> {
            LocalDate corte = d.getFechaCorte();
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, corte);

            BigDecimal monto = BigDecimal.ZERO;
            int vehiculosFacturables = 0;
            try {
                LiquidacionMensualidad liq = calcularLiquidacion(d.getId());
                monto = liq.totalPagar();
                vehiculosFacturables = liq.totalVehiculos();
            } catch (Exception ignored) {
                // Si no tiene facturables o falta tarifa, monto es 0
            }

            Pago ultimoAprobado = pagoRepository.findFirstByDuenoIdAndEstadoPagoOrderByFechaPagoDesc(d.getId(), "APROBADO")
                    .orElse(null);

            // Estado de cuenta:
            // Al día: ACTIVO y faltan más de 3 días (diasRestantes > 3)
            // Por vencer: ACTIVO y faltan 0–3 días (diasRestantes >= 0 && diasRestantes <= 3)
            // En mora: INACTIVO y fechaCorte < hoy (diasRestantes < 0)
            // Inactivo: INACTIVO con corte no vencido (diasRestantes >= 0, p. ej. inhabilitado manualmente)
            String estadoCuenta;
            String badgeClass;

            if ("ACTIVO".equalsIgnoreCase(d.getEstado())) {
                if (diasRestantes > 3) {
                    estadoCuenta = "Al día";
                    badgeClass = "ok";
                } else {
                    estadoCuenta = "Por vencer";
                    badgeClass = "wr";
                }
            } else {
                if (corte.isBefore(hoy)) {
                    estadoCuenta = "En mora";
                    badgeClass = "er";
                } else {
                    estadoCuenta = "Inactivo";
                    badgeClass = "no";
                }
            }

            return com.autos_colombia.dto.EstadoCuentaDTO.builder()
                    .dueno(d)
                    .fechaCorte(corte)
                    .diasRestantes(diasRestantes)
                    .montoPagar(monto)
                    .vehiculosFacturables(vehiculosFacturables)
                    .ultimoPagoAprobado(ultimoAprobado)
                    .estadoCuenta(estadoCuenta)
                    .estadoClaseBadge(badgeClass)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Record de liquidación ─────────────────────────────────────────────────
    public record LiquidacionMensualidad(
            Dueno dueno,
            long cantidadCarros,
            long cantidadMotos,
            BigDecimal tarifaCarro,
            BigDecimal tarifaMoto,
            BigDecimal subtotalCarros,
            BigDecimal subtotalMotos,
            BigDecimal totalPagar,
            int totalVehiculos        // facturables únicamente
    ) {}

    /**
     * Determina si un vehículo es facturable:
     *   - ACTIVO (con o sin celda) → facturable
     *   - INACTIVO con celda asignada → inactivado por mora → facturable
     *   - INACTIVO con celda null → inhabilitado manualmente → NO facturable
     */
    private boolean esFacturable(Vehiculo v) {
        if ("ACTIVO".equalsIgnoreCase(v.getEstado())) return true;
        if ("INACTIVO".equalsIgnoreCase(v.getEstado()) && v.getCelda() != null) return true;
        return false;
    }

    /**
     * Calcula la liquidación usando solo vehículos facturables.
     * Lanza error de negocio si:
     *   - no hay vehículos facturables
     *   - hay carros facturables pero la tarifa CARRO no está configurada (es 0)
     *   - hay motos facturable pero la tarifa MOTO no está configurada (es 0)
     */
    @Transactional(readOnly = true)
    public LiquidacionMensualidad calcularLiquidacion(Long idDueno) {
        Dueno dueno = duenoRepository.findById(idDueno)
                .orElseThrow(() -> new IllegalArgumentException("Dueño no encontrado con ID: " + idDueno));

        List<Vehiculo> todos = vehiculoRepository.findByDuenoId(idDueno);
        List<Vehiculo> facturables = todos.stream().filter(this::esFacturable).collect(Collectors.toList());

        if (facturables.isEmpty()) {
            throw new IllegalStateException(
                    "El cliente no tiene vehículos facturables. Los vehículos inhabilitados manualmente (sin celda) no se cobran.");
        }

        long carros = facturables.stream().filter(v -> "CARRO".equalsIgnoreCase(v.getTipoVehiculo())).count();
        long motos  = facturables.stream().filter(v -> "MOTO".equalsIgnoreCase(v.getTipoVehiculo())).count();

        BigDecimal tarifaCarro = tarifaService.obtenerValorTarifa("CARRO");
        BigDecimal tarifaMoto  = tarifaService.obtenerValorTarifa("MOTO");

        // Validar que las tarifas necesarias estén configuradas
        if (carros > 0 && BigDecimal.ZERO.compareTo(tarifaCarro) == 0) {
            throw new IllegalStateException(
                    "La tarifa para CARRO no está configurada. Configúrela en Tarifas antes de procesar el pago.");
        }
        if (motos > 0 && BigDecimal.ZERO.compareTo(tarifaMoto) == 0) {
            throw new IllegalStateException(
                    "La tarifa para MOTO no está configurada. Configúrela en Tarifas antes de procesar el pago.");
        }

        BigDecimal subtotalCarros = tarifaCarro.multiply(BigDecimal.valueOf(carros));
        BigDecimal subtotalMotos  = tarifaMoto.multiply(BigDecimal.valueOf(motos));
        BigDecimal total = subtotalCarros.add(subtotalMotos);

        return new LiquidacionMensualidad(
                dueno, carros, motos, tarifaCarro, tarifaMoto,
                subtotalCarros, subtotalMotos, total, facturables.size()
        );
    }

    // ── Referencia única ──────────────────────────────────────────────────────
    private String generarReferencia(String estado) {
        String prefijo = "RECHAZADO".equals(estado) ? "RECH" : "PAGO";
        return prefijo + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Guarda el intento RECHAZADO en transacción independiente (REQUIRES_NEW)
     * para que persista aunque la transacción padre haga rollback.
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

    // ── Proceso principal ─────────────────────────────────────────────────────
    public Pago procesarPago(PagoDTO dto) {
        // calcularLiquidacion ya valida facturables y tarifas → lanza si hay error
        LiquidacionMensualidad liq = calcularLiquidacion(dto.getIdDueno());
        Dueno dueno = liq.dueno();

        // Consultar pasarela simulada
        String estadoPago = pasarela.procesarTransaccion(
                liq.totalPagar(),
                dto.getMetodoPago(),
                dto.getResultadoSimulado()
        );

        if ("RECHAZADO".equals(estadoPago)) {
            // Persistir intento sin modificar estado del dueño/vehículos
            Pago intento = guardarIntento(dueno, liq.totalPagar(), liq.totalVehiculos(),
                    dto.getMetodoPago(), "RECHAZADO");
            throw new PagoRechazadoException(
                    "Pago rechazado por la pasarela. Referencia del intento: " + intento.getReferenciaTransaccion(),
                    intento
            );
        }

        // ── APROBADO ──────────────────────────────────────────────────────────

        // T11: fecha_corte — si ya venció, partir desde hoy; si no, sumar desde la fecha actual
        LocalDate hoy = LocalDate.now();
        LocalDate nuevaFechaCorte = dueno.getFechaCorte().isBefore(hoy)
                ? hoy.plusMonths(1)
                : dueno.getFechaCorte().plusMonths(1);
        dueno.setFechaCorte(nuevaFechaCorte);

        // T11: reactivar solo si el dueño está inactivo
        if ("INACTIVO".equalsIgnoreCase(dueno.getEstado())) {
            dueno.setEstado("ACTIVO");
            // Reactivar SOLO los vehículos inactivados por mora (INACTIVO con celda asignada)
            // Los INACTIVO con celda null fueron inhabilitados manualmente → no tocar
            List<Vehiculo> vehiculos = vehiculoRepository.findByDuenoId(dueno.getId());
            for (Vehiculo v : vehiculos) {
                if ("INACTIVO".equalsIgnoreCase(v.getEstado()) && v.getCelda() != null) {
                    v.setEstado("ACTIVO");
                    vehiculoRepository.save(v);
                }
            }
        }
        duenoRepository.save(dueno);

        Pago pago = Pago.builder()
                .dueno(dueno)
                .fechaPago(LocalDateTime.now())
                .montoTotal(liq.totalPagar())
                .cantidadVehiculos(liq.totalVehiculos())
                .referenciaTransaccion(generarReferencia("APROBADO"))
                .metodoPago(dto.getMetodoPago().toUpperCase())
                .estadoPago("APROBADO")
                .build();

        return pagoRepository.save(pago);
    }

    // ── Excepción interna ─────────────────────────────────────────────────────
    public static class PagoRechazadoException extends RuntimeException {
        private final Pago pago;
        public PagoRechazadoException(String msg, Pago pago) {
            super(msg);
            this.pago = pago;
        }
        public Pago getPago() { return pago; }
    }
}
