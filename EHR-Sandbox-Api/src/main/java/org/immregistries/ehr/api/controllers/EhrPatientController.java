package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.NextOfKinRelationshipPK;
import org.immregistries.ehr.api.repositories.AuditRevisionEntityRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.mapping.IImmunizationMapper;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
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

import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.immregistries.ehr.api.AuditRevisionListener.COPIED_ENTITY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.COPIED_FACILITY_ID;
import static org.immregistries.ehr.api.controllers.FhirClientController.IMM_REGISTRY_SUFFIX;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients", "/facilities/{facilityId}/patients"})
public class EhrPatientController {
    public static final String GOLDEN_SYSTEM_TAG = "http://hapifhir.io/fhir/NamingSystem/mdm-record-status";
    public static final String GOLDEN_RECORD = "GOLDEN_RECORD";

    private static final Logger logger = LoggerFactory.getLogger(EhrPatientController.class);
    @Autowired
    HL7printer hl7printer;
    @Autowired
    ImmunizationRegistryController immRegistryController;
    @Autowired
    IImmunizationMapper immunizationMapper;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private CustomClientFactory customClientFactory;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
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


    @GetMapping()
    public Iterable<EhrPatient> patients(@PathVariable() String facilityId) {
        return ehrPatientRepository.findByFacilityId(facilityId);
    }

    @GetMapping("/{patientId}")
    public Optional<EhrPatient> patient(@PathVariable() String patientId) {
        return ehrPatientRepository.findById(patientId);
    }

    @GetMapping("/{patientId}/$history")
    public List<Revision<Integer, EhrPatient>> patientHistory(@PathVariable() String patientId) {
        Revisions<Integer, EhrPatient> revisions = ehrPatientRepository.findRevisions(patientId);
        return revisions.getContent();
    }

    @GetMapping("/{patientId}/$populate")
    @Transactional()
    public ResponseEntity<String> populatePatient(
            @PathVariable() String tenantId,
            @PathVariable() String facilityId,
            @PathVariable() String patientId,
            @RequestParam Optional<Integer> vaccinationNumber) {
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
    public ResponseEntity<String> postPatient(
            @PathVariable() String tenantId,
            @PathVariable() String facilityId,
            @RequestBody EhrPatient patient,
            @RequestParam(COPIED_ENTITY_ID) Optional<String> copiedEntityId,
            @RequestParam(COPIED_FACILITY_ID) Optional<Integer> copiedFacilityId,
            @RequestParam Optional<Boolean> populate) {
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
                Stream<String> potentialIds =
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

    public ResponseEntity<String> postPatient(
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
    public EhrPatient putPatient(@PathVariable() String facilityId,
                                 @RequestBody EhrPatient newPatient) {
        // patient data check + flavours
//        logger.info("facility {}", newPatient.getFacility().getNameDisplay());
        EhrPatient oldPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId, newPatient.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid ids"));
        newPatient.setFacility(oldPatient.getFacility());
        newPatient.setUpdatedDate(new Date());
        return ehrPatientRepository.save(newPatient);
    }

    @GetMapping("/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$fetchAndLoad")
    public ResponseEntity<Set<VaccinationEvent>> fetchAndLoadImmunizationsFromIIS(@PathVariable() String facilityId,
                                                                                  @PathVariable() String patientId,
                                                                                  @PathVariable() Integer registryId,
                                                                                  @RequestParam Optional<Long> _since) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.getImmunizationRegistry(registryId);
        IGenericClient client = customClientFactory.newGenericClient(immunizationRegistry);
        Facility facility = facilityRepository.findById(facilityId).get();
        EhrPatient patient = ehrPatientRepository.findById(patientId).get();

        // TODO switch to $match ?
        EhrIdentifier ehrIdentifier = patient.getMrnEhrIdentifier();
        Bundle searchBundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode(ehrIdentifier.getSystem(), ehrIdentifier.getValue()))
                .returnBundle(Bundle.class).execute();
        String id = null;
        for (Bundle.BundleEntryComponent entry : searchBundle.getEntry()) {
            if (entry.getResource().getMeta().hasTag()) { // TODO better condition to check if golden record
                id = new IdType(entry.getResource().getId()).getIdPart();
                break;
            }
        }
        if (id != null) {
            Parameters in = new Parameters()
                    .addParameter("_mdm", "true")
                    .addParameter("_type", "Immunization,ImmunizationRecommendation");
            if (_since.isPresent()) {
                in.addParameter("_since", String.valueOf(_since.get()));
            }
            Bundle outBundle = client.operation()
                    .onInstance("Patient/" + id)
                    .named("$everything")
                    .withParameters(in)
                    .prettyPrint()
                    .useHttpGet()
                    .returnResourceType(Bundle.class).execute();
            Set<VaccinationEvent> set = new HashSet<>(outBundle.getEntry().size());

            RequestDetails requestDetails = new ServletRequestDetails();
            requestDetails.setTenantId(facilityId);

            for (Bundle.BundleEntryComponent entry : outBundle.getEntry()) {
                if (entry.getResource().getResourceType().name().equals("Immunization")) {
                    Immunization immunization = (Immunization) entry.getResource();
                    if (immunization.getMeta().getTag(GOLDEN_SYSTEM_TAG, GOLDEN_RECORD) != null) {
                        immunization.setPatient(new Reference().setReference(patient.getId()));
                        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent((Immunization) entry.getResource());
                        vaccinationEvent.setPatient(patient);
                        vaccinationEvent.setAdministeringFacility(facility);
                        set.add(immunizationMapper.toVaccinationEvent(immunization));
                    }
                }

                //TODO Recommendations
            }
            return ResponseEntity.accepted().body(set);
        }
        return ResponseEntity.badRequest().body(new HashSet<>());
    }

    @GetMapping("/{patientId}/qbp")
    public ResponseEntity<String> qbp(@PathVariable() String patientId) {
        QueryConverter queryConverter = QueryConverter.getQueryConverter(QueryType.QBP_Z34);
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
//        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findByPatientId(patientId);
//        Vaccine vaccine = vaccinationEvent.getVaccine()
        Facility facility = ehrPatient.getFacility();
        String vxu = hl7printer.buildVxu(null, ehrPatient, facility);
        return ResponseEntity.ok(queryConverter.convert(vxu));
    }

    @PostMapping("/{patientId}/qbp" + FhirClientController.IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> qbpSend(@PathVariable() Integer registryId, @RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immRegistryController.getImmunizationRegistry(registryId);
        try {
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            connector.setUserid(immunizationRegistry.getIisUsername());
            connector.setPassword(immunizationRegistry.getIisPassword());
            connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            return ResponseEntity.ok(connector.submitMessage(message, false));
        } catch (Exception e1) {
            e1.printStackTrace();
            return new ResponseEntity<>("SOAP Error: " + e1.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
