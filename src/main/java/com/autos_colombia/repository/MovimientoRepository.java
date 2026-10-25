package com.autos_colombia.repository;

import com.autos_colombia.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findTop10ByOrderByFechaHoraDesc();
    List<Movimiento> findAllByOrderByFechaHoraDesc();
}
