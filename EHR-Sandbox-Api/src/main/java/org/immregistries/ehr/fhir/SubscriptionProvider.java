package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Subscription;


public class SubscriptionProvider implements IResourceProvider {


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
    public Subscription createSubscription(@ResourceParam Subscription subscription) {
        return subscription.copy();
    }

    @Read()
    public Subscription readSubscription() {
        OperationOutcome operationOutcome = new OperationOutcome();
        Subscription sub = new Subscription();
//        Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent()
//                .setType(Subscription.SubscriptionChannelType.RESTHOOK)
//                .setEndpoint(Server.serverBaseUrl)
//                .setPayload("application/json");
//        sub.setChannel(channel);
        sub.setReason("testing purposes");
        sub.setTopic("/subscriptionTopic");

//        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        return sub;
    }


}
