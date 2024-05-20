package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Group;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Parameters;
import org.immregistries.ehr.BulkImportController;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.ServerR5.GroupProviderR5;
import org.immregistries.ehr.logic.BundleImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.immregistries.ehr.logic.ResourceIdentificationService.FACILITY_SYSTEM;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/groups"})
public class EhrGroupController {
    @Autowired
    Map<String, BulkImportStatus> resultCacheStore;

    Logger logger = LoggerFactory.getLogger(RemoteGroupController.class);
    @Autowired
    CustomClientFactory customClientFactory;
    //    @Autowired
//    Map<Integer, Map<Integer, Map<String, Group>>> remoteGroupsStore;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private GroupProviderR5 groupProviderR5;
    @Autowired
    private FhirClientController fhirClientController;
    @Autowired
    private FhirConversionController fhirConversionController;
    @Autowired
    private BulkImportController bulkImportController;
    @Autowired
    private BundleImportService bundleImportService;

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
            for (EhrGroupCharacteristic characteristic : characteristics) {
                characteristic.setGroupId(newEntity.getId());
            }
            ehrGroup.setEhrGroupCharacteristics(characteristics);
            newEntity = ehrGroupRepository.save(ehrGroup);
            if (ehrGroup.getImmunizationRegistry() != null) {
                fhirClientController.postResource(ehrGroup.getImmunizationRegistry().getId(), "Group", fhirConversionController.groupResource(newEntity.getId(), facility).getBody());
            }
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
            for (EhrGroupCharacteristic characteristic : ehrGroup.getEhrGroupCharacteristics()) {
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
            Iterable<EhrGroup> ehrGroups = ehrGroupRepository.findByFacilityId(facility.getId());
            Stream<EhrGroup> stream = StreamSupport.stream(ehrGroups.spliterator(), false);
            for (Facility childFacility : facilityRepository.findByParentFacilityId(facility.getId())) {
                Iterable<EhrGroup> childEhrGroups = ehrGroupRepository.findByFacilityId(childFacility.getId());
                stream = Stream.concat(stream, StreamSupport.stream(childEhrGroups.spliterator(), false));
            }
            return ResponseEntity.ok().body(stream.collect(Collectors.toSet()));
        }
    }

    @GetMapping("/{groupId}/$import-status")
    public ResponseEntity<BulkImportStatus> getImportStatus(@PathVariable String groupId) {
        return ResponseEntity.ok(resultCacheStore.get(groupId));
    }

    @PostMapping("/{groupId}/$import-view-result")
    public ResponseEntity<Set<EhrEntity>> getImportStatusResult(@PathVariable String groupId, @RequestBody String url) {
//        BulkImportStatus bulkImportStatus = resultCacheStore.get(groupId);
        EhrGroup group = ehrGroupRepository.findById(groupId).get();
        return bulkImportController.viewBulkResult(group.getImmunizationRegistry().getId(), group.getFacility(), url);
    }

    @GetMapping("/{groupId}/$import")
    @PostMapping("/{groupId}/$import")
    public ResponseEntity<?> bulkImport(@RequestAttribute EhrGroup ehrGroup) throws IOException {
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
        }
        IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
        String id = getRemoteGroup(client, ehrGroup).getIdElement().getIdPart();
        IHttpResponse kickoff = bulkImportController.bulkKickOffHttpResponse(immunizationRegistry.getId(),
                id,
                Optional.empty(),
                Optional.of("Patient,Immunization"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        logger.info("Kickoff status {}", kickoff.getStatus());
        if (kickoff.getStatus() == 500) {
            throw new RuntimeException(kickoff.getResponse().toString());
        } else if (kickoff.getStatus() == 200 | kickoff.getStatus() == 202) {
            String contentLocationUrl = kickoff.getHeaders("Content-Location").get(0);
            BulkImportStatus bulkImportStatusStarted = new BulkImportStatus("Started", 0, new Date().getTime());
            resultCacheStore.put(ehrGroup.getId(), bulkImportStatusStarted);
            /**
             * Check thread async
             */
            Thread checkStatusThread = new Thread(() -> {
                logger.info("Thread started");
                int count = 0;
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                while (count++ < 30) {
                    logger.info("Thread checking bulk status for the {} time", count);
                    ResponseEntity responseEntity = bulkImportController.bulkCheckStatusHttpResponse(immunizationRegistry, contentLocationUrl);
                    if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                        BulkImportStatus bulkImportSuccess = new BulkImportStatus(new String((byte[]) Objects.requireNonNull(responseEntity.getBody()), StandardCharsets.UTF_8));
                        resultCacheStore.put(ehrGroup.getId(), bulkImportSuccess);
                        /**
                         * end
                         */
                        break;
                    } else if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                        BulkImportStatus bulkImportStatus = new BulkImportStatus("In Progress", count, new Date().getTime());
                        resultCacheStore.put(ehrGroup.getId(), bulkImportStatus);
                        /**
                         * Keep trying, wait the expected delay
                         */
                        //unchecked
                        Map<String, java.util.List<String>> headers = (Map<String, java.util.List<String>>) responseEntity.getBody();
                        {
                            assert headers != null;
                            final long delay;
                            if (headers.get("Retry-After").size() > 0 && StringUtils.isNotBlank(headers.get("Retry-After").get(0))) {
                                delay = Integer.parseInt(headers.get("Retry-After").get(0));
                            } else {
                                delay = 120;
                            }
                            try {
                                Thread.sleep(delay * 1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                logger.info("Thread Stopped");
            });
            checkStatusThread.start();
        }
        return ResponseEntity.ok(kickoff.getStatus());
    }

    @PostMapping("/{groupId}/$add")
    @Transactional()
    public EhrGroup add(@RequestAttribute EhrGroup ehrGroup, @RequestParam String patientId) {
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient Not found"));
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            Set<EhrPatient> set = ehrGroup.getPatientList();
            set.add(ehrPatient);
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            return newEntity;
        } else {
            Parameters in = new Parameters();
            /**
             * First do match to get destination reference or identifier
             */
            in.addParameter("memberId", new Identifier().setValue(ehrPatient.getMrn()).setSystem(ehrPatient.getMrnSystem()));
            in.addParameter("providerNpi", new Identifier().setSystem(FACILITY_SYSTEM).setValue(ehrGroup.getFacility().getId())); //TODO update with managingEntity
            IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
            IBaseResource remoteGroup = getRemoteGroup(client, ehrGroup);
            Parameters out = client.operation().onInstance(remoteGroup.getIdElement()).named("$member-add").withParameters(in).execute();
//            groupProviderR5.update((Group) remoteGroup, ehrGroup.getFacility(), immunizationRegistry);
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
    public EhrGroup refreshOne(@PathVariable String groupId) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        return refreshOne(ehrGroup);
    }

    public EhrGroup refreshOne(@RequestAttribute EhrGroup ehrGroup) {
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            ehrGroup.setFacility(null);
            return ehrGroup;
        } else {
            IBaseResource remoteGroup = getRemoteGroup(ehrGroup);
            if (remoteGroup != null) {
                groupProviderR5.update((Group) remoteGroup, ehrGroup.getFacility(), immunizationRegistry);
            }
            return ehrGroupRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), ehrGroup.getId()).get();
        }
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
