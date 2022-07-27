package org.immregistries.ehr.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Subscription;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.entities.SubscriptionStore;
import org.immregistries.ehr.fhir.SubscriptionProvider;
import org.immregistries.ehr.logic.CustomClientBuilder;
import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.repositories.SubscriptionStoreRepository;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
//@RequestMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
//@RequestMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private SubscriptionStoreRepository subscriptionStoreRepository;

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
    public Boolean subscribeToIIS(@PathVariable() int facilityId , @RequestBody String body) {
        logger.info("Subscription post");
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        Subscription sub = SubscriptionProvider.generateRestHookSubscription(facilityId, ir.getIisFhirUrl());
        IGenericClient client = new CustomClientBuilder(ir).getClient();

        MethodOutcome outcome = client.create().resource(sub).execute();

        if(outcome.getResource() == null) {

        }
        IParser parser  = EhrApiApplication.fhirContext.newJsonParser();
        Subscription outcomeSub = (Subscription) outcome.getResource();

        logger.info(parser.encodeResourceToString(outcomeSub));

        subscriptionStoreRepository.save(new SubscriptionStore(outcomeSub));

        switch(outcomeSub.getStatus()) {
            case ACTIVE: {
                // return positive message
                // set up waiting for handshake and heartbeat
                break;
            }
            case REQUESTED: {
                break;
            }
            case OFF: {
                break;
            }
            case ERROR:
            case NULL:
            case ENTEREDINERROR: {

            }
        }
        return outcome.getCreated();
    }

}
