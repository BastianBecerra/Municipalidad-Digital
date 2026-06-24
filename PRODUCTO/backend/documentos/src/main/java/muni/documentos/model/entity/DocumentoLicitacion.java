package muni.documentos.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import muni.documentos.model.enums.TipoDocumentoLicitacion;

import java.time.LocalDate;

@Entity
@Table(name = "documentos_licitacion")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentoLicitacion extends Documento {

    @Column(name = "codigo_licitacion", nullable = false)
    private String codigoLicitacion;

    @Column(name = "proyecto_id")
    private Long proyectoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_licitacion", nullable = false)
    private TipoDocumentoLicitacion tipoLicitacion;

    @Column(name = "fecha_apertura")
    private LocalDate fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;
}
