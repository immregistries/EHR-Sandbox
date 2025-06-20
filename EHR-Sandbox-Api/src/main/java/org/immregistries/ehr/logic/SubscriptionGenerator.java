package org.immregistries.ehr.logic;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Subscription;
import org.immregistries.ehr.api.entities.Facility;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.Random;

import static org.immregistries.ehr.api.controllers.SubscriptionController.*;

@Service
public class SubscriptionGenerator {


    /**
     * IN PROGRESS
     * TODO generate specific object for a list of supported topics
     *
     * @param facility
     * @param topicUrl
     * @return
     */
    public Subscription generateRestHookSubscription(Facility facility, String topicUrl) {
        Subscription subscription = generateRestHookSubscription(facility);
        if (StringUtils.isNotBlank(topicUrl)) {
            subscription.setTopic(topicUrl);
        }
        return subscription;
    }

    public Subscription generateRestHookSubscription(Facility facility) {
        Subscription sub = new Subscription();
        sub.addIdentifier().setValue(facility.getId() + "").setSystem("EHR_Sandbox"); // Currently facilityIds are used as identifiers
        sub.setStatus(Enumerations.SubscriptionStatusCodes.REQUESTED);
//        sub.setTopic(iis_uri + "/SubscriptionTopic/sandbox");
//        sub.setTopic(iis_uri.split("/fhir")[0] + "/SubscriptionTopic/Group");
        // TODO set canonical definition and host somewhere on HIT DEV ?
//        sub.setTopic(iis_uri.split("/fhir")[0] + "/SubscriptionTopic/data-quality-issues");
        sub.setTopic("/SubscriptionTopic/data-quality-issues");


        sub.setReason("testing purposes");
        /*
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

        /*
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
