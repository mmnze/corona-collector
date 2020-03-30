package de.mmenze.corona.web.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.domain.enums.RegionType;
import de.mmenze.corona.repository.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;


/**
 * Imports JSON data for Bundesländer, source is the zeit.de web site
 */
@Slf4j
@Component
public class BundeslandCasesImportScheduledHandler extends BaseCasesImporter {

    @Value("${application.corona.zeit.germany-url:none}")
    private String zeitGermanyJsonUrl;
    @Autowired
    private RegionRepository regionRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Scheduled(cron = "0 15 0 * * *")
    public void importBundeslandData() throws JsonProcessingException {
        log.debug("Starting importing Bundesland cases data");

        ObjectMapper mapper = new ObjectMapper();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        ResponseEntity<String> response = restTemplate.getForEntity(zeitGermanyJsonUrl, String.class);
        JsonNode root = mapper.readTree(response.getBody());

        Map<String, Region> mappedRegions = regionRepository.getAllByRegionTypeMappedByName(RegionType.STATE);
        LocalDate date = LocalDate.now().minusDays(1);

        for (JsonNode bundesland : root.get("states").get("items")) {
            String name =  bundesland.get("name").asText();
            Region region = mappedRegions.get(name);
            if (region == null) {
                region = new Region();
                region.setName(name);
                regionRepository.save(region);
            }


            if (!existsCasesFor(date, region)) {
                Cases cases = new Cases();
                cases.setRegion(region);
                cases.setDate(date);
                cases.setConfirmed(bundesland.get("currentStats").get("count").asInt());
                cases.setDeaths(bundesland.get("currentStats").get("dead").asInt());
                cases.setRecovered(bundesland.get("currentStats").get("recovered").asInt());
                completeCasesAndDeltaCases(cases, date, region);
            }
        }
        log.debug("Done importing Bundesland data");
    }

}
