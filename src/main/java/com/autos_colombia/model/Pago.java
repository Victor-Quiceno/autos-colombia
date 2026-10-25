package com.autos_colombia.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_dueno", nullable = false)
    private Dueno dueno;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "cantidad_vehiculos", nullable = false)
    private Integer cantidadVehiculos;

    @Column(name = "referencia_transaccion", nullable = false, unique = true, length = 60)
    private String referenciaTransaccion;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago; // PSE, TARJETA_CREDITO, TARJETA_DEBITO, EFECTIVO

    @Column(name = "estado_pago", nullable = false, length = 20)
    @Builder.Default
    private String estadoPago = "APROBADO"; // APROBADO, RECHAZADO
}
