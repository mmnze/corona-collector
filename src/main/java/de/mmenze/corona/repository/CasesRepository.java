package de.mmenze.corona.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.domain.enums.RegionType;

@Repository
public interface CasesRepository extends JpaRepository<Cases, Long> {

    Cases findByDateAndRegion(LocalDate date, Region region);

    List<Cases> findAllByRegion(Region region);

    @Query("SELECT c FROM Cases c WHERE c.date=:date AND c.region.regionType=:regionType")
    List<Cases> findAllByDateAndRegionType(LocalDate date, RegionType regionType);

    default Map<String, Cases> getAllByDateAndRegionTypeMappedByRegion(LocalDate date, RegionType regionType) {
        Map<String, Cases> mappedCases = new HashMap<>();
        findAllByDateAndRegionType(date, regionType).forEach(e -> mappedCases.put(e.getRegion().getName(), e));
        return mappedCases;
    }

}
