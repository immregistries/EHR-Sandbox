package org.immregistries.ehr.api.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.Patient;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/new_patient")
    public Patient patient() {
        return new Patient();
    }

    @GetMapping("/random_patient")
    public Patient randomPatient() {
        return randomGenerator.randomPatient(null,null);
    }

    @GetMapping("/random_vaccination")
    public VaccinationEvent randomVaccination() {
        return randomGenerator.randomVaccinationEvent(null,null);
    }

    @GetMapping("/code_maps")
    public String codeMaps() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        CodeMap codeMap = codeMapManager.getCodeMap();
        return gson.toJson(codeMap);
    }

}
