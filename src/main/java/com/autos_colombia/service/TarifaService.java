package com.autos_colombia.service;

import com.autos_colombia.dto.TarifaDTO;
import com.autos_colombia.model.Tarifa;
import com.autos_colombia.repository.TarifaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    @Transactional(readOnly = true)
    public List<Tarifa> obtenerTodas() {
        return tarifaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerValorTarifa(String tipoVehiculo) {
        return tarifaRepository.findByTipoVehiculo(tipoVehiculo)
                .map(Tarifa::getValorMensualidad)
                .orElse(BigDecimal.ZERO);
    }

    public Tarifa actualizarTarifa(TarifaDTO dto) {
        Tarifa tarifa = tarifaRepository.findByTipoVehiculo(dto.getTipoVehiculo().toUpperCase())
                .orElse(Tarifa.builder()
                        .tipoVehiculo(dto.getTipoVehiculo().toUpperCase())
                        .build());

        tarifa.setValorMensualidad(dto.getValorMensualidad());
        tarifa.setFechaActualizacion(LocalDate.now());

        return tarifaRepository.save(tarifa);
    }
}
