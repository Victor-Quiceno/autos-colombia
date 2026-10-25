package com.autos_colombia.repository;

import com.autos_colombia.model.Novedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NovedadRepository extends JpaRepository<Novedad, Long> {
    List<Novedad> findAllByOrderByFechaHoraDesc();
    List<Novedad> findByEstadoNovedad(String estadoNovedad);
    long countByEstadoNovedad(String estadoNovedad);
}
