package muni.documentos.repository;

import muni.documentos.model.entity.DocumentoSalvoconducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoSalvoconductoRepository extends JpaRepository<DocumentoSalvoconducto, Long> {
}
