package muni.usuarios.services;

import lombok.RequiredArgsConstructor;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServices {

    private final UsuarioRepository usuarioRepository;

    // --- CRUD básico ---

    public List<Usuarios> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuarios> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuarios guardar(Usuarios usuario) {
        // Validar que no exista RUT duplicado
        if (usuarioRepository.existsByRut(usuario.getRut())) {
            throw new RuntimeException("Ya existe un usuario con el RUT: " + usuario.getRut());
        }
        // Validar que no exista email duplicado
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con el email: " + usuario.getEmail());
        }
        return usuarioRepository.save(usuario);
    }

    public Usuarios actualizar(Long id, Usuarios datosActualizados) {
        Usuarios existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        existente.setNombres(datosActualizados.getNombres());
        existente.setApellidoPaterno(datosActualizados.getApellidoPaterno());
        existente.setApellidoMaterno(datosActualizados.getApellidoMaterno());
        existente.setFechaNacimiento(datosActualizados.getFechaNacimiento());
        existente.setGenero(datosActualizados.getGenero());
        existente.setEmail(datosActualizados.getEmail());
        existente.setTelefono(datosActualizados.getTelefono());
        existente.setDireccion(datosActualizados.getDireccion());
        existente.setComuna(datosActualizados.getComuna());
        existente.setRegion(datosActualizados.getRegion());
        existente.setRol(datosActualizados.getRol());

        return usuarioRepository.save(existente);
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- Consultas específicas ---

    public Optional<Usuarios> buscarPorRut(String rut) {
        return usuarioRepository.findByRut(rut);
    }

    public Optional<Usuarios> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuarios> listarPorRol(String rol) {
        return usuarioRepository.findByRol(rol);
    }

    public List<Usuarios> listarPorComuna(String comuna) {
        return usuarioRepository.findByComuna(comuna);
    }

    public List<Usuarios> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    // --- Operaciones de estado ---

    public Usuarios desactivar(Long id) {
        Usuarios usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(false);
        return usuarioRepository.save(usuario);
    }

    public Usuarios activar(Long id) {
        Usuarios usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }
}
