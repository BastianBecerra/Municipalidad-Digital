package muni.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni.usuarios.entities.Territorio;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.services.TerritorioServices;
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
class TerritorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TerritorioServices territorioServices;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "VECINO")
    void testListarTodos() throws Exception {
        Territorio t = new Territorio();
        t.setId(1L);
        t.setNombre("Test");

        when(territorioServices.listarTodos()).thenReturn(List.of(t));

        mockMvc.perform(get("/territorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Test"));
    }

    @Test
    @WithMockUser(roles = "VECINO")
    void testBuscarPorId_Found() throws Exception {
        Territorio t = new Territorio();
        t.setId(1L);
        when(territorioServices.buscarPorId(1L)).thenReturn(Optional.of(t));

        mockMvc.perform(get("/territorios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "VECINO")
    void testBuscarPorId_NotFound() throws Exception {
        when(territorioServices.buscarPorId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/territorios/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCrear_Authorized() throws Exception {
        Territorio t = new Territorio();
        t.setNombre("Nuevo");

        when(territorioServices.guardar(any(Territorio.class))).thenReturn(t);

        mockMvc.perform(post("/territorios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testCrear_UnauthorizedRole() throws Exception {
        mockMvc.perform(post("/territorios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActualizar() throws Exception {
        Territorio t = new Territorio();
        t.setNombre("Actualizado");

        when(territorioServices.actualizar(eq(1L), any(Territorio.class))).thenReturn(t);

        mockMvc.perform(put("/territorios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEliminar() throws Exception {
        doNothing().when(territorioServices).eliminar(1L);

        mockMvc.perform(delete("/territorios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VECINO")
    void testListarPorComuna() throws Exception {
        when(territorioServices.listarPorComuna("Santiago")).thenReturn(List.of(new Territorio()));

        mockMvc.perform(get("/territorios/comuna/Santiago"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VECINO")
    void testListarPorRegion() throws Exception {
        when(territorioServices.listarPorRegion("RM")).thenReturn(List.of(new Territorio()));

        mockMvc.perform(get("/territorios/region/RM"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VECINO")
    void testListarPorTipo() throws Exception {
        when(territorioServices.listarPorTipo("Tipo")).thenReturn(List.of(new Territorio()));

        mockMvc.perform(get("/territorios/tipo/Tipo"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VECINO")
    void testListarActivos() throws Exception {
        when(territorioServices.listarActivos()).thenReturn(List.of(new Territorio()));

        mockMvc.perform(get("/territorios/activos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDesactivar() throws Exception {
        when(territorioServices.desactivar(1L)).thenReturn(new Territorio());

        mockMvc.perform(patch("/territorios/1/desactivar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivar() throws Exception {
        when(territorioServices.activar(1L)).thenReturn(new Territorio());

        mockMvc.perform(patch("/territorios/1/activar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testAsignarVecino() throws Exception {
        when(territorioServices.asignarVecino(1L, 2L)).thenReturn(new Usuarios());

        mockMvc.perform(patch("/territorios/1/vecinos/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testDesasignarVecino() throws Exception {
        when(territorioServices.desasignarVecino(2L)).thenReturn(new Usuarios());

        mockMvc.perform(delete("/territorios/vecinos/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void testListarVecinos() throws Exception {
        when(territorioServices.listarVecinos(1L)).thenReturn(List.of(new Usuarios()));

        mockMvc.perform(get("/territorios/1/vecinos"))
                .andExpect(status().isOk());
    }
}
