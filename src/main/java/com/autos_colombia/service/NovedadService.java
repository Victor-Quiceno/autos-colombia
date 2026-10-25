package com.autos_colombia.service;

import com.autos_colombia.dto.NovedadDTO;
import com.autos_colombia.model.Novedad;
import com.autos_colombia.model.Usuario;
import com.autos_colombia.model.Vehiculo;
import com.autos_colombia.repository.NovedadRepository;
import com.autos_colombia.repository.UsuarioRepository;
import com.autos_colombia.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NovedadService {

    private final NovedadRepository novedadRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public NovedadService(NovedadRepository novedadRepository,
                          VehiculoRepository vehiculoRepository,
                          UsuarioRepository usuarioRepository) {
        this.novedadRepository = novedadRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Novedad> obtenerTodas() {
        return novedadRepository.findAllByOrderByFechaHoraDesc();
    }

    public Novedad registrarNovedad(NovedadDTO dto, String usernameOperario) {
        String placa = dto.getPlaca().trim().toUpperCase();

        Vehiculo vehiculo = vehiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo con placa " + placa + " no encontrado."));

        Usuario usuario = usuarioRepository.findByUsername(usernameOperario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no válido para registrar la novedad."));

        Novedad novedad = Novedad.builder()
                .vehiculo(vehiculo)
                .usuario(usuario)
                .tipoNovedad(dto.getTipoNovedad().toUpperCase())
                .descripcion(dto.getDescripcion().trim())
                .nivelSeveridad(dto.getNivelSeveridad().toUpperCase())
                .fechaHora(LocalDateTime.now())
                .estadoNovedad("ABIERTA")
                .build();

        return novedadRepository.save(novedad);
    }

    public void cambiarEstadoNovedad(Long idNovedad, String nuevoEstado) {
        Novedad novedad = novedadRepository.findById(idNovedad)
                .orElseThrow(() -> new IllegalArgumentException("Novedad no encontrada"));
        novedad.setEstadoNovedad(nuevoEstado.toUpperCase());
        novedadRepository.save(novedad);
    }
}
