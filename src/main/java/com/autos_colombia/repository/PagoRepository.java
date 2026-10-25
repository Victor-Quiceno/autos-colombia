package com.autos_colombia.repository;

import com.autos_colombia.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReferenciaTransaccion(String referenciaTransaccion);
    List<Pago> findAllByOrderByFechaPagoDesc();
    List<Pago> findByDuenoIdOrderByFechaPagoDesc(Long duenoId);
}
