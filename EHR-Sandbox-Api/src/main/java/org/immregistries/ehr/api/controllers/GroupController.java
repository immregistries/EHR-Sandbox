package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r5.model.Group;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.immregistries.ehr.api.controllers.FhirClientController.IMM_REGISTRY_SUFFIX;

@RestController()
@Conditional(OnR5Condition.class)
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}" + IMM_REGISTRY_SUFFIX + "/groups"})
public class GroupController {
    Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    @Autowired
    FhirContext fhirContext;

    @Autowired
    Map<Integer, Map<Integer, Group>> groupsStore;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;

    @GetMapping()
    public ResponseEntity<Set<String>> getAll(@PathVariable Integer facilityId, @PathVariable String patientId) {
        IParser parser = fhirContext.newJsonParser();
        Set<String> set = groupsStore
                .getOrDefault(facilityId, new HashMap<>(0)).entrySet().stream().map(
                        entry -> parser.encodeResourceToString(entry.getValue()))
                .collect(Collectors.toSet());
        return ResponseEntity.ok(set);
    }

    @PutMapping()
    public ResponseEntity<String> update(@RequestBody Group group, @PathVariable Integer facilityId, @PathVariable Integer immRegistryId) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(immRegistryId);
        IParser parser = fhirContext.newJsonParser();
        groupsStore.putIfAbsent(facilityId, new HashMap<>(5));
        groupsStore.get(facilityId).putIfAbsent(immunizationRegistry.getId(), group);
        // TODO send to IIS
        return ResponseEntity.ok(parser.encodeResourceToString(group));

    }


}
