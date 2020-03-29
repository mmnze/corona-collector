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
 * Imports JSON data for Landkreise (districts), source is the zeit.de web site
 */
@Slf4j
@Component
public class DistrictCasesImportScheduledHandler extends BaseCasesImporter {

    @Value("${application.corona.zeit.germany-url:none}")
    private String zeitGermanyJsonUrl;
    @Autowired
    private RegionRepository regionRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Scheduled(cron = "0 55 23 * * *")
    public void importDistrictData() throws JsonProcessingException {
        importDistrictData(false);
    }

    public void importDistrictData(boolean historicalData) throws JsonProcessingException {
        log.debug("Starting importing district cases data, includes historical data: {}", historicalData);
        ObjectMapper mapper = new ObjectMapper();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        ResponseEntity<String> response = restTemplate.getForEntity(zeitGermanyJsonUrl, String.class);
        JsonNode root = mapper.readTree(response.getBody());

        LocalDate startDate = null;
        if (historicalData) {
            startDate = LocalDate.parse(root.get("kreise").get("meta").get("historicalStats").get("start").asText());
        } else {
            String changeTimestamp = root.get("lastUpdate").asText();
            changeTimestamp = changeTimestamp.substring(0, 10);
            startDate = LocalDate.now();
        }
        Map<String, Region> mappedRegions = regionRepository.getAllByRegionTypeMappedByCode(RegionType.DISTRICT);

        for (JsonNode district : root.get("kreise").get("items")) {
            String code =  district.get("ags").asText();
            Region region = mappedRegions.get(code);
            if (region == null) {
                // all districts are pre-loaded, hence issue a warning if not found
                log.warn("District not found! {}", code);
                continue;
            }

            LocalDate date = startDate;
            if (historicalData) {
                for (JsonNode count : district.get("historicalCounts").get("count")) {
                    // data is incomplete for today, ignore it
                    if (LocalDate.now().equals(date)) {
                        continue;
                    }

                    if (!existsCasesFor(date, region)) {
                        Cases cases = new Cases();
                        cases.setRegion(region);
                        cases.setDate(date);
                        cases.setConfirmed(count.asInt());
                        completeCasesAndDeltaCases(cases, date, region);
                    }
                    date = date.plusDays(1);
                }
            } else {
                if (!existsCasesFor(date, region)) {
                    Cases cases = new Cases();
                    cases.setRegion(region);
                    cases.setDate(date);
                    cases.setConfirmed(district.get("currentStats").get("count").asInt());
                    cases.setConfirmed(district.get("currentStats").get("recovered").asInt());
                    cases.setConfirmed(district.get("currentStats").get("dead").asInt());
                    completeCasesAndDeltaCases(cases, date, region);
                }
            }
        }
        log.debug("Done importing district data");
    }

}
