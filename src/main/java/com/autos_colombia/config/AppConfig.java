package com.autos_colombia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuración de beans de infraestructura compartidos.
 * El Clock de sistema se inyecta en los servicios para permitir
 * que los tests lo sobrescriban con un clock fijo sin modificar código de producción.
 */
@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
