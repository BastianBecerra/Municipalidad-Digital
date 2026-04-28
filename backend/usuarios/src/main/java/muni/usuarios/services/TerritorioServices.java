package muni.usuarios.services;

import lombok.RequiredArgsConstructor;
import muni.usuarios.entities.Territorio;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.TerritorioRepository;
import muni.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TerritorioServices {

    private final TerritorioRepository territorioRepository;
    private final UsuarioRepository usuarioRepository;

    // --- CRUD ---

    public List<Territorio> listarTodos() {
        return territorioRepository.findAll();
    }

    public Optional<Territorio> buscarPorId(Long id) {
        return territorioRepository.findById(id);
    }

    public Territorio guardar(Territorio territorio) {
        return territorioRepository.save(territorio);
    }

    public Territorio actualizar(Long id, Territorio datosActualizados) {
        Territorio existente = territorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Territorio no encontrado con ID: " + id));

        existente.setNombre(datosActualizados.getNombre());
        existente.setTipo(datosActualizados.getTipo());
        existente.setNumeroUnidadVecinal(datosActualizados.getNumeroUnidadVecinal());
        existente.setComuna(datosActualizados.getComuna());
        existente.setRegion(datosActualizados.getRegion());
        existente.setDireccionSede(datosActualizados.getDireccionSede());
        existente.setLatitud(datosActualizados.getLatitud());
        existente.setLongitud(datosActualizados.getLongitud());
        existente.setLimiteNorte(datosActualizados.getLimiteNorte());
        existente.setLimiteSur(datosActualizados.getLimiteSur());
        existente.setLimiteEste(datosActualizados.getLimiteEste());
        existente.setLimiteOeste(datosActualizados.getLimiteOeste());
        existente.setEmail(datosActualizados.getEmail());
        existente.setTelefono(datosActualizados.getTelefono());
        existente.setPresidente(datosActualizados.getPresidente());
        existente.setDescripcion(datosActualizados.getDescripcion());

        return territorioRepository.save(existente);
    }

    public void eliminar(Long id) {
        if (!territorioRepository.existsById(id)) {
            throw new RuntimeException("Territorio no encontrado con ID: " + id);
        }
        territorioRepository.deleteById(id);
    }

    // --- Consultas específicas ---

    public List<Territorio> listarPorComuna(String comuna) {
        return territorioRepository.findByComuna(comuna);
    }

    public List<Territorio> listarPorRegion(String region) {
        return territorioRepository.findByRegion(region);
    }

    public List<Territorio> listarPorTipo(String tipo) {
        return territorioRepository.findByTipo(tipo);
    }

    public List<Territorio> listarPorComunaYTipo(String comuna, String tipo) {
        return territorioRepository.findByComunaAndTipo(comuna, tipo);
    }

    public List<Territorio> listarActivos() {
        return territorioRepository.findByActivoTrue();
    }

    // --- Operaciones de estado ---

    public Territorio desactivar(Long id) {
        Territorio territorio = territorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Territorio no encontrado con ID: " + id));
        territorio.setActivo(false);
        return territorioRepository.save(territorio);
    }

    public Territorio activar(Long id) {
        Territorio territorio = territorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Territorio no encontrado con ID: " + id));
        territorio.setActivo(true);
        return territorioRepository.save(territorio);
    }

    // --- Asignación de vecinos ---

    public Usuarios asignarVecino(Long territorioId, Long usuarioId) {
        Territorio territorio = territorioRepository.findById(territorioId)
                .orElseThrow(() -> new RuntimeException("Territorio no encontrado con ID: " + territorioId));
        Usuarios usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setTerritorio(territorio);
        return usuarioRepository.save(usuario);
    }

    public Usuarios desasignarVecino(Long usuarioId) {
        Usuarios usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setTerritorio(null);
        return usuarioRepository.save(usuario);
    }

    public List<Usuarios> listarVecinos(Long territorioId) {
        Territorio territorio = territorioRepository.findById(territorioId)
                .orElseThrow(() -> new RuntimeException("Territorio no encontrado con ID: " + territorioId));
        return territorio.getVecinos();
    }
}
