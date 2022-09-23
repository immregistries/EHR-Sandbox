package org.immregistries.ehr.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.ResourceClient;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping("/new_patient")
    public Patient patient() {
        return new Patient();
    }

    @GetMapping("/random_patient")
    public Patient randomPatient() {
        return RandomGenerator.randomPatient(null,null);
    }

    @GetMapping("/random_vaccination")
    public VaccinationEvent randomVaccination() {
        return RandomGenerator.randomVaccinationEvent(null,null);
    }

    @GetMapping("/code_maps")
    public String codeMaps() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        CodeMap codeMap = CodeMapManager.getCodeMap();
//        logger.info("Code Maps fetched");
        return gson.toJson(codeMap);
    }

    @GetMapping("/settings")
    public ImmunizationRegistry settings() {
        return immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
    }

    @PutMapping("/settings")
    public ImmunizationRegistry putSettings(@RequestBody ImmunizationRegistry settings) {
        ImmunizationRegistry oldSettings = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        settings.setUser(oldSettings.getUser());
        settings.setId(oldSettings.getId());
        return immunizationRegistryRepository.save(settings);
    }



}
