package com.autos_colombia.service;

import com.autos_colombia.dto.MovimientoEntradaDTO;
import com.autos_colombia.dto.MovimientoSalidaDTO;
import com.autos_colombia.model.Movimiento;
import com.autos_colombia.model.Usuario;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.MovimientoRepository;
import com.autos_colombia.repository.UsuarioRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoService(MovimientoRepository movimientoRepository,
                             VehiculoRepository vehiculoRepository,
                             UsuarioRepository usuarioRepository) {
        this.movimientoRepository = movimientoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Movimiento> obtenerUltimos10() {
        return movimientoRepository.findTop10ByOrderByFechaHoraDesc();
    }

    @Transactional(readOnly = true)
    public List<Movimiento> obtenerTodos() {
        return movimientoRepository.findAllByOrderByFechaHoraDesc();
    }

    public Movimiento registrarEntrada(MovimientoEntradaDTO dto, String usernameOperario) {
        String placa = dto.getPlaca().trim().toUpperCase();

        Vehiculo vehiculo = vehiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new IllegalArgumentException("El vehículo con placa " + placa + " no está registrado."));

        // HU04 Criterio 2: Vehículo activo y dueño activo
        if (!"ACTIVO".equalsIgnoreCase(vehiculo.getEstado())) {
            throw new IllegalStateException("Rechazado: el vehículo " + placa + " se encuentra Inactivo en el sistema.");
        }

        if (vehiculo.getDueno() == null || !"ACTIVO".equalsIgnoreCase(vehiculo.getDueno().getEstado())) {
            throw new IllegalStateException("Rechazado: el dueño del vehículo no se encuentra activo o presenta suspensión por mora.");
        }

        // HU04 Criterio 3: estado_presencia debe ser Ausente
        if ("PRESENTE".equalsIgnoreCase(vehiculo.getEstadoPresencia())) {
            throw new IllegalStateException("Rechazado: el vehículo " + placa + " ya se encuentra dentro del parqueadero.");
        }

        Usuario usuario = usuarioRepository.findByUsername(usernameOperario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no válido para autorizar el movimiento."));

        // Actualizar presencia
        vehiculo.setEstadoPresencia("PRESENTE");
        vehiculoRepository.save(vehiculo);

        // Guardar movimiento
        Movimiento movimiento = Movimiento.builder()
                .vehiculo(vehiculo)
                .usuario(usuario)
                .tipoMovimiento("ENTRADA")
                .fechaHora(LocalDateTime.now())
                .build();

        return movimientoRepository.save(movimiento);
    }

    public Movimiento registrarSalida(MovimientoSalidaDTO dto, String usernameOperario) {
        String placa = dto.getPlaca().trim().toUpperCase();

        Vehiculo vehiculo = vehiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new IllegalArgumentException("El vehículo con placa " + placa + " no está registrado."));

        // HU05 Criterio 2: estado_presencia debe ser Presente
        if (!"PRESENTE".equalsIgnoreCase(vehiculo.getEstadoPresencia())) {
            throw new IllegalStateException("Rechazado: el vehículo " + placa + " figura como Ausente; no se registra ingreso previo.");
        }

        Usuario usuario = usuarioRepository.findByUsername(usernameOperario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no válido para autorizar el movimiento."));

        // Actualizar presencia
        vehiculo.setEstadoPresencia("AUSENTE");
        vehiculoRepository.save(vehiculo);

        // Guardar movimiento
        Movimiento movimiento = Movimiento.builder()
                .vehiculo(vehiculo)
                .usuario(usuario)
                .tipoMovimiento("SALIDA")
                .fechaHora(LocalDateTime.now())
                .build();

        return movimientoRepository.save(movimiento);
    }
}
