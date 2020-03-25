package de.mmenze.corona.web;

import de.mmenze.corona.web.schedule.BundeslandCasesImportScheduledHandler;
import de.mmenze.corona.web.schedule.WorldCasesImportScheduledHandler;
import de.mmenze.corona.web.schedule.CountriesOfTheWorldImportScheduledHandler;
import de.mmenze.corona.web.service.SendMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoronaController {

    @Autowired
    private WorldCasesImportScheduledHandler coronaHandler;

    @GetMapping("/api/import/world")
    public void importCsvs() {
        coronaHandler.importAllWorldDataCsv();
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
