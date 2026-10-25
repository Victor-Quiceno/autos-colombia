package com.autos_colombia.repository;

import com.autos_colombia.model.Dueno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DuenoRepository extends JpaRepository<Dueno, Long> {
    Optional<Dueno> findByNumeroDocumento(String numeroDocumento);
    Optional<Dueno> findByCorreo(String correo);
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByCorreo(String correo);
    List<Dueno> findByEstado(String estado);
    List<Dueno> findByFechaCorte(LocalDate fechaCorte);
    List<Dueno> findByFechaCorteBeforeAndEstado(LocalDate fechaCorte, String estado);
}
