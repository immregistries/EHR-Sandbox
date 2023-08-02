package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.gclient.IOperation;
import ca.uhn.fhir.rest.gclient.IOperationUnnamed;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.Client.ResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class FhirClientController {
    private static final Logger logger = LoggerFactory.getLogger(FhirClientController.class);
    public static final String PATIENT_PREFIX = "/tenants/{tenantId}/facilities/{facilityId}/patients";
    public static final String IMMUNIZATION_PREFIX = PATIENT_PREFIX + "/{patientId}/vaccinations";
    public static final String IMM_REGISTRY_SUFFIX = "/imm-registry/{registryId}";
    @Autowired
    private FhirContext fhirContext;
    @Autowired
    private ResourceClient resourceClient;
    @Autowired
    private CustomClientFactory customClientFactory;
    @Autowired
    private FhirConversionController fhirConversionController;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private PatientIdentifierRepository patientIdentifierRepository;
    @Autowired
    private ImmunizationIdentifierRepository immunizationIdentifierRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;

    @GetMapping(IMM_REGISTRY_SUFFIX + "/{resourceType}/{id}")
    public ResponseEntity<String> getFhirResourceFromIIS(
            @PathVariable() Integer registryId,
            @PathVariable() String resourceType,
            @PathVariable() String id) {
        return ResponseEntity.ok(resourceClient.read(resourceType, id, immunizationRegistryController.settings(registryId)));
    }

    @PostMapping(IMM_REGISTRY_SUFFIX + "/{resourceType}/search")
    public ResponseEntity<String> searchFhirResourceFromIIS(
            @PathVariable() Integer registryId,
            @PathVariable() String resourceType,
            @RequestBody Identifier identifier) {
        Bundle bundle = customClientFactory.newGenericClient(registryId).search()
                .forResource(resourceType)
                .where(Patient.IDENTIFIER.exactly().identifier(identifier.getValue()))
                .returnBundle(Bundle.class).execute();
        return ResponseEntity.ok(parser("").encodeResourceToString(bundle));
    }

    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postPatient(
            @PathVariable() Integer registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        ImmunizationRegistry ir = immunizationRegistryController.settings(registryId);
        MethodOutcome outcome = resourceClient.create(patient, ir);
        /**
         * Registering received id as external id
         */
        patientIdentifierRepository.save(new PatientIdentifier(patientId,registryId,outcome.getId().getIdPart()));
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }
    
    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$match")
    public ResponseEntity<List<String>> matchPatient(
            @PathVariable() Integer facilityId,
            @PathVariable() Integer registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        Bundle bundle = matchPatientOperation(facilityId,registryId,patientId,message);

        return ResponseEntity.ok(
                bundle.getEntry().stream().filter(Bundle.BundleEntryComponent::hasResource).map(bundleEntryComponent -> bundleEntryComponent.getResource().getIdPart()).collect(Collectors.toList()
                )
        );
    }

    public Bundle matchPatientOperation(
            Integer facilityId,
            Integer registryId,
            String patientId,
            String message) {
        if(message == null) {
            message = fhirConversionController.getPatientAsResource(patientId,facilityId).getBody();
        }
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        Parameters parameters = new Parameters();
        return customClientFactory.newGenericClient(registryId).operation().onType("Patient").named("match").withParameters(parameters).andParameter("resource",patient).returnResourceType(Bundle.class).execute();
    }



    @PutMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> updatePatient(
            @PathVariable() Integer registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(registryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(patient,
                "Patient",
                patient.getIdentifierFirstRep(),
                immunizationRegistry);
        /**
         * Registering received id as external id
         */
        patientIdentifierRepository.save(new PatientIdentifier(patientId,registryId,outcome.getId().getIdPart()));
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> getPatient(
            @PathVariable() Integer registryId,
            @PathVariable() String patientId) {
        return ResponseEntity.ok(resourceClient.read("patient", String.valueOf(patientId),
                immunizationRegistryController.settings(registryId)));
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
    @Transactional
    public ResponseEntity<String> updateImmunization(
            @PathVariable() Integer facilityId,
            @PathVariable() String patientId,
            @PathVariable() Integer registryId,
            @PathVariable() String vaccinationId,
            @RequestBody String message,
            @RequestParam(required = false) String patientFhirId) {
        IParser parser = parser(message);
        Immunization immunization = parser.parseResource(Immunization.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(registryId);
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

            /**
             * If no fatal exception caught : stored fatal feedbacks are erased for the vaccination
             * TODO do the same for Patient ?
             */
            feedbackRepository.deleteByVaccinationEventIdAndIisAndSeverity(
                    vaccinationId,
                    immunizationRegistry.getId().toString(),
                    "fatal");
            return ResponseEntity.ok(outcome.getId().getIdPart());

        } catch ( FhirClientConnectionException f) {
            f.printStackTrace();
            return ResponseEntity.badRequest().body(f.getMessage());
        } catch (BaseServerResponseException baseServerResponseException) {
            Feedback feedback = new Feedback();
            feedback.setFacility(facilityRepository.findById(facilityId).orElseThrow());
            feedback.setPatient(ehrPatientRepository.findByFacilityIdAndId(facilityId,patientId).orElseThrow());
            feedback.setVaccinationEvent(vaccinationEventRepository.findByPatientIdAndId(patientId,vaccinationId).orElseThrow());
            feedback.setCode("invalid");
            feedback.setSeverity("fatal");
            feedback.setIis(String.valueOf(immunizationRegistry.getId()));
            feedback.setTimestamp(new Timestamp(new Date().getTime()));
            feedback.setContent(baseServerResponseException.getMessage());
            feedbackRepository.save(feedback);
            return ResponseEntity.badRequest().body(baseServerResponseException.getMessage());
//            throw baseServerResponseException;
        }
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> getImmunization(
            @PathVariable() Integer registryId,
            @PathVariable() int vaccinationId) {
        ImmunizationRegistry registry = immunizationRegistryController.settings(registryId);
        return ResponseEntity.ok(resourceClient.read("immunization", String.valueOf(vaccinationId), registry));
    }

    @PostMapping(IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postResource(
            @PathVariable() Integer registryId,
            @RequestParam(name = "type") String type,
            @RequestBody String message) {
        IParser parser = parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry registry = immunizationRegistryController.settings(registryId);
        MethodOutcome outcome = resourceClient.create(resource, registry);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping(IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> putResource(
            @PathVariable() Integer registryId,
            @RequestParam String type,
            @RequestBody String message) {
        IParser parser = parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry ir = immunizationRegistryController.settings(registryId);
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
            @PathVariable() Integer registryId,
            @PathVariable() String target,
            @PathVariable() Optional<String> targetId,
            @RequestParam Map<String,String> allParams) {
        Parameters parameters = new Parameters();
        for (Map.Entry<String,String> entry: allParams.entrySet()) {
            parameters.addParameter(entry.getKey(),entry.getValue());
        }
        operationType = operationType.replaceFirst("\\$","");

        IGenericClient client = customClientFactory.newGenericClient(immunizationRegistryController.settings(registryId));

        IOperation iOperation = client.operation();
        IOperationUnnamed iOperationUnnamed;
        if (targetId.isPresent()) {
            iOperationUnnamed = iOperation.onInstance(target + "/" + targetId.get());
        } else {
            iOperationUnnamed = iOperation.onType(target);
        }
        Bundle bundle = iOperationUnnamed.named(operationType)
                .withParameters(parameters)
                .prettyPrint().useHttpGet().returnResourceType(Bundle.class)
                .execute();

        return ResponseEntity.ok(parser("").encodeResourceToString(bundle));
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
