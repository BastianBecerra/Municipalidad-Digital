package muni.usuarios.controller;

import lombok.RequiredArgsConstructor;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.services.UsuarioServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuariosController {

    private final UsuarioServices usuarioServices;

    // GET /usuarios — Listar todos
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping
    public ResponseEntity<List<Usuarios>> listarTodos() {
        return ResponseEntity.ok(usuarioServices.listarTodos());
    }

    // GET /usuarios/{id} — Buscar por ID
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<Usuarios> buscarPorId(@PathVariable Long id) {
        return usuarioServices.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /usuarios — Crear usuario
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Usuarios> crear(@RequestBody Usuarios usuario) {
        Usuarios nuevo = usuarioServices.guardar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT /usuarios/{id} — Actualizar usuario
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Usuarios> actualizar(@PathVariable Long id, @RequestBody Usuarios usuario) {
        Usuarios actualizado = usuarioServices.actualizar(id, usuario);
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /usuarios/{id} — Eliminar usuario
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioServices.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints específicos ---

    // GET /api/usuarios/rut/{rut} — Buscar por RUT
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/rut/{rut}")
    public ResponseEntity<Usuarios> buscarPorRut(@PathVariable String rut) {
        return usuarioServices.buscarPorRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/usuarios/email/{email} — Buscar por email
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuarios> buscarPorEmail(@PathVariable String email) {
        return usuarioServices.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/usuarios/rol/{rol} — Listar por rol
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Usuarios>> listarPorRol(@PathVariable String rol) {
        return ResponseEntity.ok(usuarioServices.listarPorRol(rol));
    }

    // GET /api/usuarios/comuna/{comuna} — Listar por comuna
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/comuna/{comuna}")
    public ResponseEntity<List<Usuarios>> listarPorComuna(@PathVariable String comuna) {
        return ResponseEntity.ok(usuarioServices.listarPorComuna(comuna));
    }

    // GET /api/usuarios/activos — Listar solo activos
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/activos")
    public ResponseEntity<List<Usuarios>> listarActivos() {
        return ResponseEntity.ok(usuarioServices.listarActivos());
    }

    // PATCH /api/usuarios/{id}/desactivar — Desactivar usuario
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Usuarios> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioServices.desactivar(id));
    }

    // PATCH /api/usuarios/{id}/activar — Activar usuario
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Usuarios> activar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioServices.activar(id));
    }

    // --- Perfil Propio (/me) ---

    // GET /usuarios/me — Obtener perfil propio
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')")
    @GetMapping("/me")
    public ResponseEntity<Usuarios> obtenerMiPerfil(Authentication authentication) {
        String rut = authentication.getName();
        return usuarioServices.buscarPorRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /usuarios/me — Actualizar perfil propio
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')")
    @PutMapping("/me")
    public ResponseEntity<Usuarios> actualizarMiPerfil(Authentication authentication, @RequestBody Usuarios usuario) {
        String rut = authentication.getName();
        Usuarios existente = usuarioServices.buscarPorRut(rut)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Usuarios actualizado = usuarioServices.actualizar(existente.getId(), usuario);
        return ResponseEntity.ok(actualizado);
    }
}
