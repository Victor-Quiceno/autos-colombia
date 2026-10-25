package com.autos_colombia.service;

import com.autos_colombia.dto.PanelMetricasDTO;
import com.autos_colombia.model.Movimiento;
import com.autos_colombia.model.Novedad;
import com.autos_colombia.repository.CeldaRepository;
import com.autos_colombia.repository.MovimientoRepository;
import com.autos_colombia.repository.NovedadRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PanelService {

    private final VehiculoRepository vehiculoRepository;
    private final CeldaRepository celdaRepository;
    private final NovedadRepository novedadRepository;
    private final MovimientoRepository movimientoRepository;

    public PanelService(VehiculoRepository vehiculoRepository,
                        CeldaRepository celdaRepository,
                        NovedadRepository novedadRepository,
                        MovimientoRepository movimientoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.celdaRepository = celdaRepository;
        this.novedadRepository = novedadRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public PanelMetricasDTO obtenerMetricas() {
        return PanelMetricasDTO.builder()
                .vehiculosPresentes(vehiculoRepository.countByEstadoPresencia("PRESENTE"))
                .celdasCarroDisponibles(celdaRepository.countByTipoPermitidoAndEstadoCelda("CARRO", "DISPONIBLE"))
                .celdasMotoDisponibles(celdaRepository.countByTipoPermitidoAndEstadoCelda("MOTO", "DISPONIBLE"))
                .novedadesAbiertas(novedadRepository.countByEstadoNovedad("ABIERTA"))
                .build();
    }

    public List<Movimiento> obtenerUltimosMovimientos() {
        return movimientoRepository.findTop10ByOrderByFechaHoraDesc();
    }

    public List<Novedad> obtenerNovedadesRecientes() {
        return novedadRepository.findAllByOrderByFechaHoraDesc();
    }
}
