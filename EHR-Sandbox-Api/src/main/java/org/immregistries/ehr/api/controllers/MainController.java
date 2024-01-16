package org.immregistries.ehr.api.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.AuditRevisionEntityRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.logic.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/")
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    @Autowired
    CodeMapManager codeMapManager;
    @Autowired
    RandomGenerator randomGenerator;
    @Autowired
    AuditRevisionEntityRepository auditRevisionEntityRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private FacilityRepository facilityRepository;

    @GetMapping("/$random_patient")
    public EhrPatient randomPatient() {
        return randomGenerator.randomPatient(null);
    }

    @GetMapping("/$random_vaccination")
    public VaccinationEvent randomVaccination() {
        return randomGenerator.randomVaccinationEvent(null, null, null);
    }

    /**
     * Used by frontend to check if a refresh is needed on the current facility it is displaying
     *
     * @return
     */
    @GetMapping("/$notification")
    public Boolean notificationCheck(@RequestParam Optional<Long> timestamp) {
        return auditRevisionEntityRepository.existsByUserAndTimestampGreaterThan(
                userDetailsService.currentUserId(),
                timestamp.orElse(0L));
//        return auditRevisionEntityRepository.existsByUserAndTimestampGreaterThanAndSubscriptionIdNotNull(
//                userDetailsService.currentUserId(),
//                timestamp.orElse(0L));
    }

    @GetMapping("/code_maps")
    public String codeMaps() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        CodeMap codeMap = codeMapManager.getCodeMap();
        return gson.toJson(codeMap);
    }

    @GetMapping("/facilities")
    public Iterable<Facility> facilities() {
        return facilityRepository.findByUser(userDetailsService.currentUser());
    }

}
