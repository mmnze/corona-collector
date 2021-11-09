package de.mmenze.corona.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.mmenze.corona.domain.IntensiveCareData;
import de.mmenze.corona.domain.Region;

@Repository
public interface IntensiveCareDataRepository extends JpaRepository<IntensiveCareData, Long> {

    IntensiveCareData findByDateAndDistrict(LocalDate date, Region district);

}
