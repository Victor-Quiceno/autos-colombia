package com.autos_colombia.service;

import com.autos_colombia.dto.CeldaDTO;
import com.autos_colombia.model.Celda;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.CeldaRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CeldaService {

    private final CeldaRepository celdaRepository;
    private final VehiculoRepository vehiculoRepository;

    public CeldaService(CeldaRepository celdaRepository, VehiculoRepository vehiculoRepository) {
        this.celdaRepository = celdaRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<Celda> obtenerTodas() {
        return celdaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Celda> obtenerDisponiblesPorTipo(String tipo) {
        return celdaRepository.findByTipoPermitidoAndEstadoCelda(tipo.toUpperCase(), "DISPONIBLE");
    }

    @Transactional(readOnly = true)
    public Celda obtenerPorId(Long id) {
        return celdaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Celda no encontrada"));
    }

    public Celda crearCelda(CeldaDTO dto) {
        String codigo = dto.getCodigoCelda().trim().toUpperCase();
        if (celdaRepository.existsByCodigoCelda(codigo)) {
            throw new IllegalArgumentException("Ya existe una celda con el código " + codigo);
        }

        Celda celda = Celda.builder()
                .codigoCelda(codigo)
                .tipoPermitido(dto.getTipoPermitido().toUpperCase())
                .estadoCelda("DISPONIBLE")
                .build();

        return celdaRepository.save(celda);
    }

    public void cambiarEstado(Long idCelda, String nuevoEstado) {
        Celda celda = obtenerPorId(idCelda);

        // HU07 Criterio 3: Impide poner en mantenimiento si el vehículo está Presente
        if ("MANTENIMIENTO".equalsIgnoreCase(nuevoEstado)) {
            vehiculoRepository.findAll().stream()
                    .filter(v -> v.getCelda() != null && v.getCelda().getId().equals(idCelda))
                    .findFirst()
                    .ifPresent(v -> {
                        if ("PRESENTE".equalsIgnoreCase(v.getEstadoPresencia())) {
                            throw new IllegalStateException("No se puede poner la celda " + celda.getCodigoCelda() +
                                    " en mantenimiento ni reasignarla: el vehículo " + v.getPlaca() + " está Presente.");
                        }
                    });
        }

        celda.setEstadoCelda(nuevoEstado.toUpperCase());
        celdaRepository.save(celda);
    }
}
