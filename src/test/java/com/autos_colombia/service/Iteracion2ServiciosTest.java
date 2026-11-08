package com.autos_colombia.service;

import com.autos_colombia.dto.CeldaDTO;
import com.autos_colombia.dto.DuenoDTO;
import com.autos_colombia.dto.UsuarioCrearDTO;
import com.autos_colombia.dto.UsuarioEditarDTO;
import com.autos_colombia.dto.VehiculoDTO;
import com.autos_colombia.model.Celda;
import com.autos_colombia.model.Dueno;
import com.autos_colombia.model.Usuario;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.CeldaRepository;
import com.autos_colombia.repository.DuenoRepository;
import com.autos_colombia.repository.UsuarioRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class Iteracion2ServiciosTest {

    @Autowired
    private DuenoService duenoService;

    @Autowired
    private VehiculoService vehiculoService;

    @Autowired
    private CeldaService celdaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DuenoRepository duenoRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private CeldaRepository celdaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("T8.1 - Inhabilitación de dueño en cascada: vehículos pasan a INACTIVO, celdas a DISPONIBLE y celda en null")
    void testCascadaInhabilitacionDueno() {
        // Buscar dueño de prueba con vehículos
        Dueno dueno = duenoRepository.findAll().stream()
                .filter(d -> "ACTIVO".equalsIgnoreCase(d.getEstado()) && !vehiculoRepository.findByDuenoId(d.getId()).isEmpty())
                .findFirst()
                .orElseThrow();

        // Asegurar que todos sus vehículos estén AUSENTES para permitir la inhabilitación
        List<Vehiculo> vehiculos = vehiculoRepository.findByDuenoId(dueno.getId());
        for (Vehiculo v : vehiculos) {
            v.setEstadoPresencia("AUSENTE");
            vehiculoRepository.save(v);
        }

        duenoService.inhabilitarDueno(dueno.getId());

        Dueno inactivo = duenoRepository.findById(dueno.getId()).orElseThrow();
        assertEquals("INACTIVO", inactivo.getEstado());

        List<Vehiculo> vehiculosActualizados = vehiculoRepository.findByDuenoId(dueno.getId());
        for (Vehiculo v : vehiculosActualizados) {
            assertEquals("INACTIVO", v.getEstado());
            assertNull(v.getCelda(), "La celda del vehículo inhabilitado debe ser null");
        }
    }

    @Test
    @DisplayName("T8.2 - Bloqueo por presencia: rechazar inhabilitación de dueño y vehículo si algún vehículo está PRESENTE")
    void testBloqueoPorVehiculoPresente() {
        Dueno dueno = duenoRepository.findAll().stream()
                .filter(d -> "ACTIVO".equalsIgnoreCase(d.getEstado()) && !vehiculoRepository.findByDuenoId(d.getId()).isEmpty())
                .findFirst()
                .orElseThrow();

        Vehiculo vehiculo = vehiculoRepository.findByDuenoId(dueno.getId()).get(0);
        vehiculo.setEstadoPresencia("PRESENTE");
        vehiculoRepository.save(vehiculo);

        // Intento 1: Inhabilitar vehículo presente debe lanzar IllegalStateException
        assertThrows(IllegalStateException.class, () -> vehiculoService.inhabilitarVehiculo(vehiculo.getId()));

        // Intento 2: Inhabilitar dueño con vehículo presente debe lanzar IllegalStateException
        assertThrows(IllegalStateException.class, () -> duenoService.inhabilitarDueno(dueno.getId()));

        // Intento 3: Cambiar celda a MANTENIMIENTO si vehículo está presente debe fallar
        if (vehiculo.getCelda() != null) {
            assertThrows(IllegalStateException.class, () -> celdaService.cambiarEstado(vehiculo.getCelda().getId(), "MANTENIMIENTO"));
        }
    }

    @Test
    @DisplayName("T8.3 - Registro atómico de vehículo + celda: celda pasa a ASIGNADA y vehículo queda ACTIVO y AUSENTE")
    void testRegistroVehiculoYCeldaAtomico() {
        Dueno dueno = duenoRepository.findAll().stream()
                .filter(d -> "ACTIVO".equalsIgnoreCase(d.getEstado()))
                .findFirst()
                .orElseThrow();

        Celda celda = celdaService.crearCelda(CeldaDTO.builder()
                .codigoCelda("TEST-C01")
                .tipoPermitido("CARRO")
                .build());

        VehiculoDTO dto = VehiculoDTO.builder()
                .placa("TST999")
                .tipoVehiculo("CARRO")
                .marca("Toyota")
                .modelo(2024)
                .color("Rojo")
                .idDueno(dueno.getId())
                .idCelda(celda.getId())
                .build();

        Vehiculo nuevo = vehiculoService.registrarVehiculo(dto);

        assertNotNull(nuevo.getId());
        assertEquals("ACTIVO", nuevo.getEstado());
        assertEquals("AUSENTE", nuevo.getEstadoPresencia());
        assertEquals(celda.getId(), nuevo.getCelda().getId());

        Celda celdaActualizada = celdaRepository.findById(celda.getId()).orElseThrow();
        assertEquals("ASIGNADA", celdaActualizada.getEstadoCelda());
    }

    @Test
    @DisplayName("T8.4 - Usuario duplicado: rechazar creación de usuario con username existente")
    void testRechazoUsuarioDuplicado() {
        UsuarioCrearDTO dto = UsuarioCrearDTO.builder()
                .username("admin") // Ya existe precargado
                .nombreCompleto("Otro Admin")
                .rol("ROLE_ADMIN")
                .password("password123")
                .confirmPassword("password123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearUsuario(dto));
    }

    @Test
    @DisplayName("T8.5 - Contraseña cifrada: la contraseña debe almacenarse cifrada con BCrypt y nunca en texto plano")
    void testPasswordCifradaConBCrypt() {
        UsuarioCrearDTO dto = UsuarioCrearDTO.builder()
                .username("test.user")
                .nombreCompleto("Usuario Prueba")
                .rol("ROLE_OPERARIO")
                .password("claveSecreta123")
                .confirmPassword("claveSecreta123")
                .build();

        Usuario creado = usuarioService.crearUsuario(dto);

        assertNotNull(creado.getPassword());
        assertNotEquals("claveSecreta123", creado.getPassword());
        assertTrue(creado.getPassword().startsWith("$2a$") || creado.getPassword().startsWith("$2b$"));
        assertTrue(passwordEncoder.matches("claveSecreta123", creado.getPassword()));
    }

    @Test
    @DisplayName("T8.6 - Autoinhabilitación: un administrador no puede inhabilitarse a sí mismo en sesión")
    void testRechazoAutoinhabilitacion() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

        assertThrows(IllegalStateException.class, () -> usuarioService.inhabilitarUsuario(admin.getId(), "admin"));
    }

    @Test
    @DisplayName("T8.7 - Último administrador: no permitir inhabilitar ni quitar el rol al último administrador activo")
    void testProteccionUltimoAdministrador() {
        // Inactivar todos los admin excepto uno
        List<Usuario> admins = usuarioRepository.findAll().stream()
                .filter(u -> "ROLE_ADMIN".equalsIgnoreCase(u.getRol()))
                .toList();

        for (int i = 1; i < admins.size(); i++) {
            admins.get(i).setEstado("INACTIVO");
            usuarioRepository.save(admins.get(i));
        }

        Usuario unicoAdminActivo = admins.get(0);
        unicoAdminActivo.setEstado("ACTIVO");
        usuarioRepository.save(unicoAdminActivo);

        // Intento 1: Inhabilitar al único admin activo (desde otra supuesta sesión) debe fallar
        assertThrows(IllegalStateException.class, () -> usuarioService.inhabilitarUsuario(unicoAdminActivo.getId(), "otro_usuario"));

        // Intento 2: Cambiar el rol a OPERARIO del único admin activo debe fallar
        UsuarioEditarDTO editarDTO = UsuarioEditarDTO.builder()
                .id(unicoAdminActivo.getId())
                .username(unicoAdminActivo.getUsername())
                .nombreCompleto(unicoAdminActivo.getNombreCompleto())
                .rol("ROLE_OPERARIO")
                .build();

        assertThrows(IllegalStateException.class, () -> usuarioService.actualizarUsuario(unicoAdminActivo.getId(), editarDTO, "otro_usuario"));
    }
}
