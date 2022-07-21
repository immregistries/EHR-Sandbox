package org.immregistries.ehr.fhir;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.http.client.HttpClient;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.EhrApiApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;


public class SubscriptionProvider implements IResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionProvider.class);

    public static final String RESTHOOK = "rest-hook";
    /**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
    @Override
    public Class<Subscription> getResourceType() {
        return Subscription.class;
    }

    @Create()
    public MethodOutcome createSubscription(@ResourceParam Subscription subscription) {
        return new MethodOutcome();
    }

    @Read()
    public Subscription readSubscription(HttpServletRequest request, @IdParam IdType id) {
        OperationOutcome operationOutcome = new OperationOutcome();
        Subscription sub = new Subscription();
//        Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent()
//                .setType(Subscription.SubscriptionChannelType.RESTHOOK)
//                .setEndpoint(Server.serverBaseUrl)
//                .setPayload("application/json");
//        sub.setChannel(channel);
        sub.setReason("testing purposes");
        sub.addHeader("secret");
        sub.setTopic("/subscriptionTopic");
        sub.setEndpoint(request.getServletPath());
        logger.info(request.getServletPath());
        logger.info(String.valueOf(ServletUriComponentsBuilder.fromRequestUri(request)));

//        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        return sub;
    }

    public static Subscription generateRestHookSubscription(Integer facilityId, String iis_uri) {
        Subscription sub = new Subscription();
        sub.addIdentifier().setValue(facilityId + "").setSystem("EHR Sandbox"); // Currently facilityIds are used as identifiers
        sub.setStatus(Enumerations.SubscriptionState.REQUESTED);
//        sub.setTopic("florence.immregistries.com/IIS-Sandbox/SubscriptionTopic");
        sub.setTopic("http://localhost:8080/SubscriptionTopic");

        sub.setReason("testing purposes");
        sub.setName("Ehr sandbox facility number " + facilityId);

        // TODO set random secret
        sub.addHeader("Authorization: Bearer secret-secret");
        sub.setHeartbeatPeriod(5);
        sub.setEnd(new Date());

        sub.setChannelType(new Coding("http://terminology.hl7.org/CodeSystem/subscription-channel-type", RESTHOOK,RESTHOOK));
        /**
         * TODO get server base url dynamically
         */

        sub.addFilterBy().setValue("value").setSearchParamName("name");

        sub.setEndpoint(Server.serverBaseUrl + "/" + facilityId);
//        sub.setContained()
//        sub.setEndpoint(theRequestDetails.getFhirServerBase() + "/" + facilityId + "/OperationOutcome");
        SubscriptionTopic topic;
        URL url = null;
        IParser parser = EhrApiApplication.fhirContext.newJsonParser();
        HttpURLConnection con;
        try {
            url = new URL(iis_uri + "SubscriptionTopic");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
                topic = parser.parseResource(SubscriptionTopic.class, con.getInputStream());
                sub.addContained(topic);
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
