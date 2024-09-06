package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Subscription;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.EhrSubscription;
import org.immregistries.ehr.api.entities.EhrSubscriptionInfo;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrSubscriptionInfoRepository;
import org.immregistries.ehr.api.repositories.EhrSubscriptionRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.IResourceClient;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.Optional;
import java.util.Random;


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
    FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    IResourceClient resourceClient;

    @GetMapping("/tenants/{tenantId}/facilities/{facilityId}/subscription")
    public Optional<EhrSubscription> ehrSubscription(@PathVariable() String facilityId) {
        Optional<EhrSubscription> ehrSubscription = ehrSubscriptionRepository.findByIdentifier(facilityId);
        return ehrSubscription;
    }

    @GetMapping("/tenants/{tenantId}/facilities/{facilityId}" + FhirClientController.IMM_REGISTRY_SUFFIX + "/subscription/sample")
    public ResponseEntity<String> getSample(@PathVariable() String facilityId, @PathVariable() String registryId) {
        Facility facility = facilityRepository.findById(facilityId).get();
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Subscription sub = generateRestHookSubscription(facility, ir.getIisFhirUrl());
        return ResponseEntity.ok().body(fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(sub));
    }

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}" + FhirClientController.IMM_REGISTRY_SUFFIX + "/subscription")
    public Boolean subscribeToIISManualCreate(@PathVariable() String registryId, @RequestBody String stringBody) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Subscription sub = fhirComponentsDispatcher.fhirContext().newJsonParser().parseResource(Subscription.class, stringBody);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
        MethodOutcome outcome = resourceClient.create(sub, client);
        processSubscriptionOutcome(ir, outcome);
        return outcome.getCreated();
    }

    @PutMapping("/tenants/{tenantId}/facilities/{facilityId}" + FhirClientController.IMM_REGISTRY_SUFFIX + "/subscription")
    public Boolean subscribeToIISManualUpdate(@PathVariable() String registryId, @RequestBody String stringBody) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Subscription sub = fhirComponentsDispatcher.fhirContext().newJsonParser().parseResource(Subscription.class, stringBody);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
        MethodOutcome outcome = resourceClient.updateOrCreate(sub, "Subscription", new EhrIdentifier(sub.getIdentifierFirstRep()), client);
        processSubscriptionOutcome(ir, outcome);
        return outcome.getCreated();
    }

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}" + FhirClientController.IMM_REGISTRY_SUFFIX + "/subscription/data-quality-issues")
    public Boolean subscribeToIISFeedback(@PathVariable() String registryId, @PathVariable() String facilityId, @RequestParam Optional<String> groupId) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        Facility facility = facilityRepository.findById(facilityId).orElseThrow(() -> new RuntimeException("No facility found"));
        Subscription sub = generateRestHookSubscription(facility, ir.getIisFhirUrl());
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(ir);
        MethodOutcome outcome = resourceClient.updateOrCreate(sub, "Subscription", new EhrIdentifier(sub.getIdentifierFirstRep()), client);
        processSubscriptionOutcome(ir, outcome);
        return outcome.getCreated();
    }


    private EhrSubscription processSubscriptionOutcome(ImmunizationRegistry ir, MethodOutcome outcome) {
        Subscription outcomeSub = (Subscription) outcome.getResource();
        if ((outcome.getCreated() != null && outcome.getCreated()) || (outcome.getResource() != null)) {
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
        return ehrSubscription;
    }

    public Subscription generateRestHookSubscription(Facility facility, String iis_uri) {
        Subscription sub = new Subscription();
        sub.addIdentifier().setValue(facility.getId() + "").setSystem("EHR_Sandbox"); // Currently facilityIds are used as identifiers
        sub.setStatus(Enumerations.SubscriptionStatusCodes.REQUESTED);
//        sub.setTopic(iis_uri + "/SubscriptionTopic/sandbox");
//        sub.setTopic(iis_uri.split("/fhir")[0] + "/SubscriptionTopic/Group");
        sub.setTopic(iis_uri.split("/fhir")[0] + "/SubscriptionTopic/data-quality-issues");


        sub.setReason("testing purposes");
        /**
         * Giving a name for display with facility number and name
         */
        sub.setName("EHR n" + facility.getId() + " " + facility.getNameDisplay());

        sub.setHeartbeatPeriod(5);
        sub.setTimeout(30);
        sub.setEnd(new Date(System.currentTimeMillis() + 3 * 60 * 1000));
        sub.setContent(Subscription.SubscriptionPayloadContent.FULLRESOURCE);
        sub.setContentType("application/fhir+json");

        sub.setChannelType(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/subscription-channel-type").setCode(RESTHOOK));
        sub.setEndpoint(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/fhir/R5/" + facility.getId());

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
        sub.addParameter().setName(SECRET_HEADER_NAME).setValue(SECRET_PREFIX + generatedString);

        /**
         * Fetching the topic as it is currently defined in the IIS Sandbox
         * TODO define canonical ?
         */
//        SubscriptionTopic topic;
//        URL url;
//        HttpURLConnection con;
//        try {
//            url = new URL(iis_uri.split("/fhir")[0] + "/SubscriptionTopic");
//            con = (HttpURLConnection) url.openConnection();
//            con.setRequestMethod("GET");
//            con.setRequestProperty("Content-Type", "application/json");
//            con.setConnectTimeout(5000);
//            int status = con.getResponseCode();
//            if (status == 200) {
//                topic = fhirContext.newJsonParser().parseResource(SubscriptionTopic.class, con.getInputStream());
//                sub.addContained(topic);
//                sub.setTopicElement(new CanonicalType(url.toExternalForm()));
//            } else {
//                logger.info("ERROR getting Topic {}",status);
//            }
//            con.disconnect();
//
//        } catch (MalformedURLException | ProtocolException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return sub;
    }


}
