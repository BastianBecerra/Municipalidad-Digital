package muni.documentos.repository;

import muni.documentos.model.entity.DocumentoJuntaVecinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoJuntaVecinalRepository extends JpaRepository<DocumentoJuntaVecinal, Long> {
}
