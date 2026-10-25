package com.autos_colombia.service;

import com.autos_colombia.dto.VehiculoDTO;
import com.autos_colombia.model.Celda;
import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.CeldaRepository;
import com.autos_colombia.repository.DuenoRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final DuenoRepository duenoRepository;
    private final CeldaRepository celdaRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           DuenoRepository duenoRepository,
                           CeldaRepository celdaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.duenoRepository = duenoRepository;
        this.celdaRepository = celdaRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerTodos() {
        return vehiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerPorEstado(String estado) {
        return vehiculoRepository.findByEstado(estado.toUpperCase());
    }

    @Transactional(readOnly = true)
    public Vehiculo obtenerPorId(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
    }

    @Transactional(readOnly = true)
    public Vehiculo obtenerPorPlaca(String placa) {
        return vehiculoRepository.findByPlaca(placa.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el vehículo con placa: " + placa));
    }

    public Vehiculo registrarVehiculo(VehiculoDTO dto) {
        String placa = dto.getPlaca().trim().toUpperCase();
        if (vehiculoRepository.existsByPlaca(placa)) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la placa " + placa);
        }

        Dueno dueno = duenoRepository.findById(dto.getIdDueno())
                .orElseThrow(() -> new IllegalArgumentException("Dueño no encontrado"));

        // HU03 Criterio 4: No asociar vehículo a dueño inactivo
        if (!"ACTIVO".equalsIgnoreCase(dueno.getEstado())) {
            throw new IllegalStateException("No se puede registrar un vehículo a un dueño en estado Inactivo.");
        }

        Celda celda = celdaRepository.findById(dto.getIdCelda())
                .orElseThrow(() -> new IllegalArgumentException("Celda no encontrada"));

        if (!"DISPONIBLE".equalsIgnoreCase(celda.getEstadoCelda())) {
            throw new IllegalStateException("La celda seleccionada (" + celda.getCodigoCelda() + ") no está Disponible.");
        }

        if (!celda.getTipoPermitido().equalsIgnoreCase(dto.getTipoVehiculo())) {
            throw new IllegalArgumentException("La celda " + celda.getCodigoCelda() + " solo admite vehículos tipo " + celda.getTipoPermitido());
        }

        // Asignar celda
        celda.setEstadoCelda("ASIGNADA");
        celdaRepository.save(celda);

        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placa)
                .tipoVehiculo(dto.getTipoVehiculo().toUpperCase())
                .marca(dto.getMarca().trim())
                .modelo(dto.getModelo())
                .color(dto.getColor().trim())
                .fechaVinculacion(LocalDate.now())
                .estado("ACTIVO")
                .estadoPresencia("AUSENTE") // HU03 Criterio 3: estado_presencia inicial Ausente
                .dueno(dueno)
                .celda(celda)
                .build();

        return vehiculoRepository.save(vehiculo);
    }

    public Vehiculo actualizarVehiculo(Long id, VehiculoDTO dto) {
        Vehiculo vehiculo = obtenerPorId(id);
        vehiculo.setMarca(dto.getMarca().trim());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setColor(dto.getColor().trim());
        return vehiculoRepository.save(vehiculo);
    }

    public void inhabilitarVehiculo(Long id) {
        Vehiculo vehiculo = obtenerPorId(id);
        vehiculo.setEstado("INACTIVO");

        // HU10 Criterio 3: Al inhabilitar un vehículo permanentemente, la celda asociada pasa a Disponible
        if (vehiculo.getCelda() != null) {
            Celda celda = vehiculo.getCelda();
            celda.setEstadoCelda("DISPONIBLE");
            celdaRepository.save(celda);
        }

        vehiculoRepository.save(vehiculo);
    }

    public void activarVehiculo(Long id) {
        Vehiculo vehiculo = obtenerPorId(id);
        vehiculo.setEstado("ACTIVO");
        if (vehiculo.getCelda() != null) {
            Celda celda = vehiculo.getCelda();
            celda.setEstadoCelda("ASIGNADA");
            celdaRepository.save(celda);
        }
        vehiculoRepository.save(vehiculo);
    }
}
