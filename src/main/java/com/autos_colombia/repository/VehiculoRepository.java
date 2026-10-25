package com.autos_colombia.repository;

import com.autos_colombia.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByPlaca(String placa);
    boolean existsByPlaca(String placa);
    List<Vehiculo> findByEstado(String estado);
    List<Vehiculo> findByDuenoId(Long duenoId);
    List<Vehiculo> findByDuenoIdAndEstado(Long duenoId, String estado);
    long countByEstadoPresencia(String estadoPresencia);
}
