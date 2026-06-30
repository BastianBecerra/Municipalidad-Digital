package muni.usuarios.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuarios implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Datos personales ---
    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 80)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 80)
    private String apellidoMaterno;

    @Column(nullable = false, unique = true, length = 20)
    private String rut; // RUT chileno (ej: 12.345.678-9)

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String genero; // MASCULINO, FEMENINO, OTRO, PREFIERO_NO_DECIR

    // --- Datos de contacto ---
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    // --- Dirección ---
    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String comuna;

    @Column(length = 100)
    private String region;

    // --- Datos de acceso ---
    @Column(nullable = false)
    private String password;

    @Column(name = "password_clave_unica")
    private String passwordClaveUnica;

    @Column(nullable = false, length = 30)
    private String rol; // VECINO, FUNCIONARIO, ADMIN

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // --- Relación con Territorio (Junta de Vecinos) ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "territorio_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"vecinos"})
    private Territorio territorio;

    // --- Auditoría ---
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @PrePersist
    protected void onCreate() {
        if (this.activo == null) {
            this.activo = true;
        }
        this.fechaRegistro = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // --- Métodos de UserDetails ---

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // En un caso real, el rol podría ser "ROLE_VECINO", "ROLE_ADMIN".
        // Si en la base de datos lo guardas como "ADMIN", aquí le agregamos el "ROLE_" opcionalmente.
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.toUpperCase()));
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        // Usaremos el RUT como el username principal para Spring Security.
        return this.rut;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return this.activo;
    }
}
