package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController()
@Conditional(OnR5Condition.class)
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/recommendations"})
public class RecommendationController {
    Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    @Autowired
    FhirContext fhirContext;

    @Autowired
    Map<Integer, Set<ImmunizationRecommendation>> immunizationRecommendationsStore;
    @Autowired
    private FacilityRepository facilityRepository;

    @GetMapping()
    public ResponseEntity<Set<String>> getAll(@PathVariable Integer facilityId,@PathVariable String patientId) {
        IParser parser = fhirContext.newJsonParser();
        Set<String> set = immunizationRecommendationsStore
                .getOrDefault(facilityId, new HashSet<>())
                .stream()
                .filter(immunizationRecommendation -> immunizationRecommendation.getPatient().getReference().equals(patientId))
                .map(parser::encodeResourceToString).collect(Collectors.toSet());
        return ResponseEntity.ok(set);
    }

}
