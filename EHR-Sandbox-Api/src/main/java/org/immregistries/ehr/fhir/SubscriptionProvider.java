package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;


public class SubscriptionProvider implements IResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionProvider.class);
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
        sub.setTopic("florence.immregistries.com/IIS-Sandbox/SubscriptionTopic");

        sub.setReason("testing purposes");
        sub.setName("Ehr sandbox facility number " + facilityId);

        // TODO set random secret
        sub.addHeader("Authorization: Bearer secret-secret");
        sub.setHeartbeatPeriod(5);
        sub.setEnd(new Date());

        sub.setChannelType(new Coding("SubscriptionChannelType", "RESTHOOK","RESTHOOK"));
        /**
         * TODO get server base url dynamically
         */


        sub.setEndpoint(Server.serverBaseUrl + "/" + facilityId);
//        sub.setEndpoint(theRequestDetails.getFhirServerBase() + "/" + facilityId + "/OperationOutcome");
        return sub;
    }


}
