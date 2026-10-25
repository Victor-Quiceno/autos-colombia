package com.autos_colombia.repository;

import com.autos_colombia.model.Celda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CeldaRepository extends JpaRepository<Celda, Long> {
    Optional<Celda> findByCodigoCelda(String codigoCelda);
    boolean existsByCodigoCelda(String codigoCelda);
    List<Celda> findByEstadoCelda(String estadoCelda);
    List<Celda> findByTipoPermitidoAndEstadoCelda(String tipoPermitido, String estadoCelda);
    long countByTipoPermitidoAndEstadoCelda(String tipoPermitido, String estadoCelda);
}
