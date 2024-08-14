package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Group;
import org.hl7.fhir.r5.model.Parameters;
import org.immregistries.ehr.BulkImportController;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrGroupCharacteristic;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.FhirComponentsService;
import org.immregistries.ehr.fhir.ServerR5.GroupProviderR5;
import org.immregistries.ehr.logic.BundleImportService;
import org.immregistries.ehr.logic.mapping.OrganizationMapperR5;
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


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/groups"})
public class EhrGroupController {

    public static long DEFAULT_DELAY = 60;
    @Autowired
    Map<String, BulkImportStatus> resultCacheStore;

    Logger logger = LoggerFactory.getLogger(EhrGroupController.class);

    //    @Autowired
//    Map<Integer, Map<Integer, Map<String, Group>>> remoteGroupsStore;
    @Autowired()
    FhirComponentsService fhirComponentsService;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    /**
     * TODO implement for R4 too, not urgent as Organizations are extremely similar
     */
    @Autowired
    private GroupProviderR5 groupProviderR5;
    @Autowired
    private OrganizationMapperR5 organizationMapperR5;
    @Autowired
    private FhirClientController fhirClientController;
    @Autowired
    private FhirConversionController fhirConversionController;
    @Autowired
    private BulkImportController bulkImportController;
    @Autowired
    private BundleImportService bundleImportService;

    @GetMapping("/{groupId}")
    public EhrGroup get(@PathVariable() String groupId) {
        return ehrGroupRepository.findById(groupId).get();
    }

    @PostMapping()
    @Transactional()
    public ResponseEntity<String> post(@PathVariable() String facilityId,
                                       @RequestBody EhrGroup ehrGroup) {
        if (ehrGroupRepository.existsByFacilityIdAndName(facilityId, ehrGroup.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used for this facility");
        } else {
            Facility facility = facilityRepository.findById(facilityId).get();
            ehrGroup.setFacility(facility);
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            if (ehrGroup.getImmunizationRegistry() != null) {
                fhirClientController.postResource(ehrGroup.getImmunizationRegistry().getId(), "Group", fhirConversionController.groupResource(newEntity.getId()).getBody());
            }
            return new ResponseEntity<>(newEntity.getId(), HttpStatus.CREATED);
        }
    }


    @PutMapping({"", "/{groupId}"})
    @Transactional
    public ResponseEntity<EhrGroup> put(@PathVariable() String facilityId, @RequestBody EhrGroup ehrGroup) {
        EhrGroup oldEntity = ehrGroupRepository.findByFacilityIdAndId(facilityId, ehrGroup.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid ids"));
        Optional<EhrGroup> groupUsingNewName = ehrGroupRepository.findByFacilityIdAndId(facilityId, ehrGroup.getId());
        if (groupUsingNewName.isPresent() && !groupUsingNewName.get().getId().equals(oldEntity.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used by another group");
        } else {
            ehrGroup.setFacility(oldEntity.getFacility());
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            return new ResponseEntity<>(newEntity, HttpStatus.ACCEPTED);
        }
    }

    @GetMapping("/$random")
    public EhrGroup random(@PathVariable() String facilityId) {
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
    public ResponseEntity<?> getAll(@PathVariable() String facilityId, @RequestParam Optional<String> name) {
        if (name.isPresent()) {
            return ResponseEntity.ok(ehrGroupRepository.findByFacilityIdAndName(facilityId, name.get())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found")));
        } else {
            Iterable<EhrGroup> ehrGroups = ehrGroupRepository.findByFacilityId(facilityId);
            Stream<EhrGroup> stream = StreamSupport.stream(ehrGroups.spliterator(), false);
            for (Facility childFacility : facilityRepository.findByParentFacilityId(facilityId)) {
                Iterable<EhrGroup> childEhrGroups = ehrGroupRepository.findByFacilityId(childFacility.getId());
                stream = Stream.concat(stream, StreamSupport.stream(childEhrGroups.spliterator(), false));
            }
            return ResponseEntity.ok().body(stream.collect(Collectors.toList()));
        }
    }

    @GetMapping("/{groupId}/$import-status")
    public ResponseEntity<BulkImportStatus> getImportStatus(@PathVariable() String groupId) {
        return ResponseEntity.ok(resultCacheStore.get(groupId));
    }

    @GetMapping("/{groupId}/$import-status-refresh")
    public ResponseEntity<BulkImportStatus> getImportStatusWithForceRefresh(@PathVariable() String groupId) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).orElseThrow();
        BulkImportStatus bulkImportStatus = resultCacheStore.get(groupId);
        if (bulkImportStatus == null || ehrGroup.getImmunizationRegistry() == null) {
            throw new RuntimeException("No import found");
        }
        if (bulkImportStatus.getStatus().equals("done")) {
            return ResponseEntity.ok(bulkImportStatus);
        }
        remoteStatusRefresh(ehrGroup, bulkImportStatus.getCheckUrl(), bulkImportStatus.getLastAttemptCount());
        return ResponseEntity.ok(resultCacheStore.get(groupId));
    }

    @PostMapping("/{groupId}/$import-view-result")
    public ResponseEntity<Set<EhrEntity>> getImportStatusResult(@PathVariable() String groupId, @RequestBody String url) {
//        BulkImportStatus bulkImportStatus = resultCacheStore.get(groupId);
        EhrGroup group = ehrGroupRepository.findById(groupId).get();
        return bulkImportController.viewBulkResult(group.getImmunizationRegistry().getId(), group.getFacility().getId(), url);
    }

    @GetMapping("/{groupId}/$import")
    @PostMapping("/{groupId}/$import")
    public ResponseEntity<?> bulkImport(@PathVariable() String groupId) throws IOException {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
        }
        IGenericClient client = fhirComponentsService.clientFactory().newGenericClient(immunizationRegistry);
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
            BulkImportStatus bulkImportStatusStarted = BulkImportStatus.started(contentLocationUrl);
            resultCacheStore.put(ehrGroup.getId(), bulkImportStatusStarted);
            /**
             * Check thread async
             */
            Thread checkStatusThread = new Thread(null, () -> {
                logger.info("Thread started");
                int count = 0;
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                boolean stop = false;
                ResponseEntity responseEntity;
                while (count++ < 30 && !stop) {
                    logger.info("Thread checking bulk status for the {} time", count);
                    responseEntity = remoteStatusRefresh(ehrGroup, contentLocationUrl, count);
                    if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                        /**
                         * end
                         */
                        stop = true;
                    } else if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                        /**
                         * Keep trying, wait the expected delay
                         */
                        //unchecked
                        Map<String, java.util.List<String>> headers = (Map<String, java.util.List<String>>) responseEntity.getBody();
                        assert headers != null;
                        final long delay;
                        if (headers.get("Retry-After").size() > 0 && StringUtils.isNotBlank(headers.get("Retry-After").get(0))) {
                            delay = Integer.parseInt(headers.get("Retry-After").get(0));
                        } else {
                            delay = DEFAULT_DELAY;
                        }
                        try {
                            Thread.sleep(delay * 1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                logger.info("Thread Stopped");
            }, "bulk-import-group-" + ehrGroup.getId());
            checkStatusThread.start();
        }
        return ResponseEntity.ok(kickoff.getStatus());
    }

    private ResponseEntity remoteStatusRefresh(EhrGroup ehrGroup, String contentLocationUrl, Integer count) {
        logger.info("Thread checking bulk status for the {} time", count);
        ResponseEntity responseEntity = bulkImportController.bulkCheckStatusHttpResponse(ehrGroup.getImmunizationRegistry(), contentLocationUrl);

        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            BulkImportStatus bulkImportSuccess = BulkImportStatus.success(new String((byte[]) Objects.requireNonNull(responseEntity.getBody()), StandardCharsets.UTF_8));
            resultCacheStore.put(ehrGroup.getId(), bulkImportSuccess);
            /**
             * end
             */
            return responseEntity;
        } else if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
            BulkImportStatus bulkImportStatus = BulkImportStatus.inProgress(count, contentLocationUrl);
            resultCacheStore.put(ehrGroup.getId(), bulkImportStatus);
            return responseEntity;
        } else { // TODO support error
            throw new RuntimeException("ERROR " + responseEntity.getStatusCode());
        }

    }

    @PostMapping("/{groupId}/$add")
    @Transactional()
    public EhrGroup add(@PathVariable() String groupId, @RequestParam("patientId") String patientId) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
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

            EhrIdentifier ehrIdentifier = ehrPatient.getMrnEhrIdentifier();
            if (ehrIdentifier == null) {
                throw new RuntimeException("MRN required to add patient");
            }
            in.addParameter("memberId", ehrIdentifier.toR5());
            in.addParameter("providerNpi", organizationMapperR5.facilityIdentifier(ehrGroup.getFacility()));
            IGenericClient client = fhirComponentsService.clientFactory().newGenericClient(immunizationRegistry);
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
    public EhrGroup remove_member(@PathVariable() String groupId, @RequestParam String patientId) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient Not found"));
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            ehrGroup.getPatientList().remove(ehrPatient);
            ehrGroupRepository.save(ehrGroup);
            return ehrGroup;
        } else {
            Parameters in = new Parameters();
            EhrIdentifier ehrIdentifier = ehrPatient.getMrnEhrIdentifier();
            in.addParameter("memberId", ehrIdentifier.toR5());
            in.addParameter("providerNpi", organizationMapperR5.facilityIdentifier(ehrGroup.getFacility()));
            /**
             * First do match to get destination reference or identifier
             */
            IBaseResource remoteGroup = getRemoteGroup(ehrGroup);
            IGenericClient client = fhirComponentsService.clientFactory().newGenericClient(immunizationRegistry);
            Parameters out = client.operation().onInstance(remoteGroup.getIdElement()).named("$member-remove").withParameters(in).execute();
            return refreshOne(ehrGroup);
        }
    }

    @GetMapping("/{groupId}/$refresh")
    public EhrGroup refreshOne(@PathVariable() String groupId) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        return refreshOne(ehrGroup);
    }

    public EhrGroup refreshOne(EhrGroup ehrGroup) {
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
        IGenericClient client = fhirComponentsService.clientFactory().newGenericClient(immunizationRegistry);
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
