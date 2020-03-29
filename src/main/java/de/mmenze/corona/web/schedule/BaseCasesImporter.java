package de.mmenze.corona.web.schedule;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.DeltaCases;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.repository.CasesRepository;
import de.mmenze.corona.repository.DeltaCasesRepository;
import de.mmenze.corona.repository.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Slf4j
public abstract class BaseCasesImporter {

    @Autowired
    protected CasesRepository casesRepository;
    @Autowired
    protected RegionRepository regionRepository;
    @Autowired
    protected DeltaCasesRepository deltaCasesRepository;


    protected boolean existsCasesFor(LocalDate date, Region region) {
        Cases cases = casesRepository.findByDateAndRegion(date, region);
        if (cases != null) {
            log.debug("cases already in DB: {}, {}", date, region.getName());
            return  true;
        }
        return false;
    }

    protected Cases getCasesForDateNotNull(LocalDate date, Region region) {
        Cases cases = casesRepository.findByDateAndRegion(date, region);
        if (cases == null) {
            cases = new Cases();
        }
        return cases;
    }

    protected void completeCasesAndDeltaCases(Cases cases, LocalDate date, Region region) {
        Cases yesterdayCases = getCasesForDateNotNull(date.minusDays(1), region);

        // values up to 50% of yesterdays are still OK (probably a correction in the data)
        if (cases.getConfirmed()*2 < yesterdayCases.getConfirmed()) {
            log.debug("data inconclusive for {} and {}: confirmed yesterday was {}, but today is {}",
                    date, region.getName(), yesterdayCases.getConfirmed(), cases.getConfirmed());
            cases.setConfirmed(yesterdayCases.getConfirmed());
        }
        if (cases.getDeaths()*2 < yesterdayCases.getDeaths()) {
            log.debug("data inconclusive for {} and {}: deaths yesterday was {}, but today is {}",
                    date, region.getName(), yesterdayCases.getDeaths(), cases.getDeaths());
            cases.setDeaths(yesterdayCases.getDeaths());
        }
        if (cases.getRecovered()*2 < yesterdayCases.getRecovered()) {
            log.debug("data inconclusive for {} and {}: recovered yesterday was {}, but today is {}",
                    date, region.getName(), yesterdayCases.getRecovered(), cases.getRecovered());
            cases.setRecovered(yesterdayCases.getRecovered());
        }

        // calculate additional values out of these base values
        cases.setActive(cases.getConfirmed() - cases.getRecovered() - cases.getDeaths());
        cases.setMortalityRate((double)cases.getDeaths() / (double)cases.getConfirmed());
        cases.setRecoveredRate((double)cases.getRecovered() / (double)cases.getConfirmed());

        // only persist, if there is something to persis
        if (cases.getConfirmed() > 0) {
            casesRepository.save(cases);

            // calculate additional entity data
            addDeltaCases(date, region, cases, yesterdayCases);
        }
    }

    protected void updateDeltaCases(Cases cases, DeltaCases deltaCases) {
        Cases yesterdayCases = getCasesForDateNotNull(cases.getDate().minusDays(1), cases.getRegion());
        calculateDeltaCases(deltaCases, cases, yesterdayCases);
    }

    protected void addDeltaCases(LocalDate date, Region region, Cases cases, Cases yesterdayCases) {
        DeltaCases deltaCases = new DeltaCases();
        deltaCases.setRegion(region);
        deltaCases.setBaseDate(date);
        calculateDeltaCases(deltaCases, cases, yesterdayCases);
    }

    private void calculateDeltaCases(DeltaCases deltaCases, Cases cases, Cases yesterdayCases) {
        Region region = deltaCases.getRegion();
        LocalDate date = deltaCases.getBaseDate();
        Cases cases7d = getCasesForDateNotNull(date.minusDays(7), region);
        Cases cases14d = getCasesForDateNotNull(date.minusDays(7), region);

        deltaCases.setIncreaseConfirmed1d(cases.getConfirmed() - yesterdayCases.getConfirmed());
        deltaCases.setIncreaseConfirmed7d(cases.getConfirmed() - cases7d.getConfirmed());
        deltaCases.setIncreaseConfirmed14d(cases.getConfirmed() - cases14d.getConfirmed());

        deltaCases.setIncreaseDeaths1d(cases.getDeaths() - yesterdayCases.getDeaths());
        deltaCases.setIncreaseDeaths7d(cases.getDeaths() - cases7d.getDeaths());
        deltaCases.setIncreaseDeaths14d(cases.getDeaths() - cases14d.getDeaths());

        deltaCases.setIncreaseRecovered1d(cases.getRecovered() - yesterdayCases.getRecovered());
        deltaCases.setIncreaseRecovered7d(cases.getRecovered() - cases7d.getRecovered());
        deltaCases.setIncreaseRecovered14d(cases.getRecovered() - cases14d.getRecovered());

        deltaCases.setDeltaActive1d(cases.getActive() - yesterdayCases.getActive());
        deltaCases.setDeltaActive7d(cases.getActive() - cases7d.getActive());
        deltaCases.setDeltaActive14d(cases.getActive() - cases14d.getActive());

        // this is not exact, just a linear interpolation
        // we look for the first day (in the past), when the number of confirmed was less than half
        // then we linearly interpolate between this day and the following to calculate a doubling rate
        // we ignore this for situations with less than 20 confirmed cases (99.9d is default)
        double rate = 0;
        if (cases.getConfirmed() > 20) {
            int confirmedHalf = cases.getConfirmed(), confirmedDayBefore = cases.getConfirmed();
            int cntDays = 0;
            while (confirmedHalf * 2 > cases.getConfirmed()) {
                cntDays++;
                Cases past = getCasesForDateNotNull(date.minusDays(cntDays), region);
                confirmedDayBefore = confirmedHalf;
                confirmedHalf = past.getConfirmed();
            }

            if (confirmedHalf > 0) {
                rate = cntDays - 1 + ((double)(confirmedDayBefore - (cases.getConfirmed() / 2))) / (double)(confirmedDayBefore - confirmedHalf);
                rate = (double)Math.round(rate * 10) / 10;
            }
        }
        deltaCases.setDoublingRate(rate);
        deltaCasesRepository.save(deltaCases);
    }

}
