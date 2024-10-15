package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.BulkImportController;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrGroupCharacteristic;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.Client.ResourceClient;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.fhir.Server.IGroupProvider;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.IGroupMapper;
import org.immregistries.ehr.logic.mapping.IOrganizationMapper;
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

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;


@RestController
@RequestMapping({GROUP_PATH})
public class EhrGroupController {

    public static long DEFAULT_DELAY = 60;
    @Autowired
    Map<Integer, BulkImportStatus> resultCacheStore;

    Logger logger = LoggerFactory.getLogger(EhrGroupController.class);

    @Autowired()
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private FacilityRepository facilityRepository;


    @Autowired
    private FhirClientController fhirClientController;
    @Autowired
    private FhirConversionController fhirConversionController;
    @Autowired
    private BulkImportController bulkImportController;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    @Autowired
    private ResourceClient resourceClient;

    @GetMapping(GROUP_ID_SUFFIX)
    public EhrGroup get(@PathVariable(GROUP_ID) Integer groupId) {
        return ehrGroupRepository.findById(groupId).get();
    }

    @PostMapping()
    @Transactional()
    public ResponseEntity<Integer> post(@PathVariable(FACILITY_ID) Integer facilityId,
                                        @RequestBody EhrGroup ehrGroup) {
        if (ehrGroupRepository.existsByFacilityIdAndName(facilityId, ehrGroup.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used for this facility");
        } else {
            Facility facility = facilityRepository.findById(facilityId).get();
            ehrGroup.setFacility(facility);
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            if (ehrGroup.getImmunizationRegistry() != null) {
                MethodOutcome methodOutcome = resourceClient.create(fhirComponentsDispatcher.groupMapper().toFhir(ehrGroup), ehrGroup.getImmunizationRegistry());
//                fhirClientController.postResource(ehrGroup.getImmunizationRegistry().getId(), "Group", fhirConversionController.groupResource(newEntity.getId()).getBody());
            }
            return new ResponseEntity<>(newEntity.getId(), HttpStatus.CREATED);
        }
    }


    @PutMapping({"", GROUP_ID_SUFFIX})
    @Transactional
    public ResponseEntity<EhrGroup> put(@PathVariable(FACILITY_ID) Integer facilityId, @RequestBody EhrGroup ehrGroup) {
        EhrGroup oldEntity = ehrGroupRepository.findByFacilityIdAndId(facilityId, ehrGroup.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid ids"));
        Optional<EhrGroup> groupUsingNewName = ehrGroupRepository.findByFacilityIdAndId(facilityId, ehrGroup.getId());
        if (groupUsingNewName.isPresent() && !groupUsingNewName.get().getId().equals(oldEntity.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used by another group");
        } else {
            ehrGroup.setFacility(oldEntity.getFacility());
            ehrGroup.setId(oldEntity.getId());
            /**
             * Set read only Json fields
             */
            ehrGroup.setPatientList(oldEntity.getPatientList());
            if (ehrGroup.getImmunizationRegistry() == null) {
                EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
                return new ResponseEntity<>(newEntity, HttpStatus.ACCEPTED);
            } else {
                IGroupMapper groupMapper = fhirComponentsDispatcher.groupMapper();
                IBaseResource group = groupMapper.toFhir(ehrGroup);
                MethodOutcome methodOutcome = resourceClient.updateOrCreate(group, "Group", groupMapper.extractGroupIdentifier(group), ehrGroup.getImmunizationRegistry());
                if (methodOutcome.getCreated() != null) {
                    return new ResponseEntity<>(refreshOne(oldEntity.getId()), HttpStatus.ACCEPTED);
                } else if (methodOutcome.getResource() != null) {
                    ((IGroupProvider) fhirComponentsDispatcher.provider("Group")).update(methodOutcome.getResource(), ehrGroup.getFacility(), ehrGroup.getImmunizationRegistry());
                    return new ResponseEntity<>(ehrGroupRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), ehrGroup.getId()).get(), HttpStatus.ACCEPTED);
                } else {
                    throw new RuntimeException("Error parsing fhir Update result");
                }
            }
        }
    }

    @GetMapping("/$random")
    public EhrGroup random(@PathVariable(FACILITY_ID) Integer facilityId) {
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

        EhrIdentifier ehrIdentifier = new EhrIdentifier();
        ehrIdentifier.setValue(RandomStringUtils.random(15, true, true));
        ehrIdentifier.setSystem(resourceIdentificationService.getFacilityGroupIdentifierSystem(facilityRepository.findById(facilityId).orElseThrow()));
        ehrGroup.getIdentifiers().add(ehrIdentifier);

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
    public ResponseEntity<?> getAll(@PathVariable(FACILITY_ID) Integer facilityId, @RequestParam("name") Optional<String> name) {
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

    @GetMapping(GROUP_ID_SUFFIX + "/$import-status")
    public ResponseEntity<BulkImportStatus> getImportStatus(@PathVariable(GROUP_ID) Integer groupId) {
        return ResponseEntity.ok(resultCacheStore.get(groupId));
    }

    @GetMapping(GROUP_ID_SUFFIX + "/$import-status-refresh")
    public ResponseEntity<BulkImportStatus> getImportStatusWithForceRefresh(@PathVariable(GROUP_ID) Integer groupId) {
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

    @PostMapping(GROUP_ID_SUFFIX + "/$import-view-result")
    public ResponseEntity<Set<EhrEntity>> getImportStatusResult(@PathVariable(GROUP_ID) Integer groupId, @RequestBody String url) {
//        BulkImportStatus bulkImportStatus = resultCacheStore.get(groupId);
        EhrGroup group = ehrGroupRepository.findById(groupId).get();
        return bulkImportController.viewBulkResult(group.getImmunizationRegistry().getId(), group.getFacility().getId(), url);
    }

    @GetMapping(GROUP_ID_SUFFIX + "/$import")
    @PostMapping(GROUP_ID_SUFFIX + "/$import")
    public ResponseEntity<?> bulkImport(@PathVariable(GROUP_ID) Integer groupId) throws IOException {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
        }
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistry);
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

    @PostMapping(GROUP_ID_SUFFIX + "/$add")
    @Transactional()
    public EhrGroup add(@PathVariable(GROUP_ID) Integer groupId, @RequestParam("patientId") Integer patientId) {
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
            /**
             * First do match to get destination reference or identifier
             */
            IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistry);
            IBaseResource remoteGroup = getRemoteGroup(client, ehrGroup);
            EhrIdentifier ehrIdentifier = ehrPatient.getMrnEhrIdentifier();
            if (ehrIdentifier == null) {
                throw new RuntimeException("MRN required to add patient");
            }
            IBaseParameters in;
            if (ProcessingFlavor.R4.isActive()) {
                in = new org.hl7.fhir.r4.model.Parameters()
                        .addParameter("memberId", ehrIdentifier.toR4())
                        .addParameter("providerNpi", IOrganizationMapper.facilityIdToEhrIdentifier(ehrGroup.getFacility()).toR4());
            } else {
                in = new org.hl7.fhir.r5.model.Parameters()
                        .addParameter("memberId", ehrIdentifier.toR5())
                        .addParameter("providerNpi", IOrganizationMapper.facilityIdToEhrIdentifier(ehrGroup.getFacility()).toR5());
            }
            IBaseParameters out = client.operation().onInstance(remoteGroup.getIdElement()).named("$member-add").withParameters(in).execute();
            /**
             * update after result
             */
            return refreshOne(ehrGroup);
        }
    }

    @PostMapping(GROUP_ID_SUFFIX + "/$remove")
    public EhrGroup remove_member(@PathVariable(GROUP_ID) Integer groupId, @RequestParam("patientId") Integer patientId) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
        EhrPatient ehrPatient = ehrPatientRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient Not found"));
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            ehrGroup.getPatientList().remove(ehrPatient);
            ehrGroupRepository.save(ehrGroup);
            return ehrGroup;
        } else {
            EhrIdentifier ehrIdentifier = ehrPatient.getMrnEhrIdentifier();
            IBaseParameters in;
            if (ProcessingFlavor.R4.isActive()) {
                in = new org.hl7.fhir.r4.model.Parameters()
                        .addParameter("memberId", ehrIdentifier.toR4())
                        .addParameter("providerNpi", IOrganizationMapper.facilityIdToEhrIdentifier(ehrGroup.getFacility()).toR4());
            } else {
                in = new org.hl7.fhir.r5.model.Parameters()
                        .addParameter("memberId", ehrIdentifier.toR5())
                        .addParameter("providerNpi", IOrganizationMapper.facilityIdToEhrIdentifier(ehrGroup.getFacility()).toR5());
            }
            /**
             * First do match to get destination reference or identifier
             */
            IBaseResource remoteGroup = getRemoteGroup(ehrGroup);
            IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistry);
            IBaseParameters out = client.operation().onInstance(remoteGroup.getIdElement()).named("$member-remove").withParameters(in).execute();
            return refreshOne(ehrGroup);
        }
    }

    @GetMapping(GROUP_ID_SUFFIX + "/$refresh")
    public EhrGroup refreshOne(@PathVariable(GROUP_ID) Integer groupId) {
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
                ((IGroupProvider) fhirComponentsDispatcher.provider("Group")).update(remoteGroup, ehrGroup.getFacility(), immunizationRegistry);
            }
            return ehrGroupRepository.findByFacilityIdAndId(ehrGroup.getFacility().getId(), ehrGroup.getId()).get();
        }
    }

    private IBaseResource getRemoteGroup(EhrGroup ehrGroup) {
        ImmunizationRegistry immunizationRegistry = ehrGroup.getImmunizationRegistry();
        if (immunizationRegistry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
        }
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistry);
        return getRemoteGroup(client, ehrGroup);
    }

    private IBaseResource getRemoteGroup(IGenericClient client, EhrGroup ehrGroup) {
        return ((IGroupProvider) fhirComponentsDispatcher.provider("Group")).getRemoteGroup(client, ehrGroup);
    }


}
