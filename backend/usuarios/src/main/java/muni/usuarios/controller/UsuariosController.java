package muni.usuarios.controller;

import lombok.RequiredArgsConstructor;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.services.UsuarioServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuariosController {

    private final UsuarioServices usuarioServices;

    // GET /usuarios — Listar todos
    @GetMapping
    public ResponseEntity<List<Usuarios>> listarTodos() {
        return ResponseEntity.ok(usuarioServices.listarTodos());
    }

    // GET /usuarios/{id} — Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuarios> buscarPorId(@PathVariable Long id) {
        return usuarioServices.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /usuarios — Crear usuario
    @PostMapping
    public ResponseEntity<Usuarios> crear(@RequestBody Usuarios usuario) {
        Usuarios nuevo = usuarioServices.guardar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT /usuarios/{id} — Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<Usuarios> actualizar(@PathVariable Long id, @RequestBody Usuarios usuario) {
        Usuarios actualizado = usuarioServices.actualizar(id, usuario);
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /usuarios/{id} — Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioServices.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints específicos ---

    // GET /api/usuarios/rut/{rut} — Buscar por RUT
    @GetMapping("/rut/{rut}")
    public ResponseEntity<Usuarios> buscarPorRut(@PathVariable String rut) {
        return usuarioServices.buscarPorRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/usuarios/email/{email} — Buscar por email
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuarios> buscarPorEmail(@PathVariable String email) {
        return usuarioServices.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/usuarios/rol/{rol} — Listar por rol
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Usuarios>> listarPorRol(@PathVariable String rol) {
        return ResponseEntity.ok(usuarioServices.listarPorRol(rol));
    }

    // GET /api/usuarios/comuna/{comuna} — Listar por comuna
    @GetMapping("/comuna/{comuna}")
    public ResponseEntity<List<Usuarios>> listarPorComuna(@PathVariable String comuna) {
        return ResponseEntity.ok(usuarioServices.listarPorComuna(comuna));
    }

    // GET /api/usuarios/activos — Listar solo activos
    @GetMapping("/activos")
    public ResponseEntity<List<Usuarios>> listarActivos() {
        return ResponseEntity.ok(usuarioServices.listarActivos());
    }

    // PATCH /api/usuarios/{id}/desactivar — Desactivar usuario
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Usuarios> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioServices.desactivar(id));
    }

    // PATCH /api/usuarios/{id}/activar — Activar usuario
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Usuarios> activar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioServices.activar(id));
    }
}
