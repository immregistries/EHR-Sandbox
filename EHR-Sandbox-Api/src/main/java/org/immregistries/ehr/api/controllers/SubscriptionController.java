package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Subscription;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrSubscriptionInfoRepository;
import org.immregistries.ehr.api.repositories.EhrSubscriptionRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.EhrFhirOutcome;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.fhir.client.IResourceClient;
import org.immregistries.ehr.logic.SubscriptionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    public static final String RESTHOOK = "rest-hook";
    public static final String EMAIL = "mailto:Clement.Hennequin@telecomnancy.net";
    private static final String LOCAL_TOPIC = "http://localhost:8080/SubscriptionTopic/feedback";

    public static final String SECRET_HEADER_NAME = "Authorization-Subscription";
    public static final String SECRET_PREFIX = " ";
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private EhrSubscriptionRepository ehrSubscriptionRepository;
    @Autowired
    private EhrSubscriptionInfoRepository subscriptionInfoRepository;
    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private IResourceClient resourceClient;
    @Autowired
    private SubscriptionGenerator subscriptionGenerator;

    @GetMapping(FACILITY_ID_PATH + "/subscription")
    public Optional<EhrSubscription> ehrSubscription(@PathVariable(FACILITY_ID) Integer facilityId) {
        Optional<EhrSubscription> ehrSubscription = ehrSubscriptionRepository.findByIdentifier(EhrUtils.convert(facilityId));
        return ehrSubscription;
    }

    @GetMapping(FACILITY_ID_PATH + "/subscription/sample")
    public ResponseEntity<String> getSample(@PathVariable(FACILITY_ID) Integer facilityId, @RequestParam(REGISTRY_ID) Integer registryId, @RequestParam("topic") Optional<String> topic) {
        Facility facility = facilityRepository.findById(facilityId).orElseThrow(() -> new RuntimeException("No facility found"));
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Subscription sub = subscriptionGenerator.generateRestHookSubscription(facility, topic.orElse(null));
        return ResponseEntity.ok().body(fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(sub));
    }

    @PostMapping(FACILITY_ID_PATH + FHIR_CLIENT + "/subscription")
    public EhrFhirOutcome subscribeToIIS(@PathVariable(FACILITY_ID) Integer facilityId, @RequestParam(REGISTRY_ID) Integer registryId, @RequestBody() Optional<String> stringBody, @RequestParam("topic") Optional<String> topic) {
        Facility facility = facilityRepository.findById(facilityId).orElseThrow(() -> new RuntimeException("No facility found"));
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Subscription sub;
        if (stringBody.isPresent()) {
            sub = fhirComponentsDispatcher.fhirContext().newJsonParser().parseResource(Subscription.class, stringBody.get());
        } else if (topic.isPresent()) {
            sub = subscriptionGenerator.generateRestHookSubscription(facility, topic.get());
        } else {
            sub = null;
        }
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
        MethodOutcome outcome = resourceClient.create(sub, client);
        processSubscriptionOutcome(ir, outcome);
        return EhrFhirOutcome.fromMethodOutcome(outcome, fhirComponentsDispatcher.parser("{}"));

    }

    @PutMapping(FACILITY_ID_PATH + FHIR_CLIENT + "/subscription")
    public EhrFhirOutcome subscribeToIISManualUpdate(@RequestParam(REGISTRY_ID) Integer registryId, @RequestBody String stringBody) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Subscription sub = fhirComponentsDispatcher.fhirContext().newJsonParser().parseResource(Subscription.class, stringBody);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
        MethodOutcome outcome = resourceClient.updateOrCreate(sub, "Subscription", new EhrIdentifier(sub.getIdentifierFirstRep()), client);
        processSubscriptionOutcome(ir, outcome);
        return EhrFhirOutcome.fromMethodOutcome(outcome, fhirComponentsDispatcher.parser("{}"));
    }

//    @PostMapping(FACILITY_ID_PATH + FHIR_CLIENT + "/subscription")
//    public EhrFhirOutcome subscribeToIISFeedback(@RequestParam(REGISTRY_ID) Integer registryId, @PathVariable(FACILITY_ID) Integer facilityId, @RequestParam("groupId") Optional<String> groupId, @RequestParam("topic") Optional<String> topic) {
//        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
//        Facility facility = facilityRepository.findById(facilityId).orElseThrow(() -> new RuntimeException("No facility found"));
//        Subscription sub = generateRestHookSubscription(facility, ir.getIisFhirUrl());
//        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
//        MethodOutcome outcome = resourceClient.updateOrCreate(sub, "Subscription", new EhrIdentifier(sub.getIdentifierFirstRep()), client);
//        processSubscriptionOutcome(ir, outcome);
//        return EhrFhirOutcome.fromMethodOutcome(outcome, fhirComponentsDispatcher.parser("{}"));
//    }


    /**
     * Saves subscription information to local database from the FHIR method outcome
     *
     * @param immunizationRegistry remote registry information
     * @param outcome              FHIR methodOutcome
     * @return Updated Subscription
     */
    private EhrSubscription processSubscriptionOutcome(ImmunizationRegistry immunizationRegistry, MethodOutcome outcome) {
        Subscription outcomeSub = (Subscription) outcome.getResource();
        if ((outcome.getCreated() != null && outcome.getCreated()) || (outcome.getResource() != null)) {
            outcomeSub.setStatus(Enumerations.SubscriptionStatusCodes.ACTIVE);
        }
        EhrSubscription ehrSubscription = new EhrSubscription(outcomeSub);
        ehrSubscription.setImmunizationRegistry(immunizationRegistry);
        EhrSubscriptionInfo subscriptionInfo = new EhrSubscriptionInfo(ehrSubscription);
//        ehrSubscription.setSubscriptionInfo(subscriptionInfo);
        return ehrSubscriptionRepository.save(ehrSubscription);
//        switch(outcomeSub.getStatus()) {
//            case ACTIVE: {
//                // return positive message
//                // set up waiting for handshake and heartbeat
//                break;
//            }
//            case REQUESTED: {
//                break;
//            }
//            case OFF: {
//                break;
//            }
//            case ERROR:
//            case NULL:
//            case ENTEREDINERROR: {
//            }
//        }
//        return ehrSubscription;
    }


}
