package muni.usuarios.controller;

import lombok.RequiredArgsConstructor;
import muni.usuarios.entities.Territorio;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.services.TerritorioServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/territorios")
@RequiredArgsConstructor
public class TerritorioController {

    private final TerritorioServices territorioServices;

    // GET /territorios — Listar todos
    @GetMapping
    public ResponseEntity<List<Territorio>> listarTodos() {
        return ResponseEntity.ok(territorioServices.listarTodos());
    }

    // GET /territorios/{id} — Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Territorio> buscarPorId(@PathVariable Long id) {
        return territorioServices.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /territorios — Crear territorio
    @PostMapping
    public ResponseEntity<Territorio> crear(@RequestBody Territorio territorio) {
        Territorio nuevo = territorioServices.guardar(territorio);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT /territorios/{id} — Actualizar territorio
    @PutMapping("/{id}")
    public ResponseEntity<Territorio> actualizar(@PathVariable Long id, @RequestBody Territorio territorio) {
        Territorio actualizado = territorioServices.actualizar(id, territorio);
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /territorios/{id} — Eliminar territorio
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        territorioServices.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Consultas específicas ---

    // GET /territorios/comuna/{comuna}
    @GetMapping("/comuna/{comuna}")
    public ResponseEntity<List<Territorio>> listarPorComuna(@PathVariable String comuna) {
        return ResponseEntity.ok(territorioServices.listarPorComuna(comuna));
    }

    // GET /territorios/region/{region}
    @GetMapping("/region/{region}")
    public ResponseEntity<List<Territorio>> listarPorRegion(@PathVariable String region) {
        return ResponseEntity.ok(territorioServices.listarPorRegion(region));
    }

    // GET /territorios/tipo/{tipo}
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Territorio>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(territorioServices.listarPorTipo(tipo));
    }

    // GET /territorios/activos
    @GetMapping("/activos")
    public ResponseEntity<List<Territorio>> listarActivos() {
        return ResponseEntity.ok(territorioServices.listarActivos());
    }

    // --- Activar / Desactivar ---

    // PATCH /territorios/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Territorio> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(territorioServices.desactivar(id));
    }

    // PATCH /territorios/{id}/activar
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Territorio> activar(@PathVariable Long id) {
        return ResponseEntity.ok(territorioServices.activar(id));
    }

    // --- Gestión de vecinos ---

    // PATCH /territorios/{territorioId}/vecinos/{usuarioId} — Asignar vecino
    @PatchMapping("/{territorioId}/vecinos/{usuarioId}")
    public ResponseEntity<Usuarios> asignarVecino(@PathVariable Long territorioId, @PathVariable Long usuarioId) {
        return ResponseEntity.ok(territorioServices.asignarVecino(territorioId, usuarioId));
    }

    // DELETE /territorios/vecinos/{usuarioId} — Desasignar vecino
    @DeleteMapping("/vecinos/{usuarioId}")
    public ResponseEntity<Usuarios> desasignarVecino(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(territorioServices.desasignarVecino(usuarioId));
    }

    // GET /territorios/{id}/vecinos — Listar vecinos del territorio
    @GetMapping("/{id}/vecinos")
    public ResponseEntity<List<Usuarios>> listarVecinos(@PathVariable Long id) {
        return ResponseEntity.ok(territorioServices.listarVecinos(id));
    }
}
