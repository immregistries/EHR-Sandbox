package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.immregistries.ehr.fhir.Client.ResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FhirClientController {
    private static final String PATIENT_PREFIX = "/tenants/{tenantId}/facilities/{facilityId}/patients";
    private static final String IMMUNIZATION_PREFIX = PATIENT_PREFIX + "/{patientId}/vaccinations";
    public static final String IMM_REGISTRY_SUFFIX = "/imm-registry/{immRegistryId}";
    @Autowired
    private ImmRegistryController immRegistryController;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private ImmunizationMapperR5 immunizationHandler;
    @Autowired
    private PatientMapperR5 patientHandler;
    @Autowired
    private PatientIdentifierRepository patientIdentifierRepository;
    @Autowired
    private ImmunizationIdentifierRepository immunizationIdentifierRepository;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    @Autowired
    FhirContext fhirContext;
    @Autowired
    ResourceClient resourceClient;
    private static final Logger logger = LoggerFactory.getLogger(FhirClientController.class);
    @Autowired
    private FacilityRepository facilityRepository;

    @GetMapping("/iim-registry/{immRegistryId}/{resourceType}/{id}")
    public ResponseEntity<String> getFhirResourceFromIIS(
            @PathVariable() Integer immRegistryId,
            @PathVariable() String resourceType,
            @PathVariable() String id) {
        return ResponseEntity.ok(resourceClient.read(resourceType, id, immRegistryController.settings(immRegistryId)));
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/resource")
    public ResponseEntity<String> getPatientAsResource(
            HttpServletRequest request,
            @PathVariable() String patientId, @PathVariable() Integer facilityId) {
        EhrPatient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No facility found"));

        Patient fhirPatient = patientHandler.toFhirPatient(patient,
                resourceIdentificationService.getFacilityPatientIdentifierSystem(facility));
        fhirPatient.setText(null);
        String resource = parser.encodeResourceToString(fhirPatient);
        return ResponseEntity.ok(resource);
    }

    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postPatient(@PathVariable() Integer immRegistryId, @PathVariable() String patientId, @RequestBody String message) {
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        MethodOutcome outcome = resourceClient.create(patient, ir);
        /**
         * Registering received id as external id
         */
        patientIdentifierRepository.save(new PatientIdentifier(patientId,immRegistryId,outcome.getId().getIdPart()));
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> updatePatient(@PathVariable() Integer immRegistryId, @PathVariable() String patientId, @RequestBody String message) {
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(patient, "Patient", patient.getIdentifierFirstRep(), ir);
        /**
         * Registering received id as external id
         */
        patientIdentifierRepository.save(new PatientIdentifier(patientId,immRegistryId,outcome.getId().getIdPart()));
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> getPatient(@PathVariable() Integer immRegistryId, @PathVariable() String patientId) {
        return ResponseEntity.ok(resourceClient.read("patient", String.valueOf(patientId), immRegistryController.settings(immRegistryId)));
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/resource")
    public ResponseEntity<String> immunizationResource(
            HttpServletRequest request,
            @PathVariable() String vaccinationId,
            @PathVariable() Integer facilityId) {
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No facility found"));
        Immunization immunization =
                immunizationHandler.toFhirImmunization(vaccinationEvent,
                        resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility), resourceIdentificationService.getFacilityPatientIdentifierSystem(facility));
        String resource = parser.encodeResourceToString(immunization);
        return ResponseEntity.ok(resource);
    }

    @PostMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postImmunization(@PathVariable() Integer immunizationRegistryId, @PathVariable() String vaccinationId, @RequestBody String message, @RequestParam(required = false) String patientFhirId) {
        IParser parser = parser(message);
        Immunization immunization = parser.parseResource(Immunization.class, message);

        if (patientFhirId != null && !patientFhirId.isEmpty()) {
            immunization.setPatient(new Reference().setReference("Patient/" + new IdType(patientFhirId).getIdPart()));
        }
        MethodOutcome outcome;
        try {
            ImmunizationRegistry immunizationRegistry = immRegistryController.settings(immunizationRegistryId);
            outcome = resourceClient.create(immunization, immunizationRegistry);
            /**
             * Registering received id as external id
             */
            immunizationIdentifierRepository.save(new ImmunizationIdentifier(
                    vaccinationId, immunizationRegistry.getId(),outcome.getId().getIdPart()));
            return ResponseEntity.ok(outcome.getId().getIdPart());

        } catch (Exception e) {
            throw e;
//            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> updateImmunization(@PathVariable() Integer immRegistryId, @PathVariable() String vaccinationId, @RequestBody String message, @RequestParam(required = false) String patientFhirId) {
        IParser parser = parser(message);
        Immunization immunization = parser.parseResource(Immunization.class, message);
        ImmunizationRegistry immunizationRegistry = immRegistryController.settings(immRegistryId);
        if (patientFhirId != null && !patientFhirId.isEmpty()) {
            if (patientFhirId.startsWith("Patient/")) {
                immunization.setPatient(new Reference().setReference(patientFhirId));
            } else {
                immunization.setPatient(new Reference().setReference("Patient/" + patientFhirId));
            }
        }
        MethodOutcome outcome;
        try {
            outcome = resourceClient.updateOrCreate(immunization, "Immunization", immunization.getIdentifierFirstRep(), immunizationRegistry);
            /**
             * Registering received id as external id
             */
            immunizationIdentifierRepository.save(new ImmunizationIdentifier(
                    vaccinationId, immunizationRegistry.getId(),outcome.getId().getIdPart()));
            return ResponseEntity.ok(outcome.getId().getIdPart());

        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> getImmunization(@PathVariable() Integer immRegistryId, @PathVariable() int vaccinationId) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        return ResponseEntity.ok(resourceClient.read("immunization", String.valueOf(vaccinationId), ir));
    }

    @PostMapping("/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postResource(@PathVariable() Integer immRegistryId, @RequestParam(name = "type") String type, @RequestBody String message) {
        IParser parser = parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        MethodOutcome outcome = resourceClient.create(resource, ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping("/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> putResource(@PathVariable() Integer immRegistryId, @RequestParam String type, @RequestBody String message) {
        IParser parser = parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(resource, type, null, ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    private IParser parser(String message) {
        IParser parser;
        if (message.startsWith("<")) {
            parser = fhirContext.newXmlParser().setPrettyPrint(true);
        } else {
            parser = fhirContext.newJsonParser().setPrettyPrint(true);
        }
        return parser;
    }

}
