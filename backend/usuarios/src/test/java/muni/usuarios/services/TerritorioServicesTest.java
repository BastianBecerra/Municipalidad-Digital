package muni.usuarios.services;

import muni.usuarios.entities.Territorio;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.TerritorioRepository;
import muni.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerritorioServicesTest {

    @Mock
    private TerritorioRepository territorioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TerritorioServices territorioServices;

    @Test
    void testListarTodos() {
        List<Territorio> territorios = List.of(new Territorio(), new Territorio());
        when(territorioRepository.findAll()).thenReturn(territorios);
        
        List<Territorio> result = territorioServices.listarTodos();
        assertThat(result).hasSize(2);
        verify(territorioRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId() {
        Territorio t = new Territorio();
        when(territorioRepository.findById(1L)).thenReturn(Optional.of(t));
        
        Optional<Territorio> result = territorioServices.buscarPorId(1L);
        assertThat(result).isPresent().contains(t);
    }

    @Test
    void testGuardar() {
        Territorio t = new Territorio();
        when(territorioRepository.save(t)).thenReturn(t);
        
        Territorio result = territorioServices.guardar(t);
        assertThat(result).isNotNull();
    }

    @Test
    void testActualizar_Success() {
        Territorio existente = new Territorio();
        existente.setId(1L);
        existente.setNombre("Viejo");

        Territorio actualizados = new Territorio();
        actualizados.setNombre("Nuevo");
        actualizados.setTipo("Tipo");
        actualizados.setNumeroUnidadVecinal("12");
        actualizados.setComuna("Santiago");
        actualizados.setRegion("RM");
        actualizados.setDireccionSede("Sede");
        actualizados.setLatitud(1.23);
        actualizados.setLongitud(4.56);
        actualizados.setLimiteNorte("Norte");
        actualizados.setLimiteSur("Sur");
        actualizados.setLimiteEste("Este");
        actualizados.setLimiteOeste("Oeste");
        actualizados.setEmail("a@b.com");
        actualizados.setTelefono("9999");
        actualizados.setPresidente("Pres");
        actualizados.setDescripcion("Desc");

        when(territorioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(territorioRepository.save(any(Territorio.class))).thenAnswer(i -> i.getArgument(0));

        Territorio result = territorioServices.actualizar(1L, actualizados);
        assertThat(result.getNombre()).isEqualTo("Nuevo");
        assertThat(result.getTipo()).isEqualTo("Tipo");
    }

    @Test
    void testActualizar_NotFound() {
        when(territorioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> territorioServices.actualizar(1L, new Territorio()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Territorio no encontrado");
    }

    @Test
    void testEliminar_Success() {
        when(territorioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(territorioRepository).deleteById(1L);

        territorioServices.eliminar(1L);
        verify(territorioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminar_NotFound() {
        when(territorioRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> territorioServices.eliminar(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testListarPorComuna() {
        territorioServices.listarPorComuna("Stgo");
        verify(territorioRepository).findByComuna("Stgo");
    }

    @Test
    void testListarPorRegion() {
        territorioServices.listarPorRegion("RM");
        verify(territorioRepository).findByRegion("RM");
    }

    @Test
    void testListarPorTipo() {
        territorioServices.listarPorTipo("Tipo");
        verify(territorioRepository).findByTipo("Tipo");
    }

    @Test
    void testListarPorComunaYTipo() {
        territorioServices.listarPorComunaYTipo("Stgo", "Tipo");
        verify(territorioRepository).findByComunaAndTipo("Stgo", "Tipo");
    }

    @Test
    void testListarActivos() {
        territorioServices.listarActivos();
        verify(territorioRepository).findByActivoTrue();
    }

    @Test
    void testDesactivar() {
        Territorio t = new Territorio();
        t.setActivo(true);
        when(territorioRepository.findById(1L)).thenReturn(Optional.of(t));
        when(territorioRepository.save(t)).thenReturn(t);

        Territorio result = territorioServices.desactivar(1L);
        assertThat(result.getActivo()).isFalse();
    }

    @Test
    void testDesactivar_NotFound() {
        when(territorioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> territorioServices.desactivar(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testActivar() {
        Territorio t = new Territorio();
        t.setActivo(false);
        when(territorioRepository.findById(1L)).thenReturn(Optional.of(t));
        when(territorioRepository.save(t)).thenReturn(t);

        Territorio result = territorioServices.activar(1L);
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    void testActivar_NotFound() {
        when(territorioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> territorioServices.activar(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testAsignarVecino_Success() {
        Territorio t = new Territorio();
        Usuarios u = new Usuarios();
        when(territorioRepository.findById(1L)).thenReturn(Optional.of(t));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);

        Usuarios result = territorioServices.asignarVecino(1L, 2L);
        assertThat(result.getTerritorio()).isEqualTo(t);
    }

    @Test
    void testAsignarVecino_TerritorioNotFound() {
        when(territorioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> territorioServices.asignarVecino(1L, 2L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testAsignarVecino_UsuarioNotFound() {
        when(territorioRepository.findById(1L)).thenReturn(Optional.of(new Territorio()));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> territorioServices.asignarVecino(1L, 2L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testDesasignarVecino_Success() {
        Usuarios u = new Usuarios();
        u.setTerritorio(new Territorio());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);

        Usuarios result = territorioServices.desasignarVecino(2L);
        assertThat(result.getTerritorio()).isNull();
    }

    @Test
    void testDesasignarVecino_UsuarioNotFound() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> territorioServices.desasignarVecino(2L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testListarVecinos_Success() {
        Territorio t = new Territorio();
        List<Usuarios> vecinos = new ArrayList<>();
        t.setVecinos(vecinos);
        when(territorioRepository.findById(1L)).thenReturn(Optional.of(t));

        List<Usuarios> result = territorioServices.listarVecinos(1L);
        assertThat(result).isSameAs(vecinos);
    }

    @Test
    void testListarVecinos_TerritorioNotFound() {
        when(territorioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> territorioServices.listarVecinos(1L))
                .isInstanceOf(RuntimeException.class);
    }
}
