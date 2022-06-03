package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Subscription;
import org.immregistries.ehr.controllers.FeedbackController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;


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
        sub.setTopic("/subscriptionTopic");
        sub.setEndpoint(request.getServletPath());
        logger.info(request.getServletPath());
        logger.info(String.valueOf(ServletUriComponentsBuilder.fromRequestUri(request)));

//        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        return sub;
    }
}
