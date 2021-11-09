package de.mmenze.corona.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.mmenze.corona.domain.DeltaCases;
import de.mmenze.corona.domain.Region;

@Repository
public interface DeltaCasesRepository extends JpaRepository<DeltaCases, Long> {

    DeltaCases findByBaseDateAndRegion(LocalDate date, Region region);

}
