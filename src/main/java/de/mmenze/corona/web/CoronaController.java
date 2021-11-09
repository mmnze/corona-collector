package de.mmenze.corona.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import de.mmenze.corona.web.schedule.BundeslandCasesImportScheduledHandler;
import de.mmenze.corona.web.schedule.CountriesOfTheWorldImportScheduledHandler;
import de.mmenze.corona.web.schedule.DistrictCasesImportScheduledHandler;
import de.mmenze.corona.web.schedule.WorldCasesImportScheduledHandler;
import de.mmenze.corona.web.service.SendMailService;

@Controller
public class CoronaController {

    @Autowired
    private DistrictCasesImportScheduledHandler districtCasesImportScheduledHandler;

    @GetMapping("/api/import/districts")
    public void importDistricts() throws Exception {
        districtCasesImportScheduledHandler.importDistrictData();
    }

    @GetMapping("/api/import/districts/history")
    public void importHistoryDistricts() throws Exception {
        districtCasesImportScheduledHandler.importDistrictData(true);
    }



    @Autowired
    private WorldCasesImportScheduledHandler coronaHandler;

    @GetMapping("/api/import/world/all")
    public void importWorldAll() throws Exception {
        coronaHandler.importAllWorldDataCsv();
    }
    @GetMapping("/api/import/world")
    public void importCsvs() throws Exception {
        coronaHandler.importLastWorldDataCsv();
    }
    @GetMapping("/api/import/world/force")
    public void importForceCsvs() throws Exception {
        coronaHandler.importLastWorldDataCsv(true);
    }


    @Autowired
    private BundeslandCasesImportScheduledHandler bundeslandCasesImportScheduledHandler;

    @GetMapping("/api/import/states")
    public void importStates() throws Exception {
        bundeslandCasesImportScheduledHandler.importBundeslandData();
    }


    @Autowired
    private CountriesOfTheWorldImportScheduledHandler countriesOfTheWorldImportScheduledHandler;

    @GetMapping("/api/import/countries")
    public void importCountries() throws Exception {
        countriesOfTheWorldImportScheduledHandler.importCountryData();
    }


    @Autowired
    private SendMailService sendMailService;

    @GetMapping("/api/sendmail")
    public void testSendMail() {
        sendMailService.sendMail("Dies ist ein Test", "sw.toString()");
    }

}
