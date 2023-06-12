package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.EhrSubscriptionInfo;
import org.immregistries.ehr.api.entities.EhrSubscription;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.EhrSubscriptionRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.api.repositories.EhrSubscriptionInfoRepository;
import org.immregistries.ehr.fhir.Client.IResourceClient;
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
import java.util.Random;


@RestController
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    public static final String RESTHOOK = "rest-hook";
    public static final String EMAIL = "mailto:Clement.Hennequin@telecomnancy.net";
    private static final String LOCAL_TOPIC = "http://localhost:8080/SubscriptionTopic";

    public static final String SECRET_HEADER_NAME = "Authorization-Subscription";
    public static final String SECRET_PREFIX = " ";
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryController immRegistryController;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private EhrSubscriptionRepository ehrSubscriptionRepository;
    @Autowired
    private EhrSubscriptionInfoRepository subscriptionInfoRepository;
    @Autowired
    FhirContext fhirContext;
    @Autowired
    CustomClientFactory customClientFactory;
    @Autowired
    IResourceClient resourceClient;

    @GetMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
    public Optional<EhrSubscription> ehrSubscription(@PathVariable() String facilityId){
        Optional<EhrSubscription> ehrSubscription = ehrSubscriptionRepository.findByIdentifier(facilityId);
        return ehrSubscription;
    }

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}" + FhirClientController.IMM_REGISTRY_SUFFIX + "/subscription")
    public Boolean subscribeToIIS(@PathVariable() Integer registryId, @PathVariable() int facilityId , @RequestBody String body) {
        ImmunizationRegistry ir = immRegistryController.settings(registryId);
        Facility facility = facilityRepository.findById(facilityId).orElseThrow(() -> new RuntimeException("No facility found"));
        Subscription sub = generateRestHookSubscription(facility, ir.getIisFhirUrl());
        IGenericClient client = customClientFactory.newGenericClient(ir);

//        MethodOutcome outcome = client.create().resource(sub).execute();
        MethodOutcome outcome = resourceClient.updateOrCreate(sub, "Subscription",sub.getIdentifierFirstRep(), client);
        Subscription outcomeSub = (Subscription) outcome.getResource();
        if ((outcome.getCreated() != null && outcome.getCreated()) || (outcome.getResource()!=null)){
            outcomeSub.setStatus(Enumerations.SubscriptionStatusCodes.ACTIVE);
        }
        EhrSubscription ehrSubscription = new EhrSubscription(outcomeSub);
        ehrSubscription.setImmunizationRegistry(ir);
        EhrSubscriptionInfo subscriptionInfo = new EhrSubscriptionInfo(ehrSubscription);
//        ehrSubscription.setSubscriptionInfo(subscriptionInfo);
        ehrSubscriptionRepository.save(ehrSubscription);
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

    public Subscription generateRestHookSubscription(Facility facility, String iis_uri) {
        Subscription sub = new Subscription();
        sub.addIdentifier().setValue(facility.getId() + "").setSystem("EHR_Sandbox"); // Currently facilityIds are used as identifiers
        sub.setStatus(Enumerations.SubscriptionStatusCodes.REQUESTED);
//        sub.setTopic("florence.immregistries.com/IIS-Sandbox/SubscriptionTopic");
        sub.setTopic(LOCAL_TOPIC);

        sub.setReason("testing purposes");
        /**
         * Giving a name for display with facility number and name
         */
        sub.setName("EHR sandbox n" + facility.getId() + " " + facility.getNameDisplay());

        sub.setHeartbeatPeriod(5);
        sub.setTimeout(30);
        sub.setEnd(new Date(System.currentTimeMillis() + 3 * 60 * 1000));
        sub.setContent(Subscription.SubscriptionPayloadContent.FULLRESOURCE);
        sub.setContentType("application/fhir+json");

        sub.setChannelType(new Coding("http://terminology.hl7.org/CodeSystem/subscription-channel-type", RESTHOOK,RESTHOOK));
        sub.setEndpoint(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/fhir/" + facility.getId());

        /**
         * Generating a key for identification
         *
         */
        byte[] array = new byte[256];
        new Random().nextBytes(array);
        RandomStringGenerator randomStringGenerator =
                new RandomStringGenerator.Builder()
                        .withinRange('0', 'z')
                        .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                        .build();
        String generatedString = randomStringGenerator.generate(64);
        sub.addHeader(SECRET_HEADER_NAME + ":" + SECRET_PREFIX + generatedString);

        /**
         * Fetching the topic as it is currently defined in the IIS Sandbox in order to fit
         * TODO define canonical ?
         */
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
