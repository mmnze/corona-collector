package de.mmenze.corona.repository;

import de.mmenze.corona.domain.DeltaCases;
import de.mmenze.corona.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DeltaCasesRepository extends JpaRepository<DeltaCases, Long> {

    DeltaCases findByBaseDateAndRegion(LocalDate date, Region region);

}
