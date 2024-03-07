package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Group;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Parameters;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.ServerR5.GroupProviderR5;
import org.immregistries.ehr.logic.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.immregistries.ehr.logic.ResourceIdentificationService.FACILITY_SYSTEM;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/groups"})
public class EhrGroupController {
    Logger logger = LoggerFactory.getLogger(RemoteGroupController.class);
    @Autowired
    CustomClientFactory customClientFactory;
    @Autowired
    Map<Integer, Map<Integer, Map<String, Group>>> remoteGroupsStore;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private GroupProviderR5 groupProviderR5;
    @Autowired
    private FhirClientController fhirClientController;

    /**
     * hollow method for url mapping
     * query is actually executed in authorization filter
     *
     * @param ehrGroup
     * @return
     */
    @GetMapping("/{groupId}")
    public EhrGroup get(@RequestAttribute EhrGroup ehrGroup) {
        return ehrGroup;
    }

    @PostMapping()
    @Transactional()
    public ResponseEntity<String> post(@RequestAttribute Facility facility,
                                         @RequestBody EhrGroup ehrGroup) {
        if (ehrGroupRepository.existsByFacilityIdAndName(facility.getId(), ehrGroup.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used for this facility");
        } else {
            ehrGroup.setFacility(facility);
            Set<EhrGroupCharacteristic> characteristics = ehrGroup.getEhrGroupCharacteristics();
            ehrGroup.setEhrGroupCharacteristics(null);
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            /**
             * Relation with complex embeddedId is not supported in spring so references have to be solved manually
             */
            for (EhrGroupCharacteristic characteristic: characteristics) {
                characteristic.setGroupId(newEntity.getId());
            }
            ehrGroup.setEhrGroupCharacteristics(characteristics);
            newEntity = ehrGroupRepository.save(ehrGroup);
            return new ResponseEntity<>(newEntity.getId(), HttpStatus.CREATED);
        }
    }


    @PutMapping({"", "/{groupId}"})
    @Transactional
    public ResponseEntity<EhrGroup> put(@RequestAttribute Facility facility, @RequestBody EhrGroup ehrGroup) {
        EhrGroup oldEntity = ehrGroupRepository.findByFacilityIdAndId(facility.getId(), ehrGroup.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid ids"));
        Optional<EhrGroup> groupUsingNewName = ehrGroupRepository.findByFacilityIdAndId(facility.getId(), ehrGroup.getId());
        if (groupUsingNewName.isPresent() && !groupUsingNewName.get().getId().equals(oldEntity.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used by another group");
        } else {
            ehrGroup.setFacility(facility);
            /**
             * Relation with complex embeddedId is not supported in spring so references have to be solved manually
             */
            for (EhrGroupCharacteristic characteristic: ehrGroup.getEhrGroupCharacteristics()) {
                characteristic.setGroupId(ehrGroup.getId());
            }
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            return new ResponseEntity<>(newEntity, HttpStatus.ACCEPTED);
        }
    }

    @GetMapping("/$random")
    public EhrGroup random(@RequestAttribute Facility facility) {
        Faker faker = new Faker();
        EhrGroup ehrGroup = new EhrGroup();
        ehrGroup.setName(faker.educator().campus());
        ehrGroup.setDescription("Randomly generated group in EHR Sandbox including randomly selected patients in facility");
        ehrGroup.setType("Person");

        EhrGroupCharacteristic ehrGroupCharacteristic = new EhrGroupCharacteristic();
        ehrGroupCharacteristic.setCodeSystem("Some-State-Denomination-System/" + RandomStringUtils.random(3, false, true));
        ehrGroupCharacteristic.setCodeValue("Grade"); // Date, tranfered students, majors, special program
        ehrGroupCharacteristic.setValue(RandomStringUtils.random(1, false, true));
        ehrGroup.setEhrGroupCharacteristics(new HashSet<>(1));
        ehrGroup.getEhrGroupCharacteristics().add(ehrGroupCharacteristic);

//        Iterator<EhrPatient> facilityPatients = ehrPatientRepository.findByFacilityId(facility.getId()).iterator();
//
//        ehrGroup.setPatientList(new HashSet<>(4));
//        Random random = new Random();
//        EhrPatient patient = null;
//        while (facilityPatients.hasNext()) {
//            patient = facilityPatients.next();
//            if (random.nextFloat() > 0.7) {
//                ehrGroup.getPatientList().add(patient);
//            }
//        }
//        if (ehrGroup.getPatientList().isEmpty() && patient != null) {
//            ehrGroup.getPatientList().add(patient);
//        }
        return ehrGroup;
    }


    @GetMapping()
    public ResponseEntity<?> getAll(@RequestAttribute Facility facility, @RequestParam Optional<String> name) {
        if (name.isPresent()) {
            return ResponseEntity.ok(ehrGroupRepository.findByFacilityIdAndName(facility.getId(), name.get())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found")));
        } else {
            return ResponseEntity.ok(ehrGroupRepository.findByFacilityId(facility.getId()));
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not supported yet");
        }
    }

    @PostMapping("/{groupId}/$add")
    public EhrGroup add(@RequestAttribute EhrGroup ehrGroup, @RequestParam String patientId) {
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient Not found"));
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            Set<EhrPatient> set = ehrGroup.getPatientList();
            set.add(ehrPatient);
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            return newEntity;
//            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
        } else {
            Parameters in = new Parameters();
            /**
             * First do match to get destination reference or identifier
             */
            in.addParameter("memberId", new Identifier().setValue(ehrPatient.getMrn()).setSystem(ehrPatient.getMrnSystem()));
            in.addParameter("providerNpi", new Identifier().setSystem(FACILITY_SYSTEM).setValue(ehrGroup.getFacility().getId())); //TODO update with managingEntity
            IBaseResource remoteGroup = getRemoteGroup(ehrGroup);
            groupProviderR5.update((Group) remoteGroup, ehrGroup.getFacility(), immunizationRegistry);
            IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
            Parameters out = client.operation().onInstance(remoteGroup.getIdElement()).named("$member-add").withParameters(in).execute();
            /**
             * update after result ? or wait for subscription to do the job, maybe better to do it for bulk testing
             */
            return refreshOne(ehrGroup);
        }
    }

    @PostMapping("/{groupId}/$remove")
    public EhrGroup remove_member(@RequestAttribute EhrGroup ehrGroup, @RequestParam String patientId) {
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient Not found"));
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            ehrGroup.getPatientList().remove(ehrPatient);
            ehrGroupRepository.save(ehrGroup);
            return ehrGroup;
//            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
        } else {
            Parameters in = new Parameters();
            in.addParameter("memberId", new Identifier().setValue(ehrPatient.getMrn()).setSystem(ehrPatient.getMrnSystem()));
            in.addParameter("providerNpi", new Identifier().setSystem(FACILITY_SYSTEM).setValue(ehrGroup.getFacility().getId())); //TODO update with managingEntity
            /**
             * First do match to get destination reference or identifier
             */
            IBaseResource remoteGroup = getRemoteGroup(ehrGroup);
            IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
            Parameters out = client.operation().onInstance(remoteGroup.getIdElement()).named("$member-remove").withParameters(in).execute();
            return refreshOne(ehrGroup);
        }
    }

    @GetMapping("/{groupId}/$refresh")
    public EhrGroup refreshOne(@RequestAttribute EhrGroup ehrGroup) {
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        IBaseResource remoteGroup = getRemoteGroup(ehrGroup);
        if (remoteGroup != null) {
            groupProviderR5.update((Group) remoteGroup, ehrGroup.getFacility(), immunizationRegistry);
        }
        return ehrGroupRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), ehrGroup.getId()).get();
    }


    private IBaseResource getRemoteGroup(EhrGroup ehrGroup) {
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
        }
        IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
        return getRemoteGroup(client, ehrGroup);
    }

    private IBaseResource getRemoteGroup(IGenericClient client, EhrGroup ehrGroup) {
        Bundle bundle = client.search().forResource(Group.class).returnBundle(Bundle.class)
                .where(Group.NAME.matchesExactly().value(ehrGroup.getName()))
//                .where(Group.MANAGING_ENTITY.hasId("Organization/"+facilityId)) // TODO set criteria
                .execute();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.hasResource() && entry.getResource() instanceof Group
//                    && ((Group) entry.getResource()).getManagingEntity().getIdentifier().getValue().equals(String.valueOf(facilityId))
            ) {
                return entry.getResource();
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");


    }


}
