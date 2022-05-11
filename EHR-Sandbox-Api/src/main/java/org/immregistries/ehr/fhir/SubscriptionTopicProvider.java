package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.hl7.fhir.r5.model.SubscriptionTopic.SubscriptionTopicResourceTriggerComponent;

public class SubscriptionTopicProvider implements IResourceProvider {



    /**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
    @Override
    public Class<SubscriptionTopic> getResourceType() {
        return SubscriptionTopic.class;
    }

    @Read()
    public SubscriptionTopic readSubscription() {
        SubscriptionTopicResourceTriggerComponent patientTrigger = new SubscriptionTopicResourceTriggerComponent()
                .setResourceType("Patient");

        SubscriptionTopic topic  = new SubscriptionTopic()
                .setDescription("Testing communication between EHR and IIS and operation outcome")
                .setUrl("https://loaclhost:9091/ehr-sandbox/fhir/SubscriptionTopic")
//                .setUrl("https://florence.immregistries.org/iis-sandbox/fhir/SubscriptionTopic")
                .setStatus(Enumerations.PublicationStatus.DRAFT);
        topic.addResourceTrigger(patientTrigger);
        return topic;
    }

}
