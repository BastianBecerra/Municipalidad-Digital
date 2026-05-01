package muni.documentos.model.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String rut;
    private String email;
}
