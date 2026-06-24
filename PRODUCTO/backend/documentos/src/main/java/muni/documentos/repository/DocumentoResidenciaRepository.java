package muni.documentos.repository;

import muni.documentos.model.entity.DocumentoResidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoResidenciaRepository extends JpaRepository<DocumentoResidencia, Long> {
}
