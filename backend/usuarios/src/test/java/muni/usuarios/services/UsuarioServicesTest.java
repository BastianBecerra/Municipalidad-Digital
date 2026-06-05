package muni.usuarios.services;

import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServicesTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServices usuarioServices;

    @Test
    void testListarTodos() {
        List<Usuarios> usuarios = List.of(new Usuarios(), new Usuarios());
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<Usuarios> result = usuarioServices.listarTodos();
        assertThat(result).hasSize(2);
    }

    @Test
    void testBuscarPorId() {
        Usuarios u = new Usuarios();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        Optional<Usuarios> result = usuarioServices.buscarPorId(1L);
        assertThat(result).isPresent().contains(u);
    }

    @Test
    void testGuardar_Success() {
        Usuarios u = new Usuarios();
        u.setRut("12345678-9");
        u.setEmail("user@example.com");

        when(usuarioRepository.existsByRut(u.getRut())).thenReturn(false);
        when(usuarioRepository.existsByEmail(u.getEmail())).thenReturn(false);
        when(usuarioRepository.save(u)).thenReturn(u);

        Usuarios result = usuarioServices.guardar(u);
        assertThat(result).isNotNull();
    }

    @Test
    void testGuardar_DuplicateRut() {
        Usuarios u = new Usuarios();
        u.setRut("12345678-9");

        when(usuarioRepository.existsByRut(u.getRut())).thenReturn(true);

        assertThatThrownBy(() -> usuarioServices.guardar(u))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un usuario con el RUT");
    }

    @Test
    void testGuardar_DuplicateEmail() {
        Usuarios u = new Usuarios();
        u.setRut("12345678-9");
        u.setEmail("user@example.com");

        when(usuarioRepository.existsByRut(u.getRut())).thenReturn(false);
        when(usuarioRepository.existsByEmail(u.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> usuarioServices.guardar(u))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un usuario con el email");
    }

    @Test
    void testActualizar_Success() {
        Usuarios existente = new Usuarios();
        existente.setId(1L);
        existente.setNombres("Nombre viejo");

        Usuarios nuevosDatos = new Usuarios();
        nuevosDatos.setNombres("Nombre nuevo");
        nuevosDatos.setApellidoPaterno("Paterno");
        nuevosDatos.setApellidoMaterno("Materno");
        nuevosDatos.setFechaNacimiento(java.time.LocalDate.now());
        nuevosDatos.setGenero("Masculino");
        nuevosDatos.setEmail("a@b.com");
        nuevosDatos.setTelefono("123");
        nuevosDatos.setDireccion("Dir");
        nuevosDatos.setComuna("Comuna");
        nuevosDatos.setRegion("Region");
        nuevosDatos.setRol("ROL");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuarios.class))).thenAnswer(i -> i.getArgument(0));

        Usuarios result = usuarioServices.actualizar(1L, nuevosDatos);
        assertThat(result.getNombres()).isEqualTo("Nombre nuevo");
        assertThat(result.getApellidoPaterno()).isEqualTo("Paterno");
    }

    @Test
    void testActualizar_NotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioServices.actualizar(1L, new Usuarios()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void testEliminar_Success() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioServices.eliminar(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminar_NotFound() {
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> usuarioServices.eliminar(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testBuscarPorRut() {
        usuarioServices.buscarPorRut("123");
        verify(usuarioRepository).findByRut("123");
    }

    @Test
    void testBuscarPorEmail() {
        usuarioServices.buscarPorEmail("a@b.com");
        verify(usuarioRepository).findByEmail("a@b.com");
    }

    @Test
    void testListarPorRol() {
        usuarioServices.listarPorRol("ADMIN");
        verify(usuarioRepository).findByRol("ADMIN");
    }

    @Test
    void testListarPorComuna() {
        usuarioServices.listarPorComuna("Stgo");
        verify(usuarioRepository).findByComuna("Stgo");
    }

    @Test
    void testListarActivos() {
        usuarioServices.listarActivos();
        verify(usuarioRepository).findByActivoTrue();
    }

    @Test
    void testDesactivar_Success() {
        Usuarios u = new Usuarios();
        u.setActivo(true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);

        Usuarios result = usuarioServices.desactivar(1L);
        assertThat(result.getActivo()).isFalse();
    }

    @Test
    void testDesactivar_NotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioServices.desactivar(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testActivar_Success() {
        Usuarios u = new Usuarios();
        u.setActivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);

        Usuarios result = usuarioServices.activar(1L);
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    void testActivar_NotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioServices.activar(1L))
                .isInstanceOf(RuntimeException.class);
    }
}
