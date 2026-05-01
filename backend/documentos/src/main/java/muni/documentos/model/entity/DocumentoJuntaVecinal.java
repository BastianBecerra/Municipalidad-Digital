package muni.documentos.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import muni.documentos.model.enums.TipoDocumentoJJVV;

@Entity
@Table(name = "documentos_jjvv")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentoJuntaVecinal extends Documento {

    @Column(name = "junta_vecinos_id")
    private Long juntaVecinosId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acta", nullable = false)
    private TipoDocumentoJJVV tipoActa;

    @Column(name = "rut_ministro_fe")
    private String rutMinistroDeFe;
}
