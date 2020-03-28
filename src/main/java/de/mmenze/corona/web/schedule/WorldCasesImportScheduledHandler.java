package de.mmenze.corona.web.schedule;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.domain.enums.RegionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads CSV-based data on corona cases from the John Hopkins University GitHub account
 */
@Slf4j
@Component
public class WorldCasesImportScheduledHandler extends BaseCasesImporter {

    @Value("${application.corona.johns-hopkins.csv-base-url:none}")
    private String baseDailyCsvUrl;

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");


    public void importAllWorldDataCsv() {
        LocalDate date = LocalDate.of(2020, 1, 22);
        while (date.isBefore(LocalDate.now())) {
            importData(date);
            date = date.plusDays(1);
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void importLastWorldDataCsv() {
        importData(LocalDate.now().minusDays(1));
    }


    private void importData(LocalDate date) {
        log.debug("Starting to import world cases for date {}", date);
        Map<String, Region> mappedRegions = regionRepository.getAllRegionsByRegionTypeMappedByName(RegionType.COUNTRY);
        Map<String, Cases> mappedCases = new HashMap<>();

        try {
            // load data
            URL url = new URL(baseDailyCsvUrl + "/" + date.format(DATE_FORMAT) + ".csv");
            Reader reader = new InputStreamReader(url.openStream());
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            // change in format
            String countryRegion = "Country/Region";
            if (date.isAfter(LocalDate.of(2020, 03, 21))) {
                countryRegion = "Country_Region";
            }

            // combine regions to countries
            List<CSVRecord> records = parser.getRecords();
            for (CSVRecord record : records) {
                String region = record.get(countryRegion);
                int confirmed = getIntegerFrom(record.get("Confirmed"));
                int deaths = getIntegerFrom(record.get("Deaths"));
                int recovered = getIntegerFrom(record.get("Recovered"));
                region = getCleanedCountryName(region);

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
                }

                if (!existsCasesFor(date, region)) {
                    Cases cases = e.getValue();
                    cases.setRegion(region);
                    cases.setDate(date);
                    completeCasesAndDeltaCases(cases, date, region);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("Done importing world cases for date {}", date);
    }

    private int getIntegerFrom(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
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
        countryName = countryName.replace("Russian Federation", "Russian");
        countryName = countryName.replace("Palestine", "Israel");
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
        countryName = countryName.replace("West Bank and Gaza", "Israel");

        countryName = countryName.trim();
        return countryName;
    }

}
