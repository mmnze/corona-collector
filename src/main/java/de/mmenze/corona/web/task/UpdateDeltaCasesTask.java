package de.mmenze.corona.web.task;

import de.mmenze.corona.domain.Cases;
import de.mmenze.corona.domain.DeltaCases;
import de.mmenze.corona.domain.Region;
import de.mmenze.corona.repository.CasesRepository;
import de.mmenze.corona.repository.DeltaCasesRepository;
import de.mmenze.corona.repository.RegionRepository;
import de.mmenze.corona.web.schedule.BaseCasesImporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Especially when importing new country date from Johns Hopkins, manual
 * modifications might be required, some data needs to be recalculated then.
 */
@Slf4j
@Controller
public class UpdateDeltaCasesTask extends BaseCasesImporter {

    /**
     * This task assumes that regions and cases are corrected already, only delta cases need
     * to be recalculated. To make things easy, this is done for a complete region only.
     */
    @GetMapping("/task/update/delta")
    public void updateDeltaCases(@RequestParam long regionId) {
        log.debug("Starting to update delta cases for regionId {}", regionId);
        Region region = regionRepository.findById(regionId).get();
        if (region == null) {
            log.warn("could not find region with ID {}", regionId);
            return;
        }

        List<Cases> cases = casesRepository.findAllByRegion(region);
        for (Cases c: cases) {
            DeltaCases dc = deltaCasesRepository.findByBaseDateAndRegion(c.getDate(), region);
            if (dc != null) {
                updateDeltaCases(c, dc);
            } else {
                log.warn("Task is just supposed to update, but no delta case found for {} and {}", region.getName(), c.getDate());
            }
        }
        log.debug("Done update delta cases", regionId);
    }

}
