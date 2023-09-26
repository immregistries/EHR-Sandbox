package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.ServerR5.ImmunizationProviderR5;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
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
import org.springframework.util.StreamUtils;
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
import static org.immregistries.ehr.logic.mapping.PatientMapperR5.MRN_SYSTEM;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients", "/facilities/{facilityId}/patients"})
public class EhrPatientController {

    private static final Logger logger = LoggerFactory.getLogger(EhrPatientController.class);
    @Autowired
    FhirContext fhirContext;
    @Autowired
    RandomGenerator randomGenerator;
    @Autowired
    FhirClientController fhirClientController;
    @Autowired
    HL7printer hl7printer;
    @Autowired
    ImmunizationRegistryController immRegistryController;
    @Autowired
    ImmunizationMapperR5 immunizationMapper;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private CustomClientFactory customClientFactory;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private ImmunizationProviderR5 immunizationProviderR5;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private AuditRevisionEntityRepository auditRevisionEntityRepository;


    @GetMapping()
    public Iterable<EhrPatient> patients(@PathVariable() int facilityId) {
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


    @PostMapping()
//    @Transactional()
    public ResponseEntity<String> postPatient(
            @RequestAttribute Facility facility,
            @RequestBody EhrPatient patient,
            @RequestParam(COPIED_ENTITY_ID) Optional<String> copiedEntityId,
            @RequestParam(COPIED_FACILITY_ID) Optional<Integer> copiedFacilityId) {

        // patient data check + flavours
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
                        return audit.getCopiedFacilityId().equals(facility.getId());
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
                        StreamSupport.stream(ehrPatientRepository.findByFacilityId(facility.getId()).spliterator(),false)
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
        patient.setId(null);
        patient.setFacility(facility);
        patient.setCreatedDate(new Date());
        patient.setUpdatedDate(new Date());
        EhrPatient newEntity = ehrPatientRepository.save(patient);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        return ResponseEntity.created(location).body(newEntity.getId());
    }

    @PutMapping("")
    public EhrPatient putPatient(@RequestAttribute Tenant tenant,
                                 @RequestAttribute Facility facility,
//                                 @RequestAttribute EhrPatient patient,
                                 @RequestBody EhrPatient newPatient) {
        // patient data check + flavours
        EhrPatient oldPatient = ehrPatientRepository.findByFacilityIdAndId(facility.getId(), newPatient.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid ids"));
        newPatient.setFacility(facility);
        newPatient.setUpdatedDate(new Date());
        return ehrPatientRepository.save(newPatient);
    }

    @GetMapping("/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$fetchAndLoad")
    public ResponseEntity<Set<VaccinationEvent>> fetchAndLoadImmunizationsFromIIS(@RequestAttribute Facility facility,
                                                                                  @RequestAttribute EhrPatient patient,
                                                                                  @PathVariable() Integer registryId,
                                                                                  @RequestParam Optional<Long> _since) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(registryId);
        IGenericClient client = customClientFactory.newGenericClient(registryId);

        // TODO switch to $match ?
        Bundle searchBundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode(MRN_SYSTEM, patient.getMrn()))
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
            requestDetails.setTenantId(String.valueOf(facility.getId()));

            for (Bundle.BundleEntryComponent entry : outBundle.getEntry()) {
                try {
                    Immunization immunization = (Immunization) entry.getResource();
                    immunization.setPatient(new Reference().setReference(patient.getId()));
                    VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent((Immunization) entry.getResource());
                    vaccinationEvent.setPatient(patient);
                    vaccinationEvent.setAdministeringFacility(facility);
                    set.add(immunizationMapper.toVaccinationEvent(immunization));
                } catch (ClassCastException classCastException) {
                    //Ignoring other resources
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
        ImmunizationRegistry immunizationRegistry = immRegistryController.settings(registryId);
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
