package com.autos_colombia.service;

import com.autos_colombia.dto.PagoDTO;
import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Pago;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.DuenoRepository;
import com.autos_colombia.repository.PagoRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
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

    public PagoService(PagoRepository pagoRepository,
                       DuenoRepository duenoRepository,
                       VehiculoRepository vehiculoRepository,
                       TarifaService tarifaService) {
        this.pagoRepository = pagoRepository;
        this.duenoRepository = duenoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.tarifaService = tarifaService;
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

    public Pago procesarPago(PagoDTO dto) {
        LiquidacionMensualidad liq = calcularLiquidacion(dto.getIdDueno());
        Dueno dueno = liq.dueno();

        if (liq.totalVehiculos() == 0) {
            throw new IllegalStateException("El cliente no tiene vehículos vinculados para liquidar mensualidad.");
        }

        // HU08 Criterio 2 y 3: generar referencia única y sumar 1 mes a la fecha de corte
        String ref = "PAGO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        dueno.setFechaCorte(dueno.getFechaCorte().plusMonths(1));

        // HU08 Criterio 4: Si el dueño y sus vehículos se encontraban Inactivos, reactivarlos
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
}
