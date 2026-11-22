package com.autos_colombia.service;

import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.DuenoRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bean auxiliar que procesa la inactivación por mora de UN solo dueño
 * en su propia transacción independiente (@Transactional en método público).
 * Así el fallo de un dueño no hace rollback del lote completo.
 */
@Service
public class MoraProcessorService {

    private static final Logger log = LoggerFactory.getLogger(MoraProcessorService.class);

    private final DuenoRepository duenoRepository;
    private final VehiculoRepository vehiculoRepository;

    public MoraProcessorService(DuenoRepository duenoRepository,
                                VehiculoRepository vehiculoRepository) {
        this.duenoRepository = duenoRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    /**
     * Inactiva por mora a un dueño y sus vehículos ACTIVO.
     * Reglas:
     *   - Si algún vehículo está PRESENTE → omitir y lanzar excepción para que el caller lo registre en log.
     *   - Solo los vehículos ACTIVO pasan a INACTIVO.
     *   - id_celda y estadoCelda NO se tocan: conservar la asociación permite que,
     *     al pagar la mora, el sistema reactive el vehículo en su celda original (T11).
     */
    @Transactional
    public void inactivarPorMora(Dueno dueno) {
        List<Vehiculo> vehiculos = vehiculoRepository.findByDuenoId(dueno.getId());

        // Verificar si algún vehículo está PRESENTE; si es así, omitir este dueño
        boolean tienePresente = vehiculos.stream()
                .anyMatch(v -> "PRESENTE".equalsIgnoreCase(v.getEstadoPresencia()));
        if (tienePresente) {
            throw new IllegalStateException(
                    "Dueño omitido por mora: tiene vehículo(s) PRESENTE. Se reintentará en la próxima ejecución.");
        }

        // Inactivar dueño
        dueno.setEstado("INACTIVO");
        duenoRepository.save(dueno);

        // Inactivar solo los vehículos ACTIVO; conservar id_celda y estadoCelda
        // para que el pago posterior (T11) pueda reactivarlos en su misma celda.
        for (Vehiculo v : vehiculos) {
            if ("ACTIVO".equalsIgnoreCase(v.getEstado())) {
                v.setEstado("INACTIVO");
                // id_celda y estadoCelda se conservan intencionalmente (ver T11 PagoService)
                vehiculoRepository.save(v);
            }
        }

        log.warn("[MORA] Inactivado: {} | vehículos procesados: {}", dueno.getNombreCompleto(), vehiculos.size());
    }
}
