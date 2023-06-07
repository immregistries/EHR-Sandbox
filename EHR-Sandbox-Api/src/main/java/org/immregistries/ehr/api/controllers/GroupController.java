package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Group;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Reference;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.Client.CustomClientBuilder;
import org.immregistries.ehr.fhir.Client.ResourceClient;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
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
    CustomClientBuilder customClientBuilder;
    @Autowired
    Map<Integer, Map<Integer, Group>> groupsStore;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private PatientMapperR5 patientMapperR5;

    @GetMapping()
    public ResponseEntity<Set<String>> getAll(@PathVariable Integer facilityId) {
        IParser parser = fhirContext.newJsonParser();
        Set<String> set = groupsStore
                .getOrDefault(facilityId, new HashMap<>(0)).entrySet().stream().map(
                        entry -> parser.encodeResourceToString(entry.getValue()))
                .collect(Collectors.toSet());
        return ResponseEntity.ok(set);
    }

    /**
     * Disabled to be replaced by fhir operations
     */
//    @PutMapping()
//    public ResponseEntity<String> update(@RequestBody Group group, @PathVariable Integer facilityId, @PathVariable Integer immRegistryId) {
//        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(immRegistryId);
//        IParser parser = fhirContext.newJsonParser();
//        groupsStore.putIfAbsent(facilityId, new HashMap<>(5));
//        groupsStore.get(facilityId).putIfAbsent(immunizationRegistry.getId(), group);
//        // TODO send to IIS & deal with ID
//        return ResponseEntity.ok(parser.encodeResourceToString(group));
//    }

    public ResponseEntity<String> add_member(@PathVariable Integer facilityId, @PathVariable Integer immRegistryId, @RequestParam String patientId) {
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId,patientId).orElseThrow();
        Patient patient = patientMapperR5.toFhirPatient(ehrPatient);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(immRegistryId);
        /**
         * First do match to get destination reference or identifier
         */
//        IParser parser = fhirContext.newJsonParser();
        Parameters in = new Parameters()
                .addParameter("patientReference", new Reference(patient.getId()));
        Parameters out = add_member_operation(facilityId,immunizationRegistry,in);


        /**
         * update after result ? or wait for subscription to do the job, maybe better to do it for bulk testing
         */
//        groupsStore.get(facilityId).put(immunizationRegistry.getId(), group);
        return ResponseEntity.ok("");
    }

    public Parameters add_member_operation(Integer facilityId, ImmunizationRegistry immunizationRegistry, Parameters in) {
        IGenericClient client = customClientBuilder.newGenericClient(immunizationRegistry);
        Group group = groupsStore.getOrDefault(facilityId, new HashMap<>(0)).get(immunizationRegistry.getId());

        return client.operation().onInstance(group.getIdElement()).named("$add-member").withParameters(in).execute();


    }


}
