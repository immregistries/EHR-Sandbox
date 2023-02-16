package org.immregistries.ehr.api.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class MainController {
    @Autowired
    CodeMapManager codeMapManager;
    @Autowired
    RandomGenerator randomGenerator;

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private FacilityRepository facilityRepository;

    @GetMapping("/$random_patient")
    public EhrPatient randomPatient() {
        return randomGenerator.randomPatient(null,null);
    }

    @GetMapping("/$random_vaccination")
    public VaccinationEvent randomVaccination() {
        return randomGenerator.randomVaccinationEvent(null,null);
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
