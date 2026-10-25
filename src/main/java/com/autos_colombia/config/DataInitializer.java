package com.autos_colombia.config;

import com.autos_colombia.model.*;
import com.autos_colombia.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TarifaRepository tarifaRepository;
    private final CeldaRepository celdaRepository;
    private final DuenoRepository duenoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final MovimientoRepository movimientoRepository;
    private final NovedadRepository novedadRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           TarifaRepository tarifaRepository,
                           CeldaRepository celdaRepository,
                           DuenoRepository duenoRepository,
                           VehiculoRepository vehiculoRepository,
                           MovimientoRepository movimientoRepository,
                           NovedadRepository novedadRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tarifaRepository = tarifaRepository;
        this.celdaRepository = celdaRepository;
        this.duenoRepository = duenoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.movimientoRepository = movimientoRepository;
        this.novedadRepository = novedadRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Usuarios iniciales
        if (usuarioRepository.count() == 0) {
            Usuario adminPrincipal = Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .nombreCompleto("Administrador General")
                    .rol("ROLE_ADMIN")
                    .estado("ACTIVO")
                    .build();
            usuarioRepository.save(adminPrincipal);

            Usuario admin01 = Usuario.builder()
                    .username("admin01")
                    .password(passwordEncoder.encode("admin123"))
                    .nombreCompleto("Carlos Gómez")
                    .rol("ROLE_ADMIN")
                    .estado("ACTIVO")
                    .build();
            usuarioRepository.save(admin01);

            Usuario operarioPrincipal = Usuario.builder()
                    .username("operario")
                    .password(passwordEncoder.encode("operario123"))
                    .nombreCompleto("Operario General")
                    .rol("ROLE_OPERARIO")
                    .estado("ACTIVO")
                    .build();
            usuarioRepository.save(operarioPrincipal);

            Usuario operario = Usuario.builder()
                    .username("m.rios")
                    .password(passwordEncoder.encode("operario123"))
                    .nombreCompleto("Marcos Ríos")
                    .rol("ROLE_OPERARIO")
                    .estado("ACTIVO")
                    .build();
            usuarioRepository.save(operario);

            Usuario operario2 = Usuario.builder()
                    .username("c.diaz")
                    .password(passwordEncoder.encode("operario123"))
                    .nombreCompleto("Carolina Díaz")
                    .rol("ROLE_OPERARIO")
                    .estado("ACTIVO")
                    .build();
            usuarioRepository.save(operario2);
        }

        // 2. Tarifas iniciales
        if (tarifaRepository.count() == 0) {
            tarifaRepository.save(Tarifa.builder()
                    .tipoVehiculo("CARRO")
                    .valorMensualidad(new BigDecimal("120000.00"))
                    .fechaActualizacion(LocalDate.now())
                    .build());

            tarifaRepository.save(Tarifa.builder()
                    .tipoVehiculo("MOTO")
                    .valorMensualidad(new BigDecimal("60000.00"))
                    .fechaActualizacion(LocalDate.now())
                    .build());
        }

        // 3. Celdas iniciales (Carro: C-01 a C-10, Moto: M-01 a M-05)
        if (celdaRepository.count() == 0) {
            for (int i = 1; i <= 10; i++) {
                String codigo = String.format("C-%02d", i);
                celdaRepository.save(Celda.builder()
                        .codigoCelda(codigo)
                        .tipoPermitido("CARRO")
                        .estadoCelda("DISPONIBLE")
                        .build());
            }

            for (int i = 1; i <= 5; i++) {
                String codigo = String.format("M-%02d", i);
                celdaRepository.save(Celda.builder()
                        .codigoCelda(codigo)
                        .tipoPermitido("MOTO")
                        .estadoCelda("DISPONIBLE")
                        .build());
            }
        }

        // 4. Dueños y Vehículos de prueba (para reflejar los datos de los mockups)
        if (duenoRepository.count() == 0) {
            // Dueño 1: Laura Gómez
            Dueno laura = Dueno.builder()
                    .tipoDocumento("CC")
                    .numeroDocumento("1037456789")
                    .nombres("Laura")
                    .apellidos("Gómez Ruiz")
                    .telefono("3001234567")
                    .correo("laura.gomez@example.com")
                    .fechaVinculacion(LocalDate.now().minusMonths(2))
                    .fechaCorte(LocalDate.now().plusMonths(1))
                    .estado("ACTIVO")
                    .build();
            laura = duenoRepository.save(laura);

            // Dueño 2: Pedro Lara
            Dueno pedro = Dueno.builder()
                    .tipoDocumento("CC")
                    .numeroDocumento("71234567")
                    .nombres("Pedro")
                    .apellidos("Lara Pérez")
                    .telefono("3109876543")
                    .correo("pedro.lara@example.com")
                    .fechaVinculacion(LocalDate.now().minusMonths(1))
                    .fechaCorte(LocalDate.now().plusDays(15))
                    .estado("ACTIVO")
                    .build();
            pedro = duenoRepository.save(pedro);

            // Celda C-04 para Laura (Carro ABC123)
            Celda c04 = celdaRepository.findByCodigoCelda("C-04").orElseThrow();
            c04.setEstadoCelda("ASIGNADA");
            celdaRepository.save(c04);

            Vehiculo v1 = Vehiculo.builder()
                    .placa("ABC123")
                    .tipoVehiculo("CARRO")
                    .marca("Mazda")
                    .modelo(2022)
                    .color("Gris")
                    .fechaVinculacion(LocalDate.now().minusMonths(2))
                    .estado("ACTIVO")
                    .estadoPresencia("PRESENTE")
                    .dueno(laura)
                    .celda(c04)
                    .build();
            v1 = vehiculoRepository.save(v1);

            // Celda C-02 para Pedro (Carro XYZ789)
            Celda c02 = celdaRepository.findByCodigoCelda("C-02").orElseThrow();
            c02.setEstadoCelda("ASIGNADA");
            celdaRepository.save(c02);

            Vehiculo v2 = Vehiculo.builder()
                    .placa("XYZ789")
                    .tipoVehiculo("CARRO")
                    .marca("Chevrolet")
                    .modelo(2021)
                    .color("Blanco")
                    .fechaVinculacion(LocalDate.now().minusMonths(1))
                    .estado("ACTIVO")
                    .estadoPresencia("AUSENTE")
                    .dueno(pedro)
                    .celda(c02)
                    .build();
            vehiculoRepository.save(v2);

            // Celda M-03 para Pedro (Moto JKL45M - Inactivo)
            Celda m03 = celdaRepository.findByCodigoCelda("M-03").orElseThrow();
            m03.setEstadoCelda("ASIGNADA");
            celdaRepository.save(m03);

            Vehiculo v3 = Vehiculo.builder()
                    .placa("JKL45M")
                    .tipoVehiculo("MOTO")
                    .marca("Yamaha")
                    .modelo(2023)
                    .color("Negro")
                    .fechaVinculacion(LocalDate.now().minusMonths(1))
                    .estado("INACTIVO")
                    .estadoPresencia("AUSENTE")
                    .dueno(pedro)
                    .celda(m03)
                    .build();
            vehiculoRepository.save(v3);

            // Movimientos iniciales
            Usuario operario = usuarioRepository.findByUsername("m.rios").orElseThrow();
            movimientoRepository.save(Movimiento.builder()
                    .vehiculo(v1)
                    .usuario(operario)
                    .tipoMovimiento("ENTRADA")
                    .fechaHora(LocalDateTime.now().minusHours(2))
                    .build());

            movimientoRepository.save(Movimiento.builder()
                    .vehiculo(v2)
                    .usuario(operario)
                    .tipoMovimiento("SALIDA")
                    .fechaHora(LocalDateTime.now().minusHours(3))
                    .build());

            // Novedad inicial
            novedadRepository.save(Novedad.builder()
                    .vehiculo(v1)
                    .usuario(operario)
                    .tipoNovedad("RAYON")
                    .descripcion("Rayón superficial en la puerta delantera derecha detectado en el ingreso.")
                    .nivelSeveridad("MEDIA")
                    .fechaHora(LocalDateTime.now().minusHours(2))
                    .estadoNovedad("ABIERTA")
                    .build());
        }
    }
}
