package muni.documentos.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documentos_residencia")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentoResidencia extends Documento {

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "usuario_nombre_completo")
    private String usuarioNombreCompleto;

    @Column(name = "usuario_rut")
    private String usuarioRut;

    @Column(name = "usuario_direccion")
    private String usuarioDireccion;

    @Column(name = "usuario_comuna")
    private String usuarioComuna;
}
