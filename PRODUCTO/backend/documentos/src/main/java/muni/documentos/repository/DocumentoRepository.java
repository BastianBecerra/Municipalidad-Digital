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

    @Query("SELECT d FROM Documento d WHERE " +
           "TREAT(d AS DocumentoResidencia).usuarioRut = :rut OR " +
           "TREAT(d AS DocumentoSalvoconducto).usuarioRut = :rut")
    List<Documento> findByUsuarioRut(@Param("rut") String rut);

    Optional<Documento> findByHashSha256(String hashSha256);
}
