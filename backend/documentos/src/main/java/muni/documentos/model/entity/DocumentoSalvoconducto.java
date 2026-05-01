package muni.documentos.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_salvoconducto")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentoSalvoconducto extends Documento {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_rut")
    private String usuarioRut;

    @Column(name = "usuario_nombre_completo")
    private String usuarioNombreCompleto;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "direccion_destino")
    private String direccionDestino;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;
}
