package de.mmenze.corona.web.task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import de.mmenze.corona.domain.IntensiveCareData;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.domain.enums.RegionType;
import de.mmenze.corona.repository.IntensiveCareDataRepository;
import de.mmenze.corona.repository.RegionRepository;
import de.mmenze.corona.util.CsvUtils;
import de.mmenze.corona.util.UrlUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ImportIntensiveCareUnitDataTask {

    // they don't make life easy for us... file names follow different patterns. we try each of these...
    private String[] baseDailyCsvUrlPatterns = new String[] {
            "https://www.divi.de/divi-intensivregister-tagesreport-archiv/%s-divi-intensivregister-tagesreport-%s/file",
            "https://www.divi.de/divi-intensivregister-tagesreport-archiv/%s-divi-intensivregister-tagesreport-%s-1/file",
            "https://www.divi.de/divi-intensivregister-tagesreport-archiv/%s-divi-intensivregister-tagesreport-%s-1/file",
            "https://www.divi.de/divi-intensivregister-tagesreport-archiv/%s-divi-intensivregister-%s-09-15/file",
            "https://www.divi.de/divi-intensivregister-tagesreport-archiv/%s-divi-intensivregister-%s-csv/file"
    };
    private static final DateTimeFormatter[] dfs = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };
    // files do not only have the corresponding date in the name but also a running counter ID
    // which is not running with a perfect pattern... hence we have to guess a bit....
    private static int startFileId = 1578;


    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private IntensiveCareDataRepository intensiveCareDataRepository;


    @GetMapping("/task/import/intensiveCare")
    public void importData() throws Exception {
        int fileCounter = startFileId;

        log.debug("Starting to import intensive care data");
        Map<String, Region> districts = regionRepository.getAllByRegionTypeMappedByCode(RegionType.DISTRICT);
        Map<String, Region> states = regionRepository.getAllByRegionTypeMappedByCode(RegionType.STATE);

        // load data
        LocalDate date = LocalDate.of(2020, 5, 5);
        while (date.isBefore(LocalDate.now())) {
            log.debug("now reading data for date: {}", date);

            ReaderResult readerResult = getReader(fileCounter, date);
            fileCounter = readerResult.lastId;
            
            try (CSVParser parser = new CSVParser(readerResult.reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withAllowMissingColumnNames())) {
	            // combine regions to countries
	            List<CSVRecord> records = parser.getRecords();
	            for (CSVRecord record : records) {
	                IntensiveCareData icd = new IntensiveCareData();
	                icd.setDistrict(districts.get(CsvUtils.getStringWithDefaultIfMissingFrom(record, "gemeindeschluessel")));
	                if (icd.getDistrict() == null) {
	                    icd.setDistrict(districts.get(CsvUtils.getStringWithDefaultIfMissingFrom(record, "kreis")));
	                }
	
	                // state is missing for some days, try to get the data from previous days
	                icd.setState(states.get(CsvUtils.getStringWithDefaultIfMissingFrom(record, "bundesland")));
	                if (icd.getState() == null) {
	                    IntensiveCareData yesterdaysData = intensiveCareDataRepository.findByDateAndDistrict(date.minusDays(1), icd.getDistrict());
	                    icd.setState(yesterdaysData.getState());
	                }
	
	                icd.setCases((int)CsvUtils.getDoubleWithDefaultIfMissingFrom(record, "faelle_covid_aktuell"));
	                icd.setCasesVentilated((int)CsvUtils.getDoubleWithDefaultIfMissingFrom(record, "faelle_covid_aktuell_beatmet"));
	                icd.setIntensiveCareUnitsFree((int)CsvUtils.getDoubleWithDefaultIfMissingFrom(record, "betten_frei"));
	                icd.setIntensiveCareUnitsOccupied((int)CsvUtils.getDoubleWithDefaultIfMissingFrom(record, "betten_belegt"));
	                icd.setDate(date);
	
	                if (!existsDataFor(date, icd.getDistrict())) {
	                    intensiveCareDataRepository.save(icd);
	                }
	                log.trace("New ICD: {}", icd);
	            }
	            date = date.plusDays(1);
            }
        }
        log.debug("Finished importing intensive care data");
    }

    protected boolean existsDataFor(LocalDate date, Region region) {
        IntensiveCareData icd = intensiveCareDataRepository.findByDateAndDistrict(date, region);
        if (icd != null) {
            log.debug("IntensiveCareData already in DB: {}, {}", date, region.getName());
            return true;
        }
        return false;
    }

    private ReaderResult getReader(int fileCounter, LocalDate date) throws IOException {
        int startId = fileCounter;
        Reader reader = null;

        outer:
        while (fileCounter < (startId + 25)) {
            for (int i=0 ; i<dfs.length ; i++) {
                URL url = new URL(String.format(baseDailyCsvUrlPatterns[i], fileCounter, date.format(dfs[i])));
                String contentType = UrlUtils.getContentType(url);
                if (contentType.contains("csv")) {
                    reader = new InputStreamReader(url.openStream());
                    if (reader != null) {
                        log.debug("Found file under URL: {}", url.toString());
                        break outer;
                    }
                }
            }
            fileCounter++;
        }
        fileCounter -= 3;

        return new ReaderResult(fileCounter, reader);
    }

    private class ReaderResult {
        int lastId;
        Reader reader;

        public ReaderResult(int lastId, Reader reader) {
            this.lastId = lastId;
            this.reader = reader;
        }
    }

}
