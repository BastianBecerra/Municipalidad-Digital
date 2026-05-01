package muni.documentos.repository;

import muni.documentos.model.entity.DocumentoLicitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoLicitacionRepository extends JpaRepository<DocumentoLicitacion, Long> {
}
