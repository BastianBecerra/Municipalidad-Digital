package muni.documentos.model.dto;

import lombok.Data;

@Data
public class TerritorioDTO {
    private Long id;
    private String nombre;
    private String tipo;
    private String comuna;
}
