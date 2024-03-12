package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.gclient.IOperation;
import ca.uhn.fhir.rest.gclient.IOperationUnnamed;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.util.ClasspathUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.Feedback;
import org.immregistries.ehr.api.entities.ImmunizationIdentifier;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.PatientExternalIdentifier;
import org.immregistries.ehr.api.repositories.*;
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
    public static final String PATIENT_PREFIX = "/tenants/{tenantId}/facilities/{facilityId}/patients";
    public static final String GROUPS_PREFIX = "/tenants/{tenantId}/facilities/{facilityId}/groups";
    public static final String IMMUNIZATION_PREFIX = PATIENT_PREFIX + "/{patientId}/vaccinations";
    public static final String IMM_REGISTRY_SUFFIX = "/registry/{registryId}";
    private static final Logger logger = LoggerFactory.getLogger(FhirClientController.class);
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
        return ResponseEntity.ok(resourceClient.read(resourceType, id, immunizationRegistryController.getImmunizationRegistry(registryId)));
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

    @GetMapping("/smart-test/{keyId}")
    public ResponseEntity<String> searchFhirResourceFromIIS(@PathVariable String keyId) {
        ImmunizationRegistry immunizationRegistry = new ImmunizationRegistry();
        immunizationRegistry.setIisFhirUrl("http://localhost:8080/iis/fhir");
        immunizationRegistry.setIisUsername(keyId);
        immunizationRegistry.setIisPassword("-----BEGIN PRIVATE KEY-----\n" +
                "MIIJQgIBADANBgkqhkiG9w0BAQEFAASCCSwwggkoAgEAAoICAQChpp4YRHRa4nMG\n" +
                "tCgcLchUv8ZqnE0szIwY5hqOW43IHoDE6nLtIBVyn/q5bhh0CTUYcjao0IC2zY6W\n" +
                "zGPRzEwXPanRzLbJeMfXTh6y+F27M2d2QCGsxgdOI/L/3V84Lf8BTjpkQ5oDQsB3\n" +
                "jpGBZmHPkoOjOo7KGqo070ZsEy/AnTLHRUhMhYuGFoSo5+0pvj6rmKd04isHHip0\n" +
                "E8exvA5RLO1g1zWYFCUgiyQM8ac1C2JLswxxU825FSbVLePGzsxIXPXbcmrvFfKS\n" +
                "QzWHKSNVGx2dZx81yDFH57OzR/GilBqZX4EB/s4sbgv0UgAGdjY1uu4T4/lGWOsq\n" +
                "FFTiOx6wmxj/Ifs0R3+cRiQZtzzVphsH4O1llfGNJiywBOae9r8Lq2z/NbvIg9kk\n" +
                "AZRbYLt4znN01uxqf2cBVkZFx6YJ+rfqzUrazJMcqLl2VhR1VjHWXmojLr9OFZ+X\n" +
                "qqqgCa8oJqbmJ3qpMlEpI8INaLfuQVxI3RuowpYBmWMawqWO+2YNnkdwneIol0MZ\n" +
                "gHvpG8jB50UMrU3IkN0pMXSarEgQcpqAOfBte7x/teu54rRKs6wY59QuN+Tw/eBt\n" +
                "qE0Hp5nwqtwqeukhYFJAJFguW62vg/OWf9hC/Qstp00Vx76LWKNpDTC1mPJsvvc7\n" +
                "qrUpoEaFKHQPT7cv7l9o8eJuH/9TdwIDAQABAoICAGXrDzJtto62x58qZz+c/fmx\n" +
                "EACXm3DrZkksiwHGZ5YM5VNCHkOzPtKOWx4ec/x08a0AFcZJ711SLNyW8TiNfkwI\n" +
                "7NECKRzNsfHlpyVfwnkM5+Z+eLzd/i0eLU9021woY+cG03nzxV7Y71Bx6vuS+YhN\n" +
                "8AEyImT6euX1Fol9b1YZxrIs1HCqolVgTTPRRo5TDGBUVwAyuL48Hrgg4H8G+l/j\n" +
                "26Z7CadgzCfAknI2yh1/l/HyaZ3qIILY+yrdTAdEyze0pK4/bXxgchgVl+In5cb6\n" +
                "2aksI1ldZvTW+U3Hxmmix0J8HHKNnKAWoiLgsHNihLtLqF+SFNBtr5dQSlR4wdfG\n" +
                "hkLMVgWlKdNRbNVP2sndr4ExHxzm4GGN4xPK1wQSlsnPshmJLypP5gekdwzg8ABw\n" +
                "3KAj0ChZZMTTWNEecvLfIuiWAclkNBSoM8qVynZXp76v1NLn/R3naO4poClI6Upz\n" +
                "RWH5u28R/bTSF2VDaLeAgj74Oiq5n8xzXAM0fF6UIY7DVbRV1kjyQKnUbiJ0uvbQ\n" +
                "IFtptIAN8H3ntbLRGxHrj7gUPPrkBA+gV94ZXSKm9XJmIH6BMPNyZOmwO2kxe711\n" +
                "1rd7SL1D9QqETrHBsFOqVwX9vCdg4TRUe2sGCzoPm9Ompm2LMA9mH2non3ye3ISR\n" +
                "hzgVCAHp+EM1Bj+mvE7BAoIBAQDXIs1QpifZnrg2DwaasUGZIqbAf+2mBit3apSB\n" +
                "cQ59tMo5twYn37fCR/gYuV1UX/WsK0V3xZ9wR5zoQ8TGCMyQlRBo2KV0rQL07uU2\n" +
                "VouXjNr8j94hMmzRiJjU5tu1aUz4PJ6FdfuvQsusmZuljyjyXlZFtE8Y8USp8NcU\n" +
                "IY3U2ZGO0PNLcP2WfMWtTh4IrJ7UhCcfBdSnQfrmJVtgJqI1Qw4cDoMsU69HhWZw\n" +
                "PefZ+JcVPqwf6pRUNK13HX/N/5yRUvvnt7UfNddgIYKNXHrF8QaMsUnLegU4wWfX\n" +
                "zJI1AvWEax4DsgOZGJ94luvKwZtQirv7uVdEdLlpHHYdcz7dAoIBAQDAWwzG6yqx\n" +
                "fNFB/iJZKW4hh+y+4g5gpUt9EOeIjAYUQgmxkzh559cRqTaM6+9gljAbtUUgstYa\n" +
                "MX6Mp7lQ5FtJ+4zeNgp7jSCEAiktfU4ZdU+xoSeU9cVurD+y7mx+lbut/rFPQwZY\n" +
                "5ZS7IloaTlKPN3IOQIXmcYAZrB1qN/QpX4MEx2Fx5jJvMbVJVy+43oGsKQ8gpSwn\n" +
                "KWgicjlbaNWKTg44UYomLalMNBPMgDl9A+RiTYMTExDbrdtSUAk7dkCPxUVDV+5U\n" +
                "7CSbaiGE2ZLylMZw7u8Z0nOV82u10Sky9Iq3DVXZw7DUS0FLYycxgO1TNEHBhz7N\n" +
                "vQE7sRK+a9RjAoIBAQDJ7S96+EnFGBXdH6NaRO/GVWDgo/Kid34K2u3CxSQN10hM\n" +
                "fb54ZKKs5zI1up6gGGWWERNmBQbOs1jSJPGn0xB20IMTde1uowl4blaT9w2U1K+i\n" +
                "iUK8NAExdp3Ej18/WjPVd8huijwO14omWhNehQ2w19zwFfp79dE2FQk7KvdeBkx6\n" +
                "GSOS9t3+huRf80d7atK1s8iodKeRyfs+U2f9yqVLisHOkcyLKY0Ge26YAXMCAzOD\n" +
                "zExwtJQ+qSb7+6M+7iG9wX8RkjudrMKXxzwrhNbzvXQhUDJsmv4VtGrMgYXl3f4m\n" +
                "KQiF/WslIphj7nwo1in6og0pi2Aer9RrlTxEDiy9AoIBACrcXuV4AyRXRp1oziiF\n" +
                "5cdM7UC/SCrayg5NFPpFERm0eUp7HmtzcGW8Ca6u58Di1kdmRcf6cWCWxLb8rHtM\n" +
                "1taOWZ4t9nn/QU6I2LoNILjlfeN7oEE93kdB1FO/cqdmH9KuXL3nlmN6jICCMAeK\n" +
                "ijTlSuHIGM33Xm7Z/hfr7djGMoB/GMRzYauLLPTxm22bDtPYtkk05Li5Lj32q7cl\n" +
                "jedqaxq7eIf3RrD3La40qBqmIl6RHPLEHc6FV8hokxYPzGT8tOlDj5SWpbWHmX5r\n" +
                "qKWq5ujFJLvhO+YevlJeD8sN2FbfftCuHRnAxwTQTCxoqPkQPNKtmx3mzn6Jfz60\n" +
                "8jcCggEAR8g/lD+bWXIguc/4l8QeeU4eoOMtRMEscYzcCdkJ9N8837QKdgVtndjb\n" +
                "TvJq6d/iu01uPBJXW1hbpfEIjlpRbhgnhPucPQI1nqK/SbNuEAMnuYe+mX+2bBHw\n" +
                "s+VPi5WtzXEvbeZXs81eYJBTwqnUjMs3kWiXdEOiR5xmbBJru6jwNF12S5D6PdTw\n" +
                "avH/U8x2QUtMRKgbbvQRZDu93EOfT64e5uxpDaalY8Va+O/OC4wAQu72bydhNdZ/\n" +
                "f3k+Kg3ipXuvKQUERMNOFPA7IvF+2BVsX2MRgcNae7WPXt+zuVcDj2U4gK+KdGdY\n" +
                "eqceZ+pNrtgucUV8PNrTgb4ZUBWISA==\n" +
                "-----END PRIVATE KEY-----\n");
        immunizationRegistry.setIisFacilityId("");
        Bundle bundle = customClientFactory.smartAuthClient(immunizationRegistry).search()
                .forResource("Patient")
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
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
        MethodOutcome outcome = resourceClient.create(patient, ir);
        /**
         * Registering received id as external id
         */
        patientIdentifierRepository.save(new PatientExternalIdentifier(patientId, registryId, outcome.getId().getIdPart()));
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$match")
    public ResponseEntity<List<String>> matchPatient(
            @PathVariable() String facilityId,
            @PathVariable() Integer registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        Bundle bundle = matchPatientOperation(facilityId, registryId, patientId, message);

        return ResponseEntity.ok(
                bundle.getEntry().stream().filter(Bundle.BundleEntryComponent::hasResource).map(bundleEntryComponent -> bundleEntryComponent.getResource().getIdPart()).collect(Collectors.toList()
                )
        );
    }

    public Bundle matchPatientOperation(
            String facilityId,
            Integer registryId,
            String patientId,
            String message) {
        if (message == null) {
            message = fhirConversionController.getPatientAsResource(patientId, facilityId).getBody();
        }
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        Parameters parameters = new Parameters();
        return customClientFactory.newGenericClient(registryId).operation().onType("Patient").named("match").withParameters(parameters).andParameter("resource", patient).returnResourceType(Bundle.class).execute();
    }


    @PutMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> updatePatient(
            @PathVariable() Integer registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        IParser parser = parser(message);
        Patient patient = parser.parseResource(Patient.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.getImmunizationRegistry(registryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(patient,
                "Patient",
                patient.getIdentifierFirstRep(),
                immunizationRegistry);
        /**
         * Registering received id as external id
         */
        patientIdentifierRepository.save(new PatientExternalIdentifier(patientId, registryId, outcome.getId().getIdPart()));
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
                immunizationRegistryController.getImmunizationRegistry(registryId)));
    }

    @PostMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postImmunization(
            @PathVariable() Integer immunizationRegistryId,
            @PathVariable() String vaccinationId,
            @RequestBody String message,
            @RequestParam(required = false) String patientFhirId) {
        IParser parser = parser(message);
        Immunization immunization = parser.parseResource(Immunization.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.getImmunizationRegistry(immunizationRegistryId);
        if (patientFhirId != null && !patientFhirId.isEmpty()) {
            immunization.setPatient(new Reference().setReference("Patient/" + new IdType(patientFhirId).getIdPart()));
        }
        MethodOutcome outcome = resourceClient.create(immunization, immunizationRegistry);
        /**
         * Registering received id as external id
         */
        immunizationIdentifierRepository.save(new ImmunizationIdentifier(
                vaccinationId, immunizationRegistry.getId(), outcome.getId().getIdPart()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    @Transactional
    public ResponseEntity<String> updateImmunization(
            @PathVariable() String facilityId,
            @PathVariable() String patientId,
            @PathVariable() Integer registryId,
            @PathVariable() String vaccinationId,
            @RequestBody String message,
            @RequestParam(required = false) String patientFhirId) {
        IParser parser = parser(message);
        Immunization immunization = parser.parseResource(Immunization.class, message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.getImmunizationRegistry(registryId);
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
                    vaccinationId, immunizationRegistry.getId(), outcome.getId().getIdPart()));

            /**
             * If no fatal exception caught : stored fatal feedbacks are erased for the vaccination
             * TODO do the same for Patient ?
             */
            feedbackRepository.deleteByVaccinationEventIdAndIisAndSeverity(
                    vaccinationId,
                    immunizationRegistry.getId().toString(),
                    "fatal");
            return ResponseEntity.ok(outcome.getId().getIdPart());

        } catch (FhirClientConnectionException f) {
            f.printStackTrace();
            return ResponseEntity.badRequest().body(f.getMessage());
        } catch (BaseServerResponseException baseServerResponseException) {
            Feedback feedback = new Feedback();
            feedback.setFacility(facilityRepository.findById(facilityId).orElseThrow());
            feedback.setPatient(ehrPatientRepository.findByFacilityIdAndId(facilityId, patientId).orElseThrow());
            feedback.setVaccinationEvent(vaccinationEventRepository.findByPatientIdAndId(patientId, vaccinationId).orElseThrow());
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
        ImmunizationRegistry registry = immunizationRegistryController.getImmunizationRegistry(registryId);
        return ResponseEntity.ok(resourceClient.read("immunization", String.valueOf(vaccinationId), registry));
    }

    @PostMapping(IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postResource(
            @PathVariable() Integer registryId,
            @RequestParam(name = "type") String type,
            @RequestBody String message) {
        IParser parser = parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry registry = immunizationRegistryController.getImmunizationRegistry(registryId);
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
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
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
            @RequestParam Map<String, String> allParams) {
        Parameters parameters = new Parameters();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            parameters.addParameter(entry.getKey(), entry.getValue());
        }
        operationType = operationType.replaceFirst("\\$", "");

        IGenericClient client = customClientFactory.newGenericClient(immunizationRegistryController.getImmunizationRegistry(registryId));

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
