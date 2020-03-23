package de.mmenze.corona.repository;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    Region findByName(String name);


    default Map<String, Region> getAllMappedRegions() {
        Map<String, Region> mappedRegions = new HashMap<>();
        findAll().forEach(e -> mappedRegions.put(e.getName(), e));
        return mappedRegions;
    }

}
