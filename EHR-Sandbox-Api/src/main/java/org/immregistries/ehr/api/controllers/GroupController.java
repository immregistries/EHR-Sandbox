package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.ServerR5.GroupProviderR5;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.immregistries.ehr.api.controllers.FhirClientController.IMM_REGISTRY_SUFFIX;
import static org.immregistries.ehr.logic.ResourceIdentificationService.FACILITY_SYSTEM;

@RestController()
@Conditional(OnR5Condition.class)
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}" + IMM_REGISTRY_SUFFIX + "/groups"})
public class GroupController {
    Logger logger = LoggerFactory.getLogger(GroupController.class);
    @Autowired
    FhirContext fhirContext;
    @Autowired
    CustomClientFactory customClientFactory;
    @Autowired
    Map<Integer, Map<Integer, Map<String, Group>>> groupsStore;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private GroupProviderR5 groupProviderR5;
    @Autowired
    private PatientMapperR5 patientMapperR5;
    @Autowired
    private FhirClientController fhirClientController;

    @GetMapping("/sample")
    public ResponseEntity<String> getSample(@PathVariable() int facilityId) {
        Group group = new Group();
        group.setType(Group.GroupType.PERSON);
        long randn = Math.round(Math.random());
        group.setName("Generated " + randn);
        group.addIdentifier().setSystem("ehr-sandbox/group").setValue(String.valueOf(randn));
        group.setManagingEntity(new Reference().setIdentifier(new Identifier().setSystem(FACILITY_SYSTEM).setValue(String.valueOf(facilityId))));
        group.setDescription("Generated sample Group in EHR sandbox for testing");
        return ResponseEntity.ok().body(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(group));
    }

    @GetMapping()
    public ResponseEntity<Set<String>> getAll(@PathVariable Integer facilityId,@PathVariable Integer registryId) {
        IParser parser = fhirContext.newJsonParser();
        Set<String> set = groupsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(registryId,new HashMap<>(0))
                .entrySet().stream().map(
                        entry -> parser.encodeResourceToString(entry.getValue()))
                .collect(Collectors.toSet());
        return ResponseEntity.ok(set);
    }


    /**
     * Fetches remote groups from registry to store them locally and make them available
     * @param facilityId
     * @param registryId
     * @return
     */
    @GetMapping("/$fetch")
    public ResponseEntity<Set<String>> fetchFromIis(@PathVariable Integer facilityId, @PathVariable Integer registryId) {
        IParser parser = fhirContext.newJsonParser();
        ServletRequestDetails servletRequestDetails = new ServletRequestDetails();
        servletRequestDetails.setTenantId(String.valueOf(facilityId));
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(registryId);
        Bundle bundle = customClientFactory.newGenericClient(immunizationRegistry).search().forResource(Group.class).returnBundle(Bundle.class)
//                .where(Group.MANAGING_ENTITY.hasId(String.valueOf(facilityId)))
//                .where(Group.MANAGING_ENTITY.hasId("Organization/"+facilityId)) // TODO set criteria
                .execute();
        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            if (entry.hasResource() && entry.getResource() instanceof Group && ((Group) entry.getResource()).getManagingEntity().getIdentifier().getValue().equals(String.valueOf(facilityId))) {
                groupProviderR5.update((Group) entry.getResource(), servletRequestDetails, immunizationRegistry);
            }
        }
        return getAll(facilityId, registryId);
    }

    @PostMapping("/{groupId}/$member-add")
    public ResponseEntity<String> add_member(@PathVariable Integer facilityId, @PathVariable Integer registryId, @PathVariable String groupId, @RequestParam String patientId, @RequestParam Optional<Boolean> match) {
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId,patientId).orElseThrow();
        Patient patient = patientMapperR5.toFhirPatient(ehrPatient);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(registryId);
        Parameters in = new Parameters();
        /**
         * First do match to get destination reference or identifier
         */
        if (match.isPresent() && match.get()) {
            Bundle bundle = fhirClientController.matchPatientOperation(facilityId,registryId,patientId,null);
            if (!bundle.hasEntry()) {
                return ResponseEntity.internalServerError().body("Patient $match failed : IIS does not know about this patient");
            }
            String id = bundle.getEntryFirstRep().getResource().getId();

            in.addParameter("patientReference", new Reference(id).setIdentifier(patient.getIdentifierFirstRep()));
        } else {
            in.addParameter("memberId", patient.getIdentifierFirstRep());
            in.addParameter("providerNpi", new Identifier().setSystem(FACILITY_SYSTEM).setValue(String.valueOf(facilityId)));;
        }

        IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
        Group group = groupsStore.getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(registryId, new HashMap<>(0)).get(groupId);
        Parameters out = client.operation().onInstance(group.getIdElement()).named("$member-add").withParameters(in).execute();

        /**
         * update after result ? or wait for subscription to do the job, maybe better to do it for bulk testing
         */
        fetchFromIis(facilityId,registryId);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/{groupId}/$member-remove")
    public ResponseEntity<String> remove_member(@PathVariable Integer facilityId,
                                                @PathVariable Integer registryId,
                                                @PathVariable String groupId,
                                                @RequestParam() Optional<String> patientId,
                                                @RequestParam() Optional<Identifier> identifier,
                                                @RequestParam() Optional<String> reference
    ) {
        Parameters in = new Parameters();
        if (patientId.isPresent()){
            EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId,patientId.get()).orElseThrow();
            Patient patient = patientMapperR5.toFhirPatient(ehrPatient);
            in.addParameter("memberId", patient.getIdentifierFirstRep());
        }
        identifier.ifPresent(value -> in.addParameter("memberId", value));
        reference.ifPresent(value -> in.addParameter("patientReference", new Reference(value)));

        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(registryId);
        /**
         * First do match to get destination reference or identifier
         */


        logger.info("{}",in);
        IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
        Group group = groupsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(registryId, new HashMap<>(0))
                .get(groupId);
        Parameters out = client.operation().onInstance(group.getIdElement()).named("$member-remove").withParameters(in).execute();

        /**
         * update after result ? or wait for subscription to do the job, maybe better to do it for bulk testing
         */
//        groupsStore.get(facilityId).put(immunizationRegistry.getId(), group);
        fetchFromIis(facilityId,registryId);
        return ResponseEntity.ok("");
    }

}
