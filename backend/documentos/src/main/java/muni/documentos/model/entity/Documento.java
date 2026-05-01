package muni.documentos.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import muni.documentos.model.enums.EstadoBlockchain;
import muni.documentos.model.enums.EstadoDocumento;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public abstract class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDocumento estado;

    @Column(name = "ruta_archivo_pdf")
    private String rutaArchivoPdf;

    // --- Validación y Seguridad ---
    @Column(name = "firma_digital", columnDefinition = "TEXT")
    private String firmaDigital;
    
    @Column(name = "firmado_por")
    private String firmadoPor;
    
    @Column(name = "codigo_qr_url", length = 500)
    private String codigoQrUrl;

    // --- Blockchain ---
    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;

    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_blockchain")
    private EstadoBlockchain estadoBlockchain;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoDocumento.BORRADOR;
        if (this.estadoBlockchain == null) this.estadoBlockchain = EstadoBlockchain.PENDIENTE;
    }
}
