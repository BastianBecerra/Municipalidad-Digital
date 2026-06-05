package muni.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.services.UsuarioServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuariosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioServices usuarioServices;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testListarTodos() throws Exception {
        Usuarios u = new Usuarios();
        u.setId(1L);
        u.setRut("123");

        when(usuarioServices.listarTodos()).thenReturn(List.of(u));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rut").value("123"));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testBuscarPorId_Found() throws Exception {
        Usuarios u = new Usuarios();
        u.setId(1L);
        when(usuarioServices.buscarPorId(1L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testBuscarPorId_NotFound() throws Exception {
        when(usuarioServices.buscarPorId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCrear() throws Exception {
        Usuarios u = new Usuarios();
        u.setRut("123");

        when(usuarioServices.guardar(any(Usuarios.class))).thenReturn(u);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActualizar() throws Exception {
        Usuarios u = new Usuarios();
        u.setRut("123");

        when(usuarioServices.actualizar(eq(1L), any(Usuarios.class))).thenReturn(u);

        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEliminar() throws Exception {
        doNothing().when(usuarioServices).eliminar(1L);

        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testBuscarPorRut_Found() throws Exception {
        Usuarios u = new Usuarios();
        u.setRut("123");
        when(usuarioServices.buscarPorRut("123")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/usuarios/rut/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("123"));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testBuscarPorRut_NotFound() throws Exception {
        when(usuarioServices.buscarPorRut("123")).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/rut/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testBuscarPorEmail_Found() throws Exception {
        Usuarios u = new Usuarios();
        u.setEmail("a@b.com");
        when(usuarioServices.buscarPorEmail("a@b.com")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/usuarios/email/a@b.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testBuscarPorEmail_NotFound() throws Exception {
        when(usuarioServices.buscarPorEmail("a@b.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/email/a@b.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testListarPorRol() throws Exception {
        when(usuarioServices.listarPorRol("VECINO")).thenReturn(List.of(new Usuarios()));

        mockMvc.perform(get("/usuarios/rol/VECINO"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testListarPorComuna() throws Exception {
        when(usuarioServices.listarPorComuna("Santiago")).thenReturn(List.of(new Usuarios()));

        mockMvc.perform(get("/usuarios/comuna/Santiago"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testListarActivos() throws Exception {
        when(usuarioServices.listarActivos()).thenReturn(List.of(new Usuarios()));

        mockMvc.perform(get("/usuarios/activos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDesactivar() throws Exception {
        when(usuarioServices.desactivar(1L)).thenReturn(new Usuarios());

        mockMvc.perform(patch("/usuarios/1/desactivar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivar() throws Exception {
        when(usuarioServices.activar(1L)).thenReturn(new Usuarios());

        mockMvc.perform(patch("/usuarios/1/activar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "myrut", roles = "VECINO")
    void testObtenerMiPerfil_Found() throws Exception {
        Usuarios u = new Usuarios();
        u.setRut("myrut");
        when(usuarioServices.buscarPorRut("myrut")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("myrut"));
    }

    @Test
    @WithMockUser(username = "myrut", roles = "VECINO")
    void testObtenerMiPerfil_NotFound() throws Exception {
        when(usuarioServices.buscarPorRut("myrut")).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "myrut", roles = "VECINO")
    void testActualizarMiPerfil_Success() throws Exception {
        Usuarios existente = new Usuarios();
        existente.setId(1L);
        existente.setRut("myrut");

        Usuarios nuevosDatos = new Usuarios();
        nuevosDatos.setNombres("Nuevo");

        when(usuarioServices.buscarPorRut("myrut")).thenReturn(Optional.of(existente));
        when(usuarioServices.actualizar(eq(1L), any(Usuarios.class))).thenReturn(nuevosDatos);

        mockMvc.perform(put("/usuarios/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevosDatos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Nuevo"));
    }

    @Test
    @WithMockUser(username = "myrut", roles = "VECINO")
    void testActualizarMiPerfil_NotFound() throws Exception {
        Usuarios nuevosDatos = new Usuarios();
        when(usuarioServices.buscarPorRut("myrut")).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            mockMvc.perform(put("/usuarios/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nuevosDatos)))
        ).hasCauseInstanceOf(RuntimeException.class);
    }
}
