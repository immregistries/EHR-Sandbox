package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import ca.uhn.fhir.rest.gclient.IOperation;
import ca.uhn.fhir.rest.gclient.IOperationUnnamed;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientBuilder;
import org.immregistries.ehr.fhir.Client.ResourceClient;
import org.immregistries.ehr.logic.BundleImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
public class FhirClientController {
    public static final String PATIENT_PREFIX = "/tenants/{tenantId}/facilities/{facilityId}/patients";
    public static final String IMMUNIZATION_PREFIX = PATIENT_PREFIX + "/{patientId}/vaccinations";
    public static final String IMM_REGISTRY_SUFFIX = "/imm-registry/{immRegistryId}";
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private PatientIdentifierRepository patientIdentifierRepository;
    @Autowired
    private ImmunizationIdentifierRepository immunizationIdentifierRepository;



    @Autowired
    FhirContext fhirContext;
    @Autowired
    ResourceClient resourceClient;
    @Autowired
    CustomClientBuilder customClientBuilder;

    private static final Logger logger = LoggerFactory.getLogger(FhirClientController.class);
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;


    @GetMapping("/iim-registry/{immRegistryId}/{resourceType}/{id}")
    public ResponseEntity<String> getFhirResourceFromIIS(
            @PathVariable() Integer immRegistryId,
            @PathVariable() String resourceType,
            @PathVariable() String id) {
        return ResponseEntity.ok(resourceClient.read(resourceType, id, immunizationRegistryController.settings(immRegistryId)));
    }

    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postPatient(
            @PathVariable() Integer immRegistryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        ImmunizationRegistry ir = immunizationRegistryController.settings(immRegistryId);
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
    public ResponseEntity<String> updatePatient(
            @PathVariable() Integer immRegistryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(immRegistryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(patient,
                "Patient",
                patient.getIdentifierFirstRep(),
                immunizationRegistry);
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
    public ResponseEntity<String> getPatient(
            @PathVariable() Integer immRegistryId,
            @PathVariable() String patientId) {
        return ResponseEntity.ok(resourceClient.read("patient", String.valueOf(patientId),
                immunizationRegistryController.settings(immRegistryId)));
    }

    @PostMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postImmunization(
            @PathVariable() Integer immunizationRegistryId,
            @PathVariable() String vaccinationId,
            @RequestBody String message,
            @RequestParam(required = false) String patientFhirId) {
        IParser parser = parser(message);
        Immunization immunization = parser.parseResource(Immunization.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(immunizationRegistryId);
        if (patientFhirId != null && !patientFhirId.isEmpty()) {
            immunization.setPatient(new Reference().setReference("Patient/" + new IdType(patientFhirId).getIdPart()));
        }
        MethodOutcome outcome = resourceClient.create(immunization, immunizationRegistry);
        /**
         * Registering received id as external id
         */
        immunizationIdentifierRepository.save(new ImmunizationIdentifier(
                vaccinationId, immunizationRegistry.getId(),outcome.getId().getIdPart()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> updateImmunization(
            @PathVariable() Integer facilityId,
            @PathVariable() String patientId,
            @PathVariable() Integer immRegistryId,
            @PathVariable() String vaccinationId,
            @RequestBody String message,
            @RequestParam(required = false) String patientFhirId) {
        IParser parser = parser(message);
        Immunization immunization = parser.parseResource(Immunization.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(immRegistryId);
        if (patientFhirId != null && !patientFhirId.isEmpty()) {
            immunization.setPatient(new Reference().setReference("Patient/" + new IdType(patientFhirId).getIdPart()));
        }
        try {
            MethodOutcome outcome = resourceClient.updateOrCreate(immunization,
                    "Immunization",
                    immunization.getIdentifierFirstRep(),
                    immunizationRegistry);
            /**
             * Registering received id as external id
             */
            immunizationIdentifierRepository.save(new ImmunizationIdentifier(
                    vaccinationId, immunizationRegistry.getId(),outcome.getId().getIdPart()));
            return ResponseEntity.ok(outcome.getId().getIdPart());

        } catch (BaseServerResponseException baseServerResponseException) {
            Feedback feedback = new Feedback();
            feedback.setFacility(facilityRepository.findById(facilityId).orElseThrow());
            feedback.setPatient(ehrPatientRepository.findByFacilityIdAndId(facilityId,patientId).orElseThrow());
            feedback.setVaccinationEvent(vaccinationEventRepository.findByPatientIdAndId(patientId,vaccinationId).orElseThrow());
            feedback.setCode("invalid");
            feedback.setSeverity("fatal");
            feedback.setTimestamp(new Timestamp(new Date().getTime()));
            feedback.setContent(baseServerResponseException.getMessage());
            feedbackRepository.save(feedback);
            throw baseServerResponseException;
        }
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> getImmunization(
            @PathVariable() Integer immRegistryId,
            @PathVariable() int vaccinationId) {
        ImmunizationRegistry ir = immunizationRegistryController.settings(immRegistryId);
        return ResponseEntity.ok(resourceClient.read("immunization", String.valueOf(vaccinationId), ir));
    }

    @PostMapping(IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postResource(
            @PathVariable() Integer immRegistryId,
            @RequestParam(name = "type") String type,
            @RequestBody String message) {
        IParser parser = parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry ir = immunizationRegistryController.settings(immRegistryId);
        MethodOutcome outcome = resourceClient.create(resource, ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping(IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> putResource(
            @PathVariable() Integer immRegistryId,
            @RequestParam String type,
            @RequestBody String message) {
        IParser parser = parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry ir = immunizationRegistryController.settings(immRegistryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(resource, type, null, ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PostMapping({
            IMM_REGISTRY_SUFFIX + "/operation/{target}/{operationType}",
            IMM_REGISTRY_SUFFIX + "/operation/{target}/{targetId}/{operationType}",
    })
    @PutMapping({
            IMM_REGISTRY_SUFFIX + "/operation/{target}/{operationType}",
            IMM_REGISTRY_SUFFIX + "/operation/{target}/{targetId}/{operationType}",
    })
    public ResponseEntity<Object> operation(
            @PathVariable() String operationType,
            @PathVariable() Integer immRegistryId,
            @PathVariable() String target,
            @PathVariable() Optional<String> targetId,
            @RequestBody String parametersString,
            @RequestParam Map<String,String> allParams) {
        Parameters parameters = new Parameters();
        /**
         * parsing parameters, removing ? at the beginning
         */
        parametersString.replaceFirst("\\?","");
        for (Map.Entry<String,String> entry: allParams.entrySet()) {
//            logger.info("Params {} {}",entry.getKey(),entry.getValue());
            parameters.addParameter(entry.getKey(),entry.getValue());
        }
        operationType = operationType.replaceFirst("\\$","");

        IGenericClient client = customClientBuilder.newGenericClient(immunizationRegistryController.settings(immRegistryId));
        /**
         * Allows access to last response received by client
         */
        CapturingInterceptor capturingInterceptor = new CapturingInterceptor();
        client.registerInterceptor(capturingInterceptor);
        Parameters out;
        IOperation iOperation = client.operation();
        IOperationUnnamed iOperationUnnamed;
        if (targetId.isPresent()) {
            iOperationUnnamed = iOperation.onInstance(target + "/" + targetId.get());
        } else {
            iOperationUnnamed = iOperation.onType(target);
        }
        out = iOperationUnnamed.named(operationType)
                .withParameters(parameters)
                .prettyPrint().useHttpGet()
                .execute();

        IHttpResponse response =  capturingInterceptor.getLastResponse();
        try {
            Bundle bundle = fhirContext.newJsonParser().setPrettyPrint(true)
                    .parseResource(Bundle.class, response.readEntity());
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                logger.info("entry {}", entry.getResource());
            }
            return ResponseEntity.ok(parser("").encodeResourceToString(bundle));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Provides the accurate parser for said message
     * @param message
     * @return
     */
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
