package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r5.model.Immunization;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.hl7.fhir.r5.model.Meta;
import org.hl7.fhir.r5.model.Reference;
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RestController()
@Conditional(OnR5Condition.class)
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/recommendations"})
public class RecommendationController {

    Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    @Autowired
    FhirContext fhirContext;
    @GetMapping()
    public ResponseEntity<Set<String>> getAll(@PathVariable String patientId) {
        Set<String> set = new HashSet<>();
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        ImmunizationRecommendation immunizationRecommendation = new ImmunizationRecommendation()
                .setDate(new Date())
                .setPatient(new Reference(patientId));
        immunizationRecommendation
                .setId("009090909")
                .setMeta(new Meta().setLastUpdated(new Date()));
        logger.info("{}", parser.encodeResourceToString(immunizationRecommendation));
        set.add(parser.encodeResourceToString(immunizationRecommendation));
        return ResponseEntity.ok(set);
    }

}