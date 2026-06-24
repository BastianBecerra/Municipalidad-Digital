package muni.documentos.repository;

import muni.documentos.model.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    @Query(value = 
        "SELECT d.* FROM documentos d " +
        "LEFT JOIN documentos_residencia r ON d.id = r.id " +
        "LEFT JOIN documentos_salvoconducto s ON d.id = s.id " +
        "WHERE r.usuario_rut = :rut OR s.usuario_rut = :rut", 
        nativeQuery = true)
    List<Documento> findByUsuarioRut(@Param("rut") String rut);

    Optional<Documento> findByHashSha256(String hashSha256);
}
