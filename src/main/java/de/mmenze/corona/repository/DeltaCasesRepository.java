package de.mmenze.corona.repository;

import de.mmenze.corona.domain.DeltaCases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeltaCasesRepository extends JpaRepository<DeltaCases, Long> {

}
