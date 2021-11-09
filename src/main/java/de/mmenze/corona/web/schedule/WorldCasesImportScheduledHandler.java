package de.mmenze.corona.web.schedule;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.domain.enums.RegionType;
import de.mmenze.corona.util.CsvUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads CSV-based data on corona cases from the John Hopkins University GitHub account
 */
@Slf4j
@Component
public class WorldCasesImportScheduledHandler extends BaseCasesImporter {

    @Value("${application.corona.johns-hopkins.csv-base-url:none}")
    private String baseDailyCsvUrl;

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");


    public void importAllWorldDataCsv() throws Exception {
        LocalDate date = LocalDate.of(2020, 4, 22);
        while (date.isBefore(LocalDate.now())) {
            importData(date, false);
            date = date.plusDays(1);
        }
    }

    @Scheduled(cron = "0 15 6 * * *")
    public void importLastWorldDataCsv()throws Exception  {
        importData(LocalDate.now().minusDays(1), false);
    }

    public void importLastWorldDataCsv(boolean force)throws Exception  {
        importData(LocalDate.now().minusDays(1), force);
    }

    private void importData(LocalDate date, boolean update) throws Exception {
        log.debug("Starting to import world cases for date {}", date);
        Map<String, Region> mappedRegions = regionRepository.getAllByRegionTypeMappedByName(RegionType.COUNTRY);
        Map<String, Cases> mappedCases = new HashMap<>();
        Map<String, Cases> yesterdaysCases = casesRepository.getAllByDateAndRegionTypeMappedByRegion(date.minusDays(1), RegionType.COUNTRY);

        // load data
        URL url = new URL(baseDailyCsvUrl + "/" + date.format(DATE_FORMAT) + ".csv");
        Reader reader = new InputStreamReader(url.openStream());
        
        try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {	
	        // change in format
	        String countryRegion = "Country/Region";
	        if (date.isAfter(LocalDate.of(2020, 03, 21))) {
	            countryRegion = "Country_Region";
	        }
	
	        // combine regions to countries
	        List<CSVRecord> records = parser.getRecords();
	        for (CSVRecord record : records) {
	            String region = record.get(countryRegion);
	            int confirmed = CsvUtils.getIntegerFrom(record.get("Confirmed"));
	            int deaths = CsvUtils.getIntegerFrom(record.get("Deaths"));
	            int recovered = CsvUtils.getIntegerFrom(record.get("Recovered"));
	            region = getCleanedCountryName(region);
	
	            // there are few errors in the data, hence we have to ignore some countries
	            if (ignoreCountry(region)) {
	                continue;
	            }
	
	            Cases c = mappedCases.get(region);
	            if (c == null) {
	                c = new Cases();
	                mappedCases.put(region, c);
	            }
	            c.setConfirmed(confirmed + c.getConfirmed());
	            c.setDeaths(deaths + c.getDeaths());
	            c.setRecovered(recovered + c.getRecovered());
	        }
	
	        // write new data to DB
	        for (Map.Entry<String, Cases> e : mappedCases.entrySet()) {
	            Region region = mappedRegions.get(e.getKey());
	            if (region == null) {
	                region = new Region();
	                region.setName(e.getKey());
	                region.setRegionType(RegionType.COUNTRY);
	                regionRepository.save(region);
	                log.debug("New region created: {}", region.getName());
	            }
	            yesterdaysCases.remove(e.getKey());
	
	            // TODO the force flag (and the code "below" is not fully implemented)
	            if (!existsCasesFor(date, region) || update) {
	                Cases cases = e.getValue();
	                cases.setRegion(region);
	                cases.setDate(date);
	                completeCasesAndDeltaCases(cases, date, region);
	            }
	        }
	
	        if (yesterdaysCases.size() > 0) {
	            log.warn("Number of regions no longer contained in the data: {}", yesterdaysCases.size());
	            // there was data in the past, but there is none today. what's the best way to deal with this?
	            // don't know, I opt for copying yesterdays data
	            for (Cases yesterday : yesterdaysCases.values()) {
	                if (!existsCasesFor(date, yesterday.getRegion())) {
	                    Cases cases = new Cases();
	                    cases.setRegion(yesterday.getRegion());
	                    cases.setDate(date);
	                    cases.setConfirmed(yesterday.getConfirmed());
	                    cases.setRecovered(yesterday.getRecovered());
	                    cases.setDeaths(yesterday.getDeaths());
	                    completeCasesAndDeltaCases(cases, cases.getDate(), cases.getRegion());
	                }
	            }
	        }
	
	        log.debug("Done importing world cases for date {}", date);
        }
    }

    private String getCleanedCountryName(String countryName) {
        countryName = countryName.replace("Mainland", "");
        countryName = countryName.replace("SAR", "");
        countryName = countryName.replace("*", "");
        countryName = countryName.replace("Channel Islands", "United Kingdom");
        countryName = countryName.replace("Guernsey", "United Kingdom");
        countryName = countryName.replace("Jersey", "United Kingdom");
        countryName = countryName.replace("Cruise Ship", "Others");
        countryName = countryName.replace("Czechia", "Czech Republic");
        countryName = countryName.replace("Iran (Islamic Republic of)", "Iran");
        countryName = countryName.replace("Republic of Korea", "South Korean");
        countryName = countryName.replace("Korea, South", "South Korean");
        countryName = countryName.replace("South Korea", "South Korean");
        countryName = countryName.replace("South Koreann", "South Korean");
        countryName = countryName.replace("occupied Palestinian territory", "Israel");
        countryName = countryName.replace("Palestine", "Israel");
        countryName = countryName.replace("Republic of Moldova", "Moldova");
        countryName = countryName.replace("Congo (Kinshasa)", "DR Congo");
        countryName = countryName.replace("Republic of the Congo", "Congo-Brazzaville");
        countryName = countryName.replace("Congo (Brazzaville)", "Congo-Brazzaville");
        countryName = countryName.replace("Palestine", "Israel");
        countryName = countryName.replace("The ", "");
        countryName = countryName.replace(", The", "");
        countryName = countryName.replace("Guam", "US");
        countryName = countryName.replace("Macau", "Macao");
        countryName = countryName.replace("Saint Barthelemy", "France");
        countryName = countryName.replace("St. Martin", "Saint Martin");
        countryName = countryName.replace("Taipei and environs", "Taiwan");
        countryName = countryName.replace("UK", "United Kingdom");
        countryName = countryName.replace("Vatican City", "Holy See");
        countryName = countryName.replace("Vietnam", "Viet Nam");
        countryName = countryName.replace("Cote d'Ivoire", "Ivory Coast");
        countryName = countryName.replace("Eswatini", "Swaziland");
        countryName = countryName.replace("North Ireland", "United Kingdom");
        countryName = countryName.replace("North Macedonia", "Macedonia");
        countryName = countryName.replace("Cape Verde", "Cabo Verde");
        countryName = countryName.replace("Syria", "Syrian");
        countryName = countryName.replace("Diamond Princess", "Others");
        countryName = countryName.replace("MS Zaandam", "Others");
        countryName = countryName.replace("West Bank and Gaza", "Israel");
        countryName = countryName.replace("Hong Kong", "China");
        countryName = countryName.replace("Macao", "China");
        countryName = countryName.replace("Gibraltar", "United Kingdom");
        countryName = countryName.replace("Cayman Islands", "United Kingdom");
        countryName = countryName.replace("Martinique", "France");
        countryName = countryName.replace("French Guiana", "France");
        countryName = countryName.replace("Saint Martin", "France");
        countryName = countryName.replace("Reunion", "France");
        countryName = countryName.replace("Guadeloupe", "France");
        countryName = countryName.replace("Mayotte", "France");
        countryName = countryName.replace("Puerto Rico", "US");
        countryName = countryName.replace("Faroe Islands", "Netherlands");
        countryName = countryName.replace("Greenland", "Netherlands");
        countryName = countryName.replace("Aruba", "Denmark");
        countryName = countryName.replace("Curacao", "Denmark");
        countryName = countryName.replace("East Timor", "Timor-Leste");
        countryName = countryName.replace("Republic of Ireland", "Ireland");

        countryName = countryName.trim();
        return countryName;
    }

    private boolean ignoreCountry(String country) {
        Set<String> ignoredCountries = new HashSet<>();
        ignoredCountries.add("Republic of Ireland");
        ignoredCountries.add("Russian Federation");
        if (ignoredCountries.contains(country)) {
            return true;
        }
        return false;
    }

}
