package com.autos_colombia.service;

import com.autos_colombia.dto.DuenoDTO;
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
public class DuenoService {

    private final DuenoRepository duenoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final CeldaRepository celdaRepository;

    public DuenoService(DuenoRepository duenoRepository,
                        VehiculoRepository vehiculoRepository,
                        CeldaRepository celdaRepository) {
        this.duenoRepository = duenoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.celdaRepository = celdaRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculosDeDueno(Long idDueno) {
        return vehiculoRepository.findByDuenoId(idDueno);
    }

    @Transactional(readOnly = true)
    public List<Dueno> obtenerTodos() {
        return duenoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Dueno> obtenerPorEstado(String estado) {
        return duenoRepository.findByEstado(estado.toUpperCase());
    }

    @Transactional(readOnly = true)
    public Dueno obtenerPorId(Long id) {
        return duenoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dueño no encontrado con ID: " + id));
    }

    public Dueno registrarDueno(DuenoDTO dto) {
        if (duenoRepository.existsByNumeroDocumento(dto.getNumeroDocumento().trim())) {
            throw new IllegalArgumentException("Ya existe un dueño con el documento " + dto.getNumeroDocumento());
        }
        if (duenoRepository.existsByCorreo(dto.getCorreo().trim())) {
            throw new IllegalArgumentException("Ya existe un dueño con el correo " + dto.getCorreo());
        }

        LocalDate hoy = LocalDate.now();
        Dueno dueno = Dueno.builder()
                .tipoDocumento(dto.getTipoDocumento().toUpperCase())
                .numeroDocumento(dto.getNumeroDocumento().trim())
                .nombres(dto.getNombres().trim())
                .apellidos(dto.getApellidos().trim())
                .telefono(dto.getTelefono().trim())
                .correo(dto.getCorreo().trim())
                .fechaVinculacion(hoy)
                .fechaCorte(hoy.plusMonths(1)) // HU02 Criterio 3: primera fecha de corte a 1 mes calendario
                .estado("ACTIVO")
                .build();

        return duenoRepository.save(dueno);
    }

    public Dueno actualizarDueno(Long id, DuenoDTO dto) {
        Dueno dueno = obtenerPorId(id);

        dueno.setNombres(dto.getNombres().trim());
        dueno.setApellidos(dto.getApellidos().trim());
        dueno.setTelefono(dto.getTelefono().trim());
        dueno.setCorreo(dto.getCorreo().trim());

        return duenoRepository.save(dueno);
    }

    public void inhabilitarDueno(Long id) {
        Dueno dueno = obtenerPorId(id);
        List<Vehiculo> vehiculos = vehiculoRepository.findByDuenoId(id);

        // HU10 Criterio 5 / T2: Bloqueo si algún vehículo del dueño se encuentra Presente
        for (Vehiculo v : vehiculos) {
            if ("PRESENTE".equalsIgnoreCase(v.getEstadoPresencia())) {
                throw new IllegalStateException("No se puede inhabilitar al cliente " + dueno.getNombreCompleto() +
                        ": el vehículo asociado " + v.getPlaca() + " se encuentra Presente en el parqueadero.");
            }
        }

        dueno.setEstado("INACTIVO");
        duenoRepository.save(dueno);

        // T4 Cascada: Por cada vehículo: INACTIVO + celda DISPONIBLE + id_celda null
        for (Vehiculo v : vehiculos) {
            v.setEstado("INACTIVO");
            if (v.getCelda() != null) {
                Celda c = v.getCelda();
                c.setEstadoCelda("DISPONIBLE");
                celdaRepository.save(c);
                v.setCelda(null);
            }
            vehiculoRepository.save(v);
        }
    }

    public void activarDueno(Long id) {
        Dueno dueno = obtenerPorId(id);
        dueno.setEstado("ACTIVO");
        duenoRepository.save(dueno);
        // HU10 Criterio 6 / T5: Reactivar un dueño NO reactiva sus vehículos automáticamente
    }
}
