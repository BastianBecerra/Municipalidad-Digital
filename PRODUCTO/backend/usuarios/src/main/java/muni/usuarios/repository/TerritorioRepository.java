package muni.usuarios.repository;

import muni.usuarios.entities.Territorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerritorioRepository extends JpaRepository<Territorio, Long> {

    List<Territorio> findByComuna(String comuna);

    List<Territorio> findByRegion(String region);

    List<Territorio> findByTipo(String tipo);

    List<Territorio> findByComunaAndTipo(String comuna, String tipo);

    List<Territorio> findByActivoTrue();

    List<Territorio> findByPresidenteContainingIgnoreCase(String presidente);
}
