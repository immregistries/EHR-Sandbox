package org.immregistries.ehr.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.entities.SubscriptionStore;
import org.immregistries.ehr.fhir.Server;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;


@RestController
//@RequestMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
//@RequestMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    public static final String RESTHOOK = "rest-hook";
    public static final String EMAIL = "mailto:Clement.Hennequin@telecomnancy.net";
    private static final String LOCAL_TOPIC = "http://localhost:8080/SubscriptionTopic";
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
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        Subscription sub = generateRestHookSubscription(facilityId, ir.getIisFhirUrl());
        IGenericClient client = new CustomClientBuilder(ir).getClient();

        MethodOutcome outcome = client.create().resource(sub).execute();

        if(outcome.getResource() == null) {
            logger.info("No resource in outcome");
        }
        IParser parser  = EhrApiApplication.fhirContext.newJsonParser();
        Subscription outcomeSub = (Subscription) outcome.getResource();

//        logger.info(parser.encodeResourceToString(outcomeSub));
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

    public static Subscription generateRestHookSubscription(Integer facilityId, String iis_uri) {
        Subscription sub = new Subscription();
        sub.addIdentifier().setValue(facilityId + "").setSystem("EHR Sandbox"); // Currently facilityIds are used as identifiers
        sub.setStatus(Enumerations.SubscriptionState.REQUESTED);
//        sub.setTopic("florence.immregistries.com/IIS-Sandbox/SubscriptionTopic");
        sub.setTopic(LOCAL_TOPIC);

        sub.setReason("testing purposes");
        sub.setName("Ehr sandbox facility number " + facilityId);

        // TODO set random secret
        sub.addHeader("Authorization: Bearer secret-secret");
        sub.setHeartbeatPeriod(5);
        sub.setTimeout(30);
        sub.setEnd(new Date(System.currentTimeMillis() + 3 * 60 * 1000));
        sub.setContent(Subscription.SubscriptionPayloadContent.FULLRESOURCE);
        sub.setContentType("application/fhir+json");

        sub.setChannelType(new Coding("http://terminology.hl7.org/CodeSystem/subscription-channel-type", RESTHOOK,RESTHOOK));
        /**
         * TODO get server base url dynamically
         */

//        sub.addFilterBy().setResourceType("OperationOutcome")
//                .setSearchParamName("severity")
//                .setValue("warning").setSearchModifier(Enumerations.SubscriptionSearchModifier.EQUAL);
//        sub.addFilterBy().setResourceType("OperationOutcome")
//                .setSearchParamName("severity")
//                .setValue("error").setSearchModifier(Enumerations.SubscriptionSearchModifier.EQUAL);

        sub.setEndpoint(Server.serverBaseUrl + "/" + facilityId);
//        sub.setEndpoint(theRequestDetails.getFhirServerBase() + "/" + facilityId + "/OperationOutcome");
        SubscriptionTopic topic;
        URL url;
        IParser parser = EhrApiApplication.fhirContext.newJsonParser();
        HttpURLConnection con;
        try {
            url = new URL(iis_uri.split("/fhir")[0] + "/SubscriptionTopic");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
                topic = parser.parseResource(SubscriptionTopic.class, con.getInputStream());
                logger.info("status {} topic {}",status, parser.encodeResourceToString(topic));
                sub.addContained(topic);
                sub.setTopicElement(new CanonicalType(topic.getId().split("/")[1]));
            } else {
                logger.info("{}",status);
            }
            con.disconnect();

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return sub;
    }


}
