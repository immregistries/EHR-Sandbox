package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.gclient.IOperation;
import ca.uhn.fhir.rest.gclient.IOperationUnnamed;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.fhir.Client.MatchAndEverythingService;
import org.immregistries.ehr.fhir.Client.ResourceClient;
import org.immregistries.ehr.fhir.Client.SmartHealthLinksService;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.immregistries.ehr.fhir.Client.SmartHealthLinksService.SHLINK_PREFIX;

@RestController
public class FhirClientController {
    public static final String TENANT_PREFIX = "/tenants";
    public static final String FACILITY_PREFIX = TENANT_PREFIX + "/{tenantId}/facilities";
    public static final String CLINICIAN_PREFIX = TENANT_PREFIX + "/{tenantId}/clinicians";
    public static final String PATIENT_PREFIX = FACILITY_PREFIX + "/{facilityId}/patients";
    public static final String GROUPS_PREFIX = FACILITY_PREFIX + "/{facilityId}/groups";
    public static final String IMMUNIZATION_PREFIX = PATIENT_PREFIX + "/{patientId}/vaccinations";
    public static final String IMM_REGISTRY_SUFFIX = "/registry/{registryId}";
    public static final String PRIMAL_IMM_REGISTRY_SUFFIX = TENANT_PREFIX + "/{tenantId}/registry/{registryId}";
    private static final Logger logger = LoggerFactory.getLogger(FhirClientController.class);
    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private ResourceClient resourceClient;
    @Autowired
    private FhirConversionController fhirConversionController;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;
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
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SmartHealthLinksService smartHealthLinksService;

    @GetMapping(PRIMAL_IMM_REGISTRY_SUFFIX + "/{resourceType}/{id}")
    public ResponseEntity<String> getFhirResourceFromIIS(
            @PathVariable() String registryId,
            @PathVariable() String resourceType,
            @PathVariable() String id) {
        return ResponseEntity.ok(resourceClient.read(resourceType, id, immunizationRegistryService.getImmunizationRegistry(registryId)));
    }

    @PostMapping(PRIMAL_IMM_REGISTRY_SUFFIX + "/$import-shlink")
    public ResponseEntity<List<String>> smartHealthLink(
            @PathVariable() String registryId,
            @RequestBody() String url) throws MalformedURLException, URISyntaxException {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        List<String> body;
        if (url.contains(SHLINK_PREFIX)) {
            String shlink = SHLINK_PREFIX + url.split(SHLINK_PREFIX)[1];
            body = smartHealthLinksService.importSmartHealthLink(shlink, immunizationRegistry);
        } else {
            body = smartHealthLinksService.importSmartHealthLinkUrl(new URL(url), immunizationRegistry);
        }
        return ResponseEntity.ok(body);
    }


    @PostMapping(PRIMAL_IMM_REGISTRY_SUFFIX + "/{resourceType}/search")
    public ResponseEntity<String> searchFhirResourceFromIIS(
            @PathVariable() String registryId,
            @PathVariable() String resourceType,
            @RequestBody EhrIdentifier ehrIdentifier) {
        IQuery iQuery = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistryService.getImmunizationRegistry(registryId)).search()
                .forResource(resourceType);
        if (FhirComponentsDispatcher.r4Flavor()) {
            iQuery = iQuery.where(org.hl7.fhir.r4.model.Patient.IDENTIFIER.exactly().identifier(ehrIdentifier.toR4().getValue()))
                    .returnBundle(org.hl7.fhir.r4.model.Bundle.class);
        } else {
            iQuery = iQuery.where(org.hl7.fhir.r5.model.Patient.IDENTIFIER.exactly().identifier(ehrIdentifier.toR5().getValue()))
                    .returnBundle(org.hl7.fhir.r5.model.Bundle.class);
        }
        IBaseBundle bundle = (IBaseBundle) iQuery.execute();

        return ResponseEntity.ok(fhirComponentsDispatcher.parser("").encodeResourceToString(bundle));
    }

    //    @GetMapping("/smart-test")
//    public ResponseEntity<String> searchFhirResourceFromIIS(@PathVariable() String keyId) {
//
//    }
    @GetMapping("/smart-test/{keyId}")
    public ResponseEntity<String> searchFhirResourceFromIIS(@PathVariable() String keyId) {
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
        IBaseBundle bundle = (IBaseBundle) fhirComponentsDispatcher.clientFactory().smartAuthClient(immunizationRegistry).search()
                .forResource("Patient")
                .returnBundle(FhirComponentsDispatcher.bundleClass()).execute();
        return ResponseEntity.ok(fhirComponentsDispatcher.parser("").encodeResourceToString(bundle));
    }

    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postPatient(
            @PathVariable() String registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource patient = parser.parseResource(message);
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
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

    @Autowired
    MatchAndEverythingService matchAndEverythingService;

    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$match")
    public ResponseEntity<List<String>> matchPatient(
            @PathVariable() String tenantId,

            @PathVariable() String facilityId,
            @PathVariable() String registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow();
        return ResponseEntity.ok(matchAndEverythingService.matchPatientIdParts(registryId, message));
    }


    @PutMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> updatePatient(
            @PathVariable() String registryId,
            @PathVariable() String patientId,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource patient = parser.parseResource(message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(patient,
                "Patient",
                fhirComponentsDispatcher.patientMapper().getPatientIdentifier(patient),
                immunizationRegistry);
        /**
         * Registering received id as external id
         */
        patientIdentifierRepository.save(new PatientExternalIdentifier(patientId, registryId, outcome.getId().getIdPart()));
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }

        /**
         * If no fatal exception caught : stored fatal feedbacks are erased for the patient
         *
         */
        feedbackRepository.deleteByPatientIdAndIisAndSeverityAndVaccinationEventNull(
                patientId,
                immunizationRegistry.getId().toString(),
                "fatal");
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> getPatient(
            @PathVariable() String registryId,
            @PathVariable() String patientId) {
        return ResponseEntity.ok(resourceClient.read("patient", String.valueOf(patientId),
                immunizationRegistryService.getImmunizationRegistry(registryId)));
    }

    @PostMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir-client" + IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postImmunization(
            @PathVariable() String registryId,
            @PathVariable() String vaccinationId,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        IBaseResource baseImmunization = parser.parseResource(message); //TODO verify resourceType ?
        MethodOutcome outcome = resourceClient.create(baseImmunization, immunizationRegistry);
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
            @PathVariable() String registryId,
            @PathVariable() String vaccinationId,
            @RequestBody String message
//            @RequestParam(required = false) String patientFhirId DEPRECATED
    ) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource immunization = parser.parseResource(message);

        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        try {
            MethodOutcome outcome = resourceClient.updateOrCreate(immunization,
                    "Immunization",
                    fhirComponentsDispatcher.immunizationMapper().extractImmunizationIdentifier(immunization),
                    immunizationRegistry);
            /**
             * Registering received id as external id
             */
            immunizationIdentifierRepository.save(new ImmunizationIdentifier(
                    vaccinationId, immunizationRegistry.getId(), outcome.getId().getIdPart()));

            /**
             * If no fatal exception caught : stored fatal feedbacks are erased for the vaccination
             *
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
            @PathVariable() String registryId,
            @PathVariable() int vaccinationId) {
        ImmunizationRegistry registry = immunizationRegistryService.getImmunizationRegistry(registryId);
        return ResponseEntity.ok(resourceClient.read("immunization", String.valueOf(vaccinationId), registry));
    }

    @PostMapping(PRIMAL_IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> postResource(
            @PathVariable() String registryId,
            @RequestParam(name = "type") String type,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry registry = immunizationRegistryService.getImmunizationRegistry(registryId);
        MethodOutcome outcome = resourceClient.create(resource, registry);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping(PRIMAL_IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> putResource(
            @PathVariable() String registryId,
            @RequestParam String type,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(resource, type, null, ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    //    @PutMapping(FACILITY_PREFIX + "/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$transaction")
    @PostMapping(FACILITY_PREFIX + "/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$transaction")
    public ResponseEntity<String> transaction(
            @PathVariable() String registryId,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseBundle bundle = (IBaseBundle) parser.parseResource(message);
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
//        fhirComponentsService.clientFactory().newGenericClient(ir).transaction().withBundle(message).execute();
        IBaseBundle result = fhirComponentsDispatcher.clientFactory().newGenericClient(ir).transaction().withBundle(bundle).execute();
        return ResponseEntity.ok(parser.encodeResourceToString(result));
    }

    @PostMapping({
            PRIMAL_IMM_REGISTRY_SUFFIX + "/operation/{target}/{operationType}",
            PRIMAL_IMM_REGISTRY_SUFFIX + "/operation/{target}/{targetId}/{operationType}",
    })
    @PutMapping({
            PRIMAL_IMM_REGISTRY_SUFFIX + "/operation/{target}/{operationType}",
            PRIMAL_IMM_REGISTRY_SUFFIX + "/operation/{target}/{targetId}/{operationType}",
    })
    public ResponseEntity<Object> operation(
            @PathVariable() String operationType,
            @PathVariable() String registryId,
            @PathVariable() String target,
            @PathVariable() Optional<String> targetId,
            @RequestParam Map<String, String> allParams) {

        IBaseParameters parameters;
        if (FhirComponentsDispatcher.r4Flavor()) {
            parameters = new org.hl7.fhir.r4.model.Parameters();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                ((org.hl7.fhir.r4.model.Parameters) parameters).addParameter(entry.getKey(), entry.getValue());
            }
        } else {
            parameters = new org.hl7.fhir.r5.model.Parameters();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                ((org.hl7.fhir.r5.model.Parameters) parameters).addParameter(entry.getKey(), entry.getValue());
            }
        }

        operationType = operationType.replaceFirst("\\$", "");

        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistryService.getImmunizationRegistry(registryId));

        IOperation iOperation = client.operation();
        IOperationUnnamed iOperationUnnamed;
        if (targetId.isPresent()) {
            iOperationUnnamed = iOperation.onInstance(target + "/" + targetId.get());
        } else {
            iOperationUnnamed = iOperation.onType(target);
        }
        IBaseBundle bundle = (IBaseBundle) iOperationUnnamed.named(operationType)
                .withParameters(parameters)
                .prettyPrint().useHttpGet().returnResourceType(fhirComponentsDispatcher.bundleClass())
                .execute();

        return ResponseEntity.ok(fhirComponentsDispatcher.parser("").encodeResourceToString(bundle));
    }

}
