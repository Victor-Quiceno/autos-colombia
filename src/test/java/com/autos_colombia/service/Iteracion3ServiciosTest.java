package com.autos_colombia.service;

import com.autos_colombia.dto.PagoDTO;
import com.autos_colombia.dto.TarifaDTO;
import com.autos_colombia.model.Celda;
import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Pago;
import com.autos_colombia.model.Tarifa;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.CeldaRepository;
import com.autos_colombia.repository.DuenoRepository;
import com.autos_colombia.repository.PagoRepository;
import com.autos_colombia.repository.TarifaRepository;
import com.autos_colombia.repository.VehiculoRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class Iteracion3ServiciosTest {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private ScheduledTasksService scheduledTasksService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DuenoRepository duenoRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private CeldaRepository celdaRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        // Asegurar que las tarifas base existan (CARRO = 120000, MOTO = 60000)
        tarifaRepository.findByTipoVehiculo("CARRO").ifPresentOrElse(
                t -> {
                    t.setValorMensualidad(new BigDecimal("120000.00"));
                    tarifaRepository.save(t);
                },
                () -> tarifaRepository.save(Tarifa.builder()
                        .tipoVehiculo("CARRO")
                        .valorMensualidad(new BigDecimal("120000.00"))
                        .fechaActualizacion(LocalDate.now())
                        .build())
        );

        tarifaRepository.findByTipoVehiculo("MOTO").ifPresentOrElse(
                t -> {
                    t.setValorMensualidad(new BigDecimal("60000.00"));
                    tarifaRepository.save(t);
                },
                () -> tarifaRepository.save(Tarifa.builder()
                        .tipoVehiculo("MOTO")
                        .valorMensualidad(new BigDecimal("60000.00"))
                        .fechaActualizacion(LocalDate.now())
                        .build())
        );
    }

    private Dueno crearDuenoPrueba(String doc, LocalDate fechaCorte, String estado) {
        return duenoRepository.save(Dueno.builder()
                .tipoDocumento("CC")
                .numeroDocumento(doc)
                .nombres("Test")
                .apellidos("Pagos " + doc)
                .telefono("3001234567")
                .correo("test" + doc + "@autocolombia.com")
                .fechaVinculacion(LocalDate.now().minusMonths(2))
                .fechaCorte(fechaCorte)
                .estado(estado)
                .build());
    }

    private Vehiculo crearVehiculoPrueba(String placa, String tipo, Dueno dueno, String estado, Celda celda) {
        return vehiculoRepository.save(Vehiculo.builder()
                .placa(placa)
                .tipoVehiculo(tipo)
                .marca("MarcaTest")
                .modelo(2022)
                .color("Negro")
                .fechaVinculacion(LocalDate.now().minusMonths(2))
                .estado(estado)
                .estadoPresencia("AUSENTE")
                .dueno(dueno)
                .celda(celda)
                .build());
    }

    private static final java.util.concurrent.atomic.AtomicInteger celdaSeq = new java.util.concurrent.atomic.AtomicInteger(100);

    private Celda obtenerCeldaLibre(String tipo) {
        return celdaRepository.save(Celda.builder()
                .codigoCelda("T" + celdaSeq.incrementAndGet())
                .tipoPermitido(tipo)
                .estadoCelda("ASIGNADA")
                .build());
    }

    @Test
    @DisplayName("T14.1 - Monto liquidación: 2 carros ($120.000 c/u) + 1 moto ($60.000) = $300.000")
    void testCalculoMontoLiquidacion() {
        Dueno d = crearDuenoPrueba("DOC-300K", LocalDate.now().plusDays(10), "ACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        Celda c2 = obtenerCeldaLibre("CARRO");
        Celda c3 = obtenerCeldaLibre("MOTO");

        crearVehiculoPrueba("CAR001", "CARRO", d, "ACTIVO", c1);
        crearVehiculoPrueba("CAR002", "CARRO", d, "ACTIVO", c2);
        crearVehiculoPrueba("MOT001", "MOTO", d, "ACTIVO", c3);

        var liq = pagoService.calcularLiquidacion(d.getId());

        assertEquals(2, liq.cantidadCarros());
        assertEquals(1, liq.cantidadMotos());
        assertEquals(3, liq.totalVehiculos());
        assertEquals(0, new BigDecimal("300000.00").compareTo(liq.totalPagar()));
    }

    @Test
    @DisplayName("T14.2 - Pago aprobado suma 1 mes (corte vigente)")
    void testPagoAprobadoCorteVigente() {
        LocalDate corteVigente = LocalDate.now().plusDays(15);
        Dueno d = crearDuenoPrueba("DOC-VIG", corteVigente, "ACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        crearVehiculoPrueba("VIG001", "CARRO", d, "ACTIVO", c1);

        PagoDTO dto = PagoDTO.builder()
                .idDueno(d.getId())
                .metodoPago("PSE")
                .resultadoSimulado("APROBADO")
                .build();

        Pago pago = pagoService.procesarPago(dto);

        assertNotNull(pago);
        assertEquals("APROBADO", pago.getEstadoPago());

        Dueno actualizado = duenoRepository.findById(d.getId()).orElseThrow();
        assertEquals(corteVigente.plusMonths(1), actualizado.getFechaCorte());
    }

    @Test
    @DisplayName("T14.3 - Pago con corte vencido → fecha de corte = hoy + 1 mes")
    void testPagoConCorteVencido() {
        LocalDate corteVencido = LocalDate.now().minusDays(20);
        Dueno d = crearDuenoPrueba("DOC-VENC", corteVencido, "INACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        crearVehiculoPrueba("VNC001", "CARRO", d, "INACTIVO", c1);

        PagoDTO dto = PagoDTO.builder()
                .idDueno(d.getId())
                .metodoPago("TARJETA_CREDITO")
                .resultadoSimulado("APROBADO")
                .build();

        Pago pago = pagoService.procesarPago(dto);

        assertNotNull(pago);
        assertEquals("APROBADO", pago.getEstadoPago());

        Dueno actualizado = duenoRepository.findById(d.getId()).orElseThrow();
        assertEquals(LocalDate.now().plusMonths(1), actualizado.getFechaCorte());
    }

    @Test
    @DisplayName("T14.4 - Pago rechazado no cambia corte ni estados y se guarda como RECHAZADO")
    void testPagoRechazadoNoModificaEstadosYSeGuarda() {
        LocalDate corteOriginal = LocalDate.now().minusDays(5);
        Dueno d = crearDuenoPrueba("DOC-RECH", corteOriginal, "INACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        Vehiculo v = crearVehiculoPrueba("RCH001", "CARRO", d, "INACTIVO", c1);

        PagoDTO dto = PagoDTO.builder()
                .idDueno(d.getId())
                .metodoPago("PSE")
                .resultadoSimulado("RECHAZADO")
                .build();

        PagoService.PagoRechazadoException ex = assertThrows(
                PagoService.PagoRechazadoException.class,
                () -> pagoService.procesarPago(dto)
        );

        assertNotNull(ex.getPago());
        assertEquals("RECHAZADO", ex.getPago().getEstadoPago());
        assertTrue(ex.getPago().getReferenciaTransaccion().startsWith("RECH-"));

        // Verificar que no se modificó el dueño ni el vehículo
        Dueno dDb = duenoRepository.findById(d.getId()).orElseThrow();
        assertEquals("INACTIVO", dDb.getEstado());
        assertEquals(corteOriginal, dDb.getFechaCorte());

        Vehiculo vDb = vehiculoRepository.findById(v.getId()).orElseThrow();
        assertEquals("INACTIVO", vDb.getEstado());
    }

    @Test
    @DisplayName("T14.5 - Reactivación por pago: reactiva vehículos con celda y NO los de celda null")
    void testReactivacionSoloVehiculosConCelda() {
        Dueno d = crearDuenoPrueba("DOC-REACT", LocalDate.now().minusDays(10), "INACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");

        // v1: inactivo por mora (conserva celda)
        Vehiculo v1 = crearVehiculoPrueba("MORA01", "CARRO", d, "INACTIVO", c1);
        // v2: inactivo manual (celda null)
        Vehiculo v2 = crearVehiculoPrueba("MAN001", "CARRO", d, "INACTIVO", null);

        PagoDTO dto = PagoDTO.builder()
                .idDueno(d.getId())
                .metodoPago("EFECTIVO")
                .resultadoSimulado("APROBADO")
                .build();

        pagoService.procesarPago(dto);

        Dueno dAct = duenoRepository.findById(d.getId()).orElseThrow();
        assertEquals("ACTIVO", dAct.getEstado());

        Vehiculo v1Act = vehiculoRepository.findById(v1.getId()).orElseThrow();
        assertEquals("ACTIVO", v1Act.getEstado(), "El vehículo con celda debe reactivarse");

        Vehiculo v2Act = vehiculoRepository.findById(v2.getId()).orElseThrow();
        assertEquals("INACTIVO", v2Act.getEstado(), "El vehículo inhabilitado manualmente (sin celda) debe permanecer INACTIVO");
        assertNull(v2Act.getCelda());
    }

    @Test
    @DisplayName("T14.6 - Sin vehículos facturables / sin tarifa → error y nada guardado")
    void testSinVehiculosFacturablesOSinTarifaError() {
        Dueno d = crearDuenoPrueba("DOC-NOFAC", LocalDate.now(), "ACTIVO");
        // Vehículo inactivo sin celda -> no facturable
        crearVehiculoPrueba("NOF001", "CARRO", d, "INACTIVO", null);

        PagoDTO dto = PagoDTO.builder()
                .idDueno(d.getId())
                .metodoPago("PSE")
                .resultadoSimulado("APROBADO")
                .build();

        // 1. Debe lanzar error de negocio al no haber vehículos facturables
        assertThrows(IllegalStateException.class, () -> pagoService.procesarPago(dto));

        // 2. Si hay vehículo facturable pero tarifa en 0
        Dueno d2 = crearDuenoPrueba("DOC-NOTAR", LocalDate.now(), "ACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        crearVehiculoPrueba("CARNOTAR", "CARRO", d2, "ACTIVO", c1);

        Tarifa tCarro = tarifaRepository.findByTipoVehiculo("CARRO").orElseThrow();
        tCarro.setValorMensualidad(BigDecimal.ZERO);
        tarifaRepository.save(tCarro);

        PagoDTO dto2 = PagoDTO.builder()
                .idDueno(d2.getId())
                .metodoPago("PSE")
                .resultadoSimulado("APROBADO")
                .build();

        assertThrows(IllegalStateException.class, () -> pagoService.procesarPago(dto2));
    }

    @Test
    @DisplayName("T14.7 - Mora inactiva dueño y vehículos conservando la celda")
    void testMoraInactivaConservandoCelda() {
        LocalDate hoy = LocalDate.now();
        Dueno d = crearDuenoPrueba("DOC-MORA-OK", hoy.minusDays(1), "ACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        Vehiculo v = crearVehiculoPrueba("MRA100", "CARRO", d, "ACTIVO", c1);

        scheduledTasksService.verificarSuspensionPorMora(hoy);

        Dueno dDb = duenoRepository.findById(d.getId()).orElseThrow();
        assertEquals("INACTIVO", dDb.getEstado());

        Vehiculo vDb = vehiculoRepository.findById(v.getId()).orElseThrow();
        assertEquals("INACTIVO", vDb.getEstado());
        assertNotNull(vDb.getCelda(), "La celda debe conservarse para permitir reactivación por pago");
        assertEquals(c1.getId(), vDb.getCelda().getId());
    }

    @Test
    @DisplayName("T14.8 - Mora omite al dueño con vehículo Presente")
    void testMoraOmiteDuenoConVehiculoPresente() {
        LocalDate hoy = LocalDate.now();
        Dueno d = crearDuenoPrueba("DOC-PRES", hoy.minusDays(2), "ACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        Vehiculo v = crearVehiculoPrueba("PRS100", "CARRO", d, "ACTIVO", c1);
        v.setEstadoPresencia("PRESENTE");
        vehiculoRepository.save(v);

        scheduledTasksService.verificarSuspensionPorMora(hoy);

        Dueno dDb = duenoRepository.findById(d.getId()).orElseThrow();
        assertEquals("ACTIVO", dDb.getEstado(), "Dueño con vehículo PRESENTE no debe inactivarse por mora");

        Vehiculo vDb = vehiculoRepository.findById(v.getId()).orElseThrow();
        assertEquals("ACTIVO", vDb.getEstado());
    }

    @Test
    @DisplayName("T14.9 - Recordatorio solo a dueños con corte exactamente en 3 días (no 2 ni 4)")
    void testRecordatorioExactamenteTresDias() {
        LocalDate hoy = LocalDate.now();

        // d1: hoy + 3 días (debe recibir)
        Dueno d1 = crearDuenoPrueba("DOC-REC3", hoy.plusDays(3), "ACTIVO");
        Celda c1 = obtenerCeldaLibre("CARRO");
        crearVehiculoPrueba("REC003", "CARRO", d1, "ACTIVO", c1);

        // d2: hoy + 2 días (NO debe recibir)
        Dueno d2 = crearDuenoPrueba("DOC-REC2", hoy.plusDays(2), "ACTIVO");
        Celda c2 = obtenerCeldaLibre("CARRO");
        crearVehiculoPrueba("REC002", "CARRO", d2, "ACTIVO", c2);

        // d3: hoy + 4 días (NO debe recibir)
        Dueno d3 = crearDuenoPrueba("DOC-REC4", hoy.plusDays(4), "ACTIVO");
        Celda c3 = obtenerCeldaLibre("CARRO");
        crearVehiculoPrueba("REC004", "CARRO", d3, "ACTIVO", c3);

        scheduledTasksService.verificarRecordatoriosCobro(hoy);

        List<EmailService.NotificacionCorreo> correos = emailService.getUltimosCorreos();
        boolean recibidoD1 = correos.stream().anyMatch(c -> c.destinatario().equals(d1.getCorreo()));
        boolean recibidoD2 = correos.stream().anyMatch(c -> c.destinatario().equals(d2.getCorreo()));
        boolean recibidoD3 = correos.stream().anyMatch(c -> c.destinatario().equals(d3.getCorreo()));

        assertTrue(recibidoD1, "El dueño con fecha hoy+3 días debe recibir recordatorio");
        assertFalse(recibidoD2, "El dueño con fecha hoy+2 días NO debe recibir recordatorio");
        assertFalse(recibidoD3, "El dueño con fecha hoy+4 días NO debe recibir recordatorio");
    }

    @Test
    @DisplayName("T14.10 - Tarifa ≤ 0 rechazada (validación DTO) y referencia de transacción única")
    void testValidacionTarifaYReferenciaUnica() {
        // 1. Tarifa ≤ 0 rechazada por validación del DTO
        TarifaDTO dtoCero = TarifaDTO.builder()
                .tipoVehiculo("CARRO")
                .valorMensualidad(BigDecimal.ZERO)
                .build();
        var violacionesCero = validator.validate(dtoCero);
        assertFalse(violacionesCero.isEmpty(), "Tarifa = 0 debe tener violaciones de validación (@Positive)");

        TarifaDTO dtoNegativo = TarifaDTO.builder()
                .tipoVehiculo("CARRO")
                .valorMensualidad(new BigDecimal("-1000"))
                .build();
        var violacionesNeg = validator.validate(dtoNegativo);
        assertFalse(violacionesNeg.isEmpty(), "Tarifa < 0 debe tener violaciones de validación (@Positive)");

        TarifaDTO dtoValido = TarifaDTO.builder()
                .tipoVehiculo("CARRO")
                .valorMensualidad(new BigDecimal("150000"))
                .build();
        assertTrue(validator.validate(dtoValido).isEmpty(), "Tarifa > 0 debe ser válida");

        // 2. Referencia de transacción única
        Dueno d = crearDuenoPrueba("DOC-REF", LocalDate.now().plusDays(10), "ACTIVO");
        Celda c = obtenerCeldaLibre("CARRO");
        crearVehiculoPrueba("REF001", "CARRO", d, "ACTIVO", c);

        PagoDTO p1 = PagoDTO.builder().idDueno(d.getId()).metodoPago("PSE").resultadoSimulado("APROBADO").build();
        PagoDTO p2 = PagoDTO.builder().idDueno(d.getId()).metodoPago("PSE").resultadoSimulado("APROBADO").build();

        Pago res1 = pagoService.procesarPago(p1);
        Pago res2 = pagoService.procesarPago(p2);

        assertNotNull(res1.getReferenciaTransaccion());
        assertNotNull(res2.getReferenciaTransaccion());
        assertNotEquals(res1.getReferenciaTransaccion(), res2.getReferenciaTransaccion(),
                "Las referencias de dos transacciones deben ser únicas");
    }
}
