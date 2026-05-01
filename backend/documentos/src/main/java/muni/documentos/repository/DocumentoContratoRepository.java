package muni.documentos.repository;

import muni.documentos.model.entity.DocumentoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoContratoRepository extends JpaRepository<DocumentoContrato, Long> {
}
