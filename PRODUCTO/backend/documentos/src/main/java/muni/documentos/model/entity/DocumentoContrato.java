package muni.documentos.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "documentos_contrato")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentoContrato extends Documento {

    @Column(name = "rut_contratista", nullable = false)
    private String rutContratista;

    @Column(name = "licitacion_asociada_id")
    private Long licitacionAsociadaId;

    @Column(name = "fecha_inicio_contrato")
    private LocalDate fechaInicioContrato;

    @Column(name = "fecha_termino_contrato")
    private LocalDate fechaTerminoContrato;

    @Column(name = "monto_total")
    private Double montoTotal;
}
