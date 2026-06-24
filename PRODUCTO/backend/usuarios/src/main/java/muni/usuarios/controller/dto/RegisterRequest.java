package muni.usuarios.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String rut;
    private LocalDate fechaNacimiento;
    private String genero;
    private String email;
    private String telefono;
    private String direccion;
    private String comuna;
    private String region;
    private String password;
    private String passwordClaveUnica;
    private String rol;
    private Long territorioId;
}
