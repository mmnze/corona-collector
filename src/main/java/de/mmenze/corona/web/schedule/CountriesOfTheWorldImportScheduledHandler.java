package de.mmenze.corona.web.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.repository.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class CountriesOfTheWorldImportScheduledHandler {

    @Autowired
    private RegionRepository regionRepository;


    @Scheduled(cron = "0 0 2 * * *")
    public void importCountryData() throws JsonProcessingException {
        log.debug("Starting importing country data");
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity<String> response = restTemplate.getForEntity("http://api.countrylayer.com/v2/all?access_key=0a241fd62a40c23359ac762c43972b9e", String.class);
        JsonNode root = mapper.readTree(response.getBody());

        List<Region> regions = regionRepository.findAll();
        for (Region region : regions) {
            // ignore the countries we've already worked on
            if (region.getPopulation() != 0) {
                continue;
            }

            String cn = region.getName();
            boolean foundRegion = false;
            for (JsonNode node : root) {
                String name = node.get("name").asText();
                String alpha2 = node.get("alpha2Code").asText();
                if (cn.equals(name) || cn.equals(alpha2) ||
                        isNameInAlternativeNames(cn, node) || (name.contains(cn) && cn.length() >= 6) ||
                        (name.startsWith(cn) && name.charAt(cn.length()) == ' ')) {
                    foundRegion = true;
                    region.setPopulation(node.get("population").asInt());
                    region.setLat(node.get("latlng").get(0).asDouble());
                    region.setLng(node.get("latlng").get(1).asDouble());
                    region.setCode(node.get("alpha2Code").asText());
                    region.setContinent(node.get("region").asText());
                    regionRepository.save(region);
                    log.debug("Updated region {}", region.getName());
                    break;
                }
            }

            if (!foundRegion) {
                log.warn("region '{}' not found in REST reply", region.getName());
            }
        }
        log.debug("Current number of regions in DB: {}", regions.size());
        log.debug("Done importing country data");
    }

    private boolean isNameInAlternativeNames(String name, JsonNode node) {
        for (JsonNode alternativeName : node.get("altSpellings")) {
            if (name.equals(alternativeName.asText())) {
                return true;
            }
        }
        return false;
    }

}
