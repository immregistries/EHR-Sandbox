package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController()
@RequestMapping({PATIENT_ID_PATH + "/recommendations"})
public class RecommendationController {
    Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    @Autowired()
    FhirComponentsDispatcher fhirComponentsDispatcher;

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping()
    public ResponseEntity<Set<String>> getAll(@PathVariable(FACILITY_ID) Integer facilityId, @PathVariable(PATIENT_ID) Integer patientId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser();
        Set<String> set = recommendationService.getPatientMap(facilityId, patientId).entrySet().stream().map(
                        entry -> parser.encodeResourceToString(entry.getValue()))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(set);
    }

}
