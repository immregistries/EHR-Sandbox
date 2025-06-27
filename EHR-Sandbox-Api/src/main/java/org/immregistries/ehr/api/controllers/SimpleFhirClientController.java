package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IOperation;
import ca.uhn.fhir.rest.gclient.IOperationUnnamed;
import ca.uhn.fhir.rest.gclient.IQuery;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.fhir.Client.ResourceClient;
import org.immregistries.ehr.fhir.EhrFhirOutcome;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

public class SimpleFhirClientController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    private ResourceClient resourceClient;
    @Autowired
    private FeedbackController feedbackController;

    @GetMapping(FHIR_CLIENT_PATH + "/{resourceType}/{id}")
    public ResponseEntity<String> getFhirResourceFromIIS(
            @RequestParam(REGISTRY_ID) Integer registryId,
            @PathVariable("resourceType") String resourceType,
            @PathVariable("id") String id) {
        return ResponseEntity.ok(resourceClient.read(resourceType, id, immunizationRegistryService.getImmunizationRegistry(registryId)));
    }

    @PostMapping({FHIR_CLIENT_PATH + "/{resourceType}/search", FHIR_CLIENT + "/{resourceType}/search"})
    public ResponseEntity<String> searchFhirResourceFromIIS(
            @RequestParam(REGISTRY_ID) Integer registryId,
            @PathVariable("resourceType") String resourceType,
            @RequestBody EhrIdentifier ehrIdentifier) {
        IQuery iQuery = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistryService.getImmunizationRegistry(registryId)).search()
                .forResource(resourceType);
        if (ProcessingFlavor.R4.isActive()) {
            iQuery = iQuery.where(org.hl7.fhir.r4.model.Patient.IDENTIFIER.exactly().identifier(ehrIdentifier.toR4().getValue()))
                    .returnBundle(org.hl7.fhir.r4.model.Bundle.class);
        } else {
            iQuery = iQuery.where(org.hl7.fhir.r5.model.Patient.IDENTIFIER.exactly().identifier(ehrIdentifier.toR5().getValue()))
                    .returnBundle(org.hl7.fhir.r5.model.Bundle.class);
        }
        IBaseBundle bundle = (IBaseBundle) iQuery.execute();

        return ResponseEntity.ok(fhirComponentsDispatcher.parser("").encodeResourceToString(bundle));
    }

    @PostMapping({FHIR_CLIENT_PATH, FHIR_CLIENT_FACILITY_PATH})
    public ResponseEntity<EhrFhirOutcome> postResource(
            @RequestParam(REGISTRY_ID) Integer registryId,
            @PathVariable(FACILITY_ID) Optional<Integer> facilityId,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry registry = immunizationRegistryService.getImmunizationRegistry(registryId);
        MethodOutcome outcome = resourceClient.create(resource, registry);
        if (outcome.getOperationOutcome() != null) {
            feedbackController.extractAckInfoFHIR(Optional.of(registryId), facilityId, Optional.empty(), Optional.empty(), parser.encodeResourceToString(outcome.getOperationOutcome()));
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(EhrFhirOutcome.fromMethodOutcome(outcome, parser));
    }

    @PutMapping({FHIR_CLIENT_PATH, FHIR_CLIENT_FACILITY_PATH})
    public ResponseEntity<EhrFhirOutcome> putResource(
            @RequestParam(REGISTRY_ID) Integer registryId,
            @RequestParam("type") String type,
            @RequestBody String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource resource = parser.parseResource(message);
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        MethodOutcome outcome = resourceClient.updateOrCreate(resource, type, null, ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(EhrFhirOutcome.fromMethodOutcome(outcome, parser));
    }


    @PostMapping({
            FHIR_CLIENT_PATH + "/operation/{target}/{operationType}",
            FHIR_CLIENT_PATH + "/operation/{target}/{targetId}/{operationType}",
    })
    @PutMapping({
            FHIR_CLIENT_PATH + "/operation/{target}/{operationType}",
            FHIR_CLIENT_PATH + "/operation/{target}/{targetId}/{operationType}",
    })
    public ResponseEntity<Object> operation(
            @PathVariable("operationType") String operationType,
            @RequestParam(REGISTRY_ID) Integer registryId,
            @PathVariable("target") String target,
            @PathVariable("targetId") Optional<String> targetId,
            @RequestParam Map<String, String> allParams) {

        IBaseParameters parameters;
        if (ProcessingFlavor.R4.isActive()) {
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
