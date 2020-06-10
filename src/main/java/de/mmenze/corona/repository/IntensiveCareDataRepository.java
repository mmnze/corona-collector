package de.mmenze.corona.repository;

import de.mmenze.corona.domain.IntensiveCareData;
import de.mmenze.corona.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface IntensiveCareDataRepository extends JpaRepository<IntensiveCareData, Long> {

    IntensiveCareData findByDateAndDistrict(LocalDate date, Region district);

}
