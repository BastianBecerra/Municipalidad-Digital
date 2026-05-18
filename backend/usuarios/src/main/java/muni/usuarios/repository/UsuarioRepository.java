package muni.usuarios.repository;

import muni.usuarios.entities.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {

    Optional<Usuarios> findByRut(String rut);

    Optional<Usuarios> findByEmail(String email);

    List<Usuarios> findByRol(String rol);

    List<Usuarios> findByComuna(String comuna);

    List<Usuarios> findByActivoTrue();

    boolean existsByRut(String rut);

    boolean existsByEmail(String email);
}
