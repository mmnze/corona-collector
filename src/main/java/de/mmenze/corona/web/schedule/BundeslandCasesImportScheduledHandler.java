package de.mmenze.corona.web.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
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
import java.util.Locale;
import java.util.Map;


/**
 * Imports JSON data for Bundesländer, source is the zeit.de web site
 */
@Slf4j
@Component
public class BundeslandCasesImportScheduledHandler extends BaseCasesImporter {

    @Value("${application.corona.zeit.json-url:none}")
    private String zeitBundeslandJsonUrl;
    @Autowired
    private RegionRepository regionRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.GERMANY);


    @Scheduled(cron = "0 45 23 * * *")
    public void importBundeslandData() throws JsonProcessingException {
        log.debug("Starting importing Bundesland cases data");

        ObjectMapper mapper = new ObjectMapper();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        ResponseEntity<String> response = restTemplate.getForEntity(zeitBundeslandJsonUrl, String.class);
        JsonNode root = mapper.readTree(response.getBody());

        Map<String, Region> mappedRegions = regionRepository.getAllMappedRegions();
        String changeTimestamp = root.get("changeTimestamp").asText();
        changeTimestamp = changeTimestamp.substring(0, changeTimestamp.indexOf(","));
        LocalDate date = LocalDate.parse(changeTimestamp, DATE_FORMAT);

        for (JsonNode bundesland : root.get("states")) {
            String name =  bundesland.get("state").asText();
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
                cases.setConfirmed(bundesland.get("count").asInt());
                cases.setDeaths(bundesland.get("dead").asInt());
                cases.setRecovered(bundesland.get("recovered").asInt());
                completeCasesAndDeltaCases(cases, date, region);
            }
        }
        log.debug("Done importing Bundesland data");
    }

}
