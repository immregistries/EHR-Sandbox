package org.immregistries.ehr.controllers;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Subscription;
import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.fhir.SubscriptionProvider;
import org.immregistries.ehr.logic.CustomClientBuilder;
import org.immregistries.ehr.repositories.FacilityRepository;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
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

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
    public MethodOutcome subscribeToIIS(@PathVariable() int facilityId , @RequestBody String body) {
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        Subscription sub = SubscriptionProvider.generateRestHookSubscription(facilityId, ir.getIisFhirUrl());
        IGenericClient client = new CustomClientBuilder(ir).getClient();
        MethodOutcome outcome = client.create().resource(sub).execute();
        return outcome;
    }

}
