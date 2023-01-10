package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.SubscriptionInfo;
import org.immregistries.ehr.api.entities.SubscriptionStore;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.SubscriptionStoreRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientBuilder;
import org.immregistries.ehr.api.repositories.SubscriptionInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;


@RestController
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    public static final String RESTHOOK = "rest-hook";
    public static final String EMAIL = "mailto:Clement.Hennequin@telecomnancy.net";
    private static final String LOCAL_TOPIC = "http://localhost:8080/SubscriptionTopic";
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmRegistryController immRegistryController;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private SubscriptionStoreRepository subscriptionStoreRepository;
    @Autowired
    private SubscriptionInfoRepository subscriptionInfoRepository;
    @Autowired
    FhirContext fhirContext;
    @Autowired
    CustomClientBuilder customClientBuilder;

    @GetMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
    public Optional<SubscriptionStore> subscriptionStore(@PathVariable() String facilityId){
        Optional<SubscriptionStore> subscriptionStore = subscriptionStoreRepository.findByIdentifier(facilityId);
        return subscriptionStore;
    }

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}" + FhirClientController.IMM_REGISTRY_SUFFIX + "/subscription")
    public Boolean subscribeToIIS(@PathVariable() Integer immRegistryId, @PathVariable() int facilityId , @RequestBody String body) {
        ImmunizationRegistry ir = immRegistryController.settings(immRegistryId);
        Subscription sub = generateRestHookSubscription(facilityId, ir.getIisFhirUrl());
        IGenericClient client = customClientBuilder.newGenericClient(ir);

        MethodOutcome outcome = client.create().resource(sub).execute();
        IParser parser  = fhirContext.newJsonParser();
        Subscription outcomeSub = (Subscription) outcome.getResource();
        if (outcome.getCreated()){
            outcomeSub.setStatus(Enumerations.SubscriptionStatusCodes.ACTIVE);
        }
        SubscriptionStore subscriptionStore = new SubscriptionStore(outcomeSub);
        subscriptionStore.setImmunizationRegistry(ir);
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(subscriptionStore);
//        subscriptionStore.setSubscriptionInfo(subscriptionInfo);
        subscriptionStoreRepository.save(subscriptionStore);
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
        return outcome.getCreated();
    }

    public Subscription generateRestHookSubscription(Integer facilityId, String iis_uri) {
        Subscription sub = new Subscription();
        sub.addIdentifier().setValue(facilityId + "").setSystem("EHR Sandbox"); // Currently facilityIds are used as identifiers
        sub.setStatus(Enumerations.SubscriptionStatusCodes.REQUESTED);
//        sub.setTopic("florence.immregistries.com/IIS-Sandbox/SubscriptionTopic");
        sub.setTopic(LOCAL_TOPIC);

        sub.setReason("testing purposes");
        sub.setName("Ehr sandbox facility n" + facilityId);

        // TODO set random secret
        sub.addHeader("Authorization: Bearer secret-secret");
        sub.setHeartbeatPeriod(5);
        sub.setTimeout(30);
        sub.setEnd(new Date(System.currentTimeMillis() + 3 * 60 * 1000));
        sub.setContent(Subscription.SubscriptionPayloadContent.FULLRESOURCE);
        sub.setContentType("application/fhir+json");

        sub.setChannelType(new Coding("http://terminology.hl7.org/CodeSystem/subscription-channel-type", RESTHOOK,RESTHOOK));
        sub.setEndpoint(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/fhir/" + facilityId);
//        sub.setEndpoint(theRequestDetails.getFhirServerBase() + "/" + facilityId + "/OperationOutcome");
        SubscriptionTopic topic;
        URL url;
        IParser parser = fhirContext.newJsonParser();
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
//                logger.info("status {} topic {}",status, parser.encodeResourceToString(topic));
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
