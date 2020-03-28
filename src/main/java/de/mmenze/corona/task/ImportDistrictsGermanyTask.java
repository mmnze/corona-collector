package de.mmenze.corona.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.domain.enums.RegionType;
import de.mmenze.corona.repository.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Imports all German districts from the GEO-JSON file used in here.
 * This must be done once before district loading data from the web.
 */
@Slf4j
@Controller
public class ImportDistrictsGermanyTask {

    @Autowired
    private RegionRepository regionRepository;


    @GetMapping("/task/import/districs/germany")
    public void importDistricts() throws Exception{
        log.debug("Now importing districts");
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("resources/geojson/districts_germany.geojson");
        JsonNode root = mapper.readTree(file);
        Map<String, Region> mappedRegions = new HashMap<>();

        for (JsonNode district : root.get("features")) {
            String name =  district.get("properties").get("gen").asText();
            int ags = district.get("properties").get("ags").asInt();
            String destatis = district.get("properties").get("destatis").asText();
            int population = mapper.readTree(destatis.getBytes()).get("population").asInt();

            Region fromMap = mappedRegions.get(name);
            if (fromMap != null && fromMap.getPopulation() != population) {
                String type=  district.get("properties").get("bez").asText();
                if ("Landkreis".equals(type)) {
                    name = "Landkreis " + name;
                } else {
                    fromMap.setName("Landkreis " + name);
                }
            } else if (fromMap != null && fromMap.getPopulation() == population) {
                // not sure why, but some contained twice, let's ignore the second
                continue;
            }

            Region r = new Region();
            r.setRegionType(RegionType.DISTRICT);
            r.setName(name);
            r.setCode("" + ags);
            r.setPopulation(population);
            mappedRegions.put(name, r);
        }
        for (Region r : mappedRegions.values()) {
            regionRepository.save(r);
            log.debug("Done import district {}", r.getName());
        }
        log.debug("Done importing districts");
    }

}
