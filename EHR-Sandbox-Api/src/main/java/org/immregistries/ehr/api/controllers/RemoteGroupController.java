package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.fhir.FhirComponentsService;
import org.immregistries.ehr.fhir.Server.ServerR5.GroupProviderR5;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

import static org.immregistries.ehr.logic.ResourceIdentificationService.FACILITY_SYSTEM;

//@RestController()
//@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}" + IMM_REGISTRY_SUFFIX + "/groups", IMM_REGISTRY_SUFFIX + "/groups"})
public class RemoteGroupController {
    Logger logger = LoggerFactory.getLogger(RemoteGroupController.class);
    @Autowired
    FhirComponentsService fhirComponentsService;
    @Autowired
    Map<Integer, Map<Integer, Map<String, Group>>> remoteGroupsStore;
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
    @Autowired
    EhrGroupRepository ehrGroupRepository;

    @GetMapping("/sample")
    public ResponseEntity<String> getSample(@PathVariable() Optional<Integer> facilityId) {
        Group group = new Group();
        group.setType(Group.GroupType.PERSON);
        long randn = Math.round(Math.random());
        group.setName("Generated " + randn);
        group.addIdentifier().setSystem("ehr-sandbox/group").setValue(String.valueOf(randn));
//        group.setManagingEntity(new Reference().setIdentifier(new Identifier().setSystem(FACILITY_SYSTEM).setValue(String.valueOf(facilityId))));
        if (facilityId.isPresent()) {
            group.setManagingEntity(new Reference().setIdentifier(new Identifier().setSystem("School-district-corporations").setValue(String.valueOf(facilityId.get()))));
        }
        Calendar periodStart = Calendar.getInstance();
        periodStart.set(Calendar.YEAR, 2023);
        periodStart.set(Calendar.MONTH, 8);
        periodStart.set(Calendar.DAY_OF_MONTH, 1);
        Calendar periodEnd = Calendar.getInstance();
        periodEnd.set(Calendar.YEAR, 2024);
        periodEnd.set(Calendar.MONTH, 6);
        periodEnd.set(Calendar.DAY_OF_MONTH, 31);
//        Group.GroupCharacteristicComponent grade = group.addCharacteristic()
//                .setCode(new CodeableConcept(new Coding("Group-Schooling-code-definition","Grade","Grade")))
//                .setValue(new StringType("8"))
//                .setPeriod(new Period().setStart(periodStart.getTime()).setEnd(periodEnd.getTime()));
//        Group.GroupCharacteristicComponent school = group.addCharacteristic()
//                .setCode(new CodeableConcept(new Coding("http/Massachusetts.com/terminology/school-code-system","1234","Stephane Hessel Highschool")));
        group.setDescription("Group created for School example");
        group.setMembership(Group.GroupMembershipBasis.ENUMERATED);
        return ResponseEntity.ok().body(fhirComponentsService.fhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(group));
    }

    @GetMapping()
    public ResponseEntity<Set<String>> getAll(@PathVariable() String facilityId, @PathVariable() String registryId) {
        IParser parser = fhirComponentsService.fhirContext().newJsonParser();
//        ehrGroupRepository.findByFacilityIdAndImmunizationRegistryId(facilityId, registryId);

        Set<String> set = remoteGroupsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(registryId, new HashMap<>(0))
                .entrySet().stream().map(
                        entry -> parser.encodeResourceToString(entry.getValue()))
                .collect(Collectors.toSet());
        return ResponseEntity.ok(set);
    }


    /**
     * Fetches remote groups from registry to store them locally and make them available
     *
     * @param facilityId
     * @param registryId
     * @return
     */
    @GetMapping("/$fetch")
    public ResponseEntity<Set<String>> fetchFromIis(@PathVariable() String facilityId, @PathVariable() String registryId) {
        IParser parser = fhirComponentsService.fhirContext().newJsonParser();
        ServletRequestDetails servletRequestDetails = new ServletRequestDetails();
        servletRequestDetails.setTenantId(String.valueOf(facilityId));
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.getImmunizationRegistry(registryId);
        Bundle bundle = fhirComponentsService.clientFactory().newGenericClient(immunizationRegistry).search().forResource(Group.class).returnBundle(Bundle.class)
//                .where(Group.MANAGING_ENTITY.hasId(String.valueOf(facilityId)))
//                .where(Group.MANAGING_ENTITY.hasId("Organization/"+facilityId)) // TODO set criteria
                .execute();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.hasResource() && entry.getResource() instanceof Group
//                    && ((Group) entry.getResource()).getManagingEntity().getIdentifier().getValue().equals(String.valueOf(facilityId))
            ) {
                groupProviderR5.update((Group) entry.getResource(), servletRequestDetails, immunizationRegistry);
            }
        }
        return getAll(facilityId, registryId);
    }

    @PostMapping("/{groupId}/$member-add")
    public ResponseEntity<String> add_member(@PathVariable() String tenantId, @PathVariable() String facilityId, @PathVariable() String registryId, @PathVariable() String groupId, @RequestParam String patientId, @RequestParam Optional<Boolean> match) {
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId, patientId).orElseThrow();
        Patient patient = patientMapperR5.toFhir(ehrPatient);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.getImmunizationRegistry(registryId);
        Parameters in = new Parameters();
        /**
         * First do match to get destination reference or identifier
         */
        if (match.isPresent() && match.get()) {
            Bundle bundle = fhirClientController.matchPatientOperation(facilityId, registryId, patientId, null);
            if (!bundle.hasEntry()) {
                return ResponseEntity.internalServerError().body("Patient $match failed : IIS does not know about this patient");
            }
            String id = bundle.getEntryFirstRep().getResource().getId();

            in.addParameter("patientReference", new Reference(id).setIdentifier(patient.getIdentifierFirstRep()));
        } else {
            in.addParameter("memberId", patient.getIdentifierFirstRep());
            in.addParameter("providerNpi", new Identifier().setSystem(FACILITY_SYSTEM).setValue(String.valueOf(facilityId)));
            ;
        }

        IGenericClient client = fhirComponentsService.clientFactory().newGenericClient(immunizationRegistry);
        Group group = remoteGroupsStore.getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(registryId, new HashMap<>(0)).get(groupId);
        Parameters out = client.operation().onInstance(group.getIdElement()).named("$member-add").withParameters(in).execute();

        /**
         * update after result ? or wait for subscription to do the job, maybe better to do it for bulk testing
         */
        fetchFromIis(facilityId, registryId);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/{groupId}/$member-remove")
    public ResponseEntity<String> remove_member(@PathVariable() String facilityId,
                                                @PathVariable() String registryId,
                                                @PathVariable() String groupId,
                                                @RequestParam() Optional<String> patientId,
                                                @RequestParam() Optional<Identifier> identifier,
                                                @RequestParam() Optional<String> reference
    ) {
        Parameters in = new Parameters();
        if (patientId.isPresent()) {
            EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId, patientId.get()).orElseThrow();
            Patient patient = patientMapperR5.toFhir(ehrPatient);
            in.addParameter("memberId", patient.getIdentifierFirstRep());
        }
        identifier.ifPresent(value -> in.addParameter("memberId", value));
        reference.ifPresent(value -> in.addParameter("patientReference", new Reference(value)));

        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.getImmunizationRegistry(registryId);
        /**
         * First do match to get destination reference or identifier
         */


        logger.info("{}", in);
        IGenericClient client = fhirComponentsService.clientFactory().newGenericClient(immunizationRegistry);
        Group group = remoteGroupsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(registryId, new HashMap<>(0))
                .get(groupId);
        Parameters out = client.operation().onInstance(group.getIdElement()).named("$member-remove").withParameters(in).execute();

        /**
         * update after result ? or wait for subscription to do the job, maybe better to do it for bulk testing
         */
//        groupsStore.get(facilityId).put(immunizationRegistry.getId(), group);
        fetchFromIis(facilityId, registryId);
        return ResponseEntity.ok("");
    }

}
