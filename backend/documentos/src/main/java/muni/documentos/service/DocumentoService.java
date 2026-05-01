package muni.documentos.service;

import muni.documentos.model.entity.Documento;
import muni.documentos.model.entity.DocumentoJuntaVecinal;
import muni.documentos.model.entity.DocumentoLicitacion;
import muni.documentos.model.entity.DocumentoContrato;
import muni.documentos.model.entity.DocumentoSalvoconducto;
import java.util.List;

public interface DocumentoService {
    // Generics for general operations
    List<Documento> findAll();
    Documento findById(Long id);

    byte[] generatePdf(Long id);

    // Creation operations
    DocumentoJuntaVecinal createJuntaVecinalDoc(DocumentoJuntaVecinal doc, boolean isSimple);
    DocumentoLicitacion createLicitacionDoc(DocumentoLicitacion doc, boolean isSimple);
    DocumentoContrato createContratoDoc(DocumentoContrato doc, boolean isSimple);
    DocumentoSalvoconducto createSalvoconductoDoc(DocumentoSalvoconducto doc, boolean isSimple);

    // Approval logic
    Documento approveDocument(Long id);

    // Blockchain sync placeholder
    void syncWithBlockchain(Long id);
}
