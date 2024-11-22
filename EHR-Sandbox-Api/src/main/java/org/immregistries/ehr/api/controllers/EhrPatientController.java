package org.immregistries.ehr.api.controllers;

import jakarta.transaction.Transactional;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.NextOfKinRelationshipPK;
import org.immregistries.ehr.api.repositories.AuditRevisionEntityRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.fhir.Client.MatchAndEverythingService;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.RecommendationService;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
import org.immregistries.smm.tester.manager.HL7Reader;
import org.immregistries.smm.tester.manager.query.QueryConverter;
import org.immregistries.smm.tester.manager.query.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.history.Revisions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.immregistries.ehr.api.AuditRevisionListener.COPIED_ENTITY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.COPIED_FACILITY_ID;
import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
@RequestMapping({PATIENT_PATH, "/facilities/{facilityId}/patients"})
public class EhrPatientController {
    public static final String GOLDEN_SYSTEM_TAG = "http://hapifhir.io/fhir/NamingSystem/mdm-record-status";
    public static final String GOLDEN_RECORD = "GOLDEN_RECORD";

    private static final Logger logger = LoggerFactory.getLogger(EhrPatientController.class);
    public static final String VACCINATION_NUMBER = "vaccinationNumber";
    @Autowired
    private HL7printer hl7printer;
    @Autowired
    private ImmunizationMapperR5 immunizationMapper;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    private AuditRevisionEntityRepository auditRevisionEntityRepository;
    @Autowired
    private VaccinationController vaccinationController;

    @Autowired
    private RandomGenerator randomGenerator;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private RecommendationService recommendationService;


    @GetMapping()
    public Iterable<EhrPatient> patients(@PathVariable(FACILITY_ID) Integer facilityId) {
        return ehrPatientRepository.findByFacilityId(facilityId);
    }

    @GetMapping(PATIENT_ID_SUFFIX)
    public Optional<EhrPatient> patient(@PathVariable(PATIENT_ID) Integer patientId) {
        return ehrPatientRepository.findById(patientId);
    }

    @GetMapping(PATIENT_ID_SUFFIX + "/$history")
    public List<Revision<Integer, EhrPatient>> patientHistory(@PathVariable(PATIENT_ID) Integer patientId) {
        Revisions<Integer, EhrPatient> revisions = ehrPatientRepository.findRevisions(patientId);
        return revisions.getContent();
    }

    @GetMapping(PATIENT_ID_SUFFIX + "/$populate")
    @Transactional()
    public ResponseEntity<String> populatePatient(
            @PathVariable(TENANT_ID) Integer tenantId,
            @PathVariable(FACILITY_ID) Integer facilityId,
            @PathVariable(PATIENT_ID) Integer patientId,
            @RequestParam(VACCINATION_NUMBER) Optional<Integer> vaccinationNumber) {
        return populatePatient(tenantRepository.findById(tenantId).get(), facilityRepository.findById(facilityId).get(), ehrPatientRepository.findById(patientId).get(), vaccinationNumber);
    }

    public ResponseEntity<String> populatePatient(
            Tenant tenant,
            Facility facility,
            EhrPatient patient,
            Optional<Integer> vaccinationNumber) {
        if (vaccinationNumber.isEmpty()) {
            vaccinationNumber = Optional.of(3);
        }
        if (vaccinationNumber.get() > 30) {
            vaccinationNumber = Optional.of(3);
        }
        for (int i = 0; i < vaccinationNumber.get(); i++) {
            vaccinationController.postVaccinationEvents(tenant, patient, randomGenerator.randomVaccinationEvent(patient, tenant, facility));
        }
        return ResponseEntity.ok("{}");
    }


    @PostMapping()
    @Transactional()
    public ResponseEntity<Integer> postPatient(
            @PathVariable(TENANT_ID) Integer tenantId,
            @PathVariable(FACILITY_ID) Integer facilityId,
            @RequestBody EhrPatient patient,
            @RequestParam(COPIED_ENTITY_ID) Optional<Integer> copiedEntityId,
            @RequestParam(COPIED_FACILITY_ID) Optional<Integer> copiedFacilityId,
            @RequestParam("populate") Optional<Boolean> populate) {
        /**
         * If request was issued from the copy functionality, revision table is used to find out if a copy already exists to avoid duplicates
         */
        if (copiedEntityId.isPresent()) {
            Optional<Revision<Integer, EhrPatient>> previousCopyRevision = Optional.empty();

            /**
             * We check if the patient himself is a copy from a patient in the destination facility
             */
            if (copiedFacilityId.isPresent()) {
                previousCopyRevision = ehrPatientRepository.findRevisions(copiedEntityId.get()).stream().filter(revision -> {
                    AuditRevisionEntity audit = revision.getMetadata().getDelegate();
                    if (audit.getCopiedFacilityId() != null) {
                        return audit.getCopiedFacilityId().equals(facilityId);
                    } else {
                        return false;
                    }
                }).findFirst();
            }

            /**
             * We check if a copy already exists in the destination facility
             */
            if (previousCopyRevision.isEmpty()) {
                Stream<Integer> potentialIds =
                        StreamSupport.stream(ehrPatientRepository.findByFacilityId(facilityId).spliterator(), false)
//                        facility.getPatients().stream()
                                .map(EhrPatient::getId);
                previousCopyRevision = potentialIds
                        /**
                         *  finding the creation Revision Entity ie first insert found
                         */
                        .map(((id) -> ehrPatientRepository.findRevisions(id).stream()
                                .filter(revision -> revision.getMetadata().getRevisionType().equals(RevisionMetadata.RevisionType.INSERT))
                                .findFirst().get()))
                        .filter(revision -> {
                            AuditRevisionEntity audit = revision.getMetadata().getDelegate();
                            if (audit.getCopiedEntityId() != null) {
                                return audit.getCopiedEntityId().equals(copiedEntityId.get());
                            } else {
                                return false;
                            }
                        }).findFirst();

            }

            if (previousCopyRevision.isPresent()) {
//                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Facility already bearing a copy of this patient");
                /**
                 * returning the id of the older copy or original patient
                 * TODO : Update the copy ? flavor ?
                 */
                return ResponseEntity.accepted().body(previousCopyRevision.get().getEntity().getId());
            }
        }
        return postPatient(tenantRepository.findById(tenantId).get(), facilityRepository.findById(facilityId).get(), patient, populate);
    }

    public ResponseEntity<Integer> postPatient(
            Tenant tenant,
            Facility facility,
            EhrPatient patient,
            Optional<Boolean> populate) {
        // patient data check + flavours
        patient.setId(null);
        patient.setFacility(facility);
        patient.setCreatedDate(new Date());
        patient.setUpdatedDate(new Date());
        for (NextOfKinRelationship nextOfKinRelationship : patient.getNextOfKinRelationships()) {
            nextOfKinRelationship.setNextOfKinRelationshipPK(new NextOfKinRelationshipPK());
            nextOfKinRelationship.getNextOfKin().setId(null);
        }
        if (true) {
            patient.getNames().stream().map(ehrHumanName -> {
                ehrHumanName.setNameLast(ehrHumanName.getNameLast().toUpperCase());
                ehrHumanName.setNameFirst(ehrHumanName.getNameFirst().toUpperCase());
                ehrHumanName.setNameMiddle(ehrHumanName.getNameMiddle().toUpperCase());
                return ehrHumanName;
            });
        }
        EhrPatient newEntity = ehrPatientRepository.save(patient);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        if (populate.isPresent() && populate.get()) {
            populatePatient(tenant, facility, newEntity, Optional.empty());
        }
        return ResponseEntity.created(location).body(newEntity.getId());
    }

    @PutMapping("")
    public EhrPatient putPatient(@PathVariable(FACILITY_ID) Integer facilityId,
                                 @RequestBody EhrPatient newPatient) {
        // patient data check + flavours
//        logger.info("facility {}", newPatient.getFacility().getNameDisplay());
        EhrPatient oldPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId, newPatient.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid ids"));
        newPatient.setFacility(oldPatient.getFacility());
        newPatient.setUpdatedDate(new Date());
        return ehrPatientRepository.save(newPatient);
    }

    @Autowired
    MatchAndEverythingService matchAndEverythingService;

    @GetMapping(PATIENT_ID_SUFFIX + FHIR_CLIENT + "/$fetchAndLoad")
    public ResponseEntity<Set<VaccinationEvent>> fetchAndLoadImmunizationsFromIIS(
            @PathVariable(TENANT_ID) Integer tenantId,
            @PathVariable(FACILITY_ID) Integer facilityId,
            @PathVariable(PATIENT_ID) Integer patientId,
            @RequestParam(REGISTRY_ID) Integer registryId,
            @RequestParam("_since") Optional<Long> _since) {
        return ResponseEntity.ok(matchAndEverythingService.fetchAndLoadImmunizationsFromIIS(facilityId, patientId, registryId, _since));
//
//        return ResponseEntity.badRequest().body(new HashSet<>());
    }

    @GetMapping(PATIENT_ID_SUFFIX + "/qbp")
    public ResponseEntity<String> qbp(@PathVariable(PATIENT_ID) Integer patientId) {
        QueryConverter queryConverter;
        if (ProcessingFlavor.Z44.isActive()) {
            queryConverter = QueryConverter.getQueryConverter(QueryType.QBP_Z44);
        } else {
            queryConverter = QueryConverter.getQueryConverter(QueryType.QBP_Z34);
        }
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
//        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findByPatientId(patientId);
//        Vaccine vaccine = vaccinationEvent.getVaccine()
        Facility facility = ehrPatient.getFacility();
        String vxu = hl7printer.buildVxu(null, ehrPatient, facility);
        String qbp = queryConverter.convert(vxu);
        return ResponseEntity.ok(qbp);
    }

    @PostMapping(PATIENT_ID_SUFFIX + "/qbp")
    public ResponseEntity<?> qbpSend(@RequestParam(REGISTRY_ID) Integer registryId,
                                     @PathVariable(FACILITY_ID) Integer facilityId,
                                     @PathVariable(PATIENT_ID) Integer patientId,
                                     @RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        try {
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            connector.setUserid(immunizationRegistry.getIisUsername());
            connector.setPassword(immunizationRegistry.getIisPassword());
            connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            String ack = connector.submitMessage(message, false);
            HL7Reader hl7Reader = new HL7Reader(ack);

            Map<String, Map<String, ?>> map = new HashMap<>(4);
            Map<String, Feedback> errors = new HashMap<>(4);
            Map<String, Feedback> warnings = new HashMap<>(4);
            Map<String, Feedback> notices = new HashMap<>(4);
            Map<String, Feedback> infos = new HashMap<>(4);
            Map<String, String> original = new HashMap<>(1);
            original.put("code", ack);
            map.put("ALL", original);
            map.put("E", errors);
            map.put("W", warnings);
            map.put("N", notices);
            map.put("I", infos);
            while (hl7Reader.advanceToSegment("ERR")) {
                String severity = hl7Reader.getValue(4);
                Feedback feedback = new Feedback();
                feedback.setIis(immunizationRegistry.getName());
                feedback.setFacility(facilityRepository.findById(facilityId).orElse(null));
                feedback.setPatient(ehrPatientRepository.findById(patientId).orElse(null));
                feedback.setSeverity(severity);
                feedback.setContent(hl7Reader.getOriginalSegment());
                switch (severity) {
                    case "E": {
                        errors.put("code", feedback);
                        break;
                    }
                    case "W": {
                        warnings.put("code", feedback);
                        break;
                    }
                    case "N": {
                        notices.put("code", feedback);
                        break;
                    }
                    case "I": {
                        infos.put("code", feedback);
                        break;
                    }
                }
            }

//            return ResponseEntity.ok(map);
            return ResponseEntity.ok(ack);
        } catch (Exception e1) {
            e1.printStackTrace();
            return new ResponseEntity<>("SOAP Error: " + e1.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
