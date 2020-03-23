package de.mmenze.corona.repository;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CasesRepository extends JpaRepository<Cases, Long> {

    Cases getByDateAndRegion(LocalDate date, Region region);

}
