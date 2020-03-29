package de.mmenze.corona.repository;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.domain.enums.RegionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    Region findByName(String name);

    List<Region> findAllByRegionType(RegionType regionType);

    default Map<String, Region> getAllByRegionTypeMappedByName(RegionType regionType) {
        Map<String, Region> mappedRegions = new HashMap<>();
        findAllByRegionType(regionType).forEach(e -> mappedRegions.put(e.getName(), e));
        return mappedRegions;
    }

    default Map<String, Region> getAllByRegionTypeMappedByCode(RegionType regionType) {
        Map<String, Region> mappedRegions = new HashMap<>();
        findAllByRegionType(regionType).forEach(e -> mappedRegions.put(e.getCode(), e));
        return mappedRegions;
    }

}
