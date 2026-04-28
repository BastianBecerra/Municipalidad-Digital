package muni.usuarios.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "territorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Territorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Identificación del territorio ---
    @Column(nullable = false, length = 150)
    private String nombre; // Ej: "Junta de Vecinos Villa Los Aromos"

    @Column(length = 50)
    private String tipo; // JUNTA_VECINOS, UNIDAD_VECINAL, SECTOR

    @Column(name = "numero_unidad_vecinal", length = 20)
    private String numeroUnidadVecinal; // Número oficial de la unidad vecinal

    // --- Ubicación administrativa ---
    @Column(nullable = false, length = 100)
    private String comuna;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(length = 200)
    private String direccionSede; // Dirección de la sede de la junta

    // --- Ubicación geográfica ---
    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "limite_norte", length = 200)
    private String limiteNorte;

    @Column(name = "limite_sur", length = 200)
    private String limiteSur;

    @Column(name = "limite_este", length = 200)
    private String limiteEste;

    @Column(name = "limite_oeste", length = 200)
    private String limiteOeste;

    // --- Contacto ---
    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    // --- Directiva ---
    @Column(name = "presidente", length = 150)
    private String presidente; // Nombre del presidente de la junta

    @Column(length = 1000)
    private String descripcion;

    // --- Estado ---
    @Column(nullable = false)
    private Boolean activo;

    // --- Relación con Usuarios ---
    @OneToMany(mappedBy = "territorio", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Usuarios> vecinos;

    // --- Auditoría ---
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @PrePersist
    protected void onCreate() {
        if (this.activo == null) {
            this.activo = true;
        }
        this.fechaCreacion = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}
