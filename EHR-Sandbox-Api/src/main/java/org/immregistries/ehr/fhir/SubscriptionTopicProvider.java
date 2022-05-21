package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
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
                .setResource("Patient");

        SubscriptionTopic topic  = new SubscriptionTopic()
                .setDescription("Testing communication between EHR and IIS and operation outcome")
                .setUrl("https://localhost:9091/ehr-sandbox/fhir/SubscriptionTopic")
//                .setUrl("https://florence.immregistries.org/iis-sandbox/fhir/SubscriptionTopic")
                .setStatus(Enumerations.PublicationStatus.DRAFT)
                .setExperimental(true).setPublisher("Aira/Nist").setTitle("Health equity data quality requests within Immunization systems");

        topic.addResourceTrigger(patientTrigger);
        topic.addEventTrigger().setEvent( new CodeableConcept()
                // https://terminology.hl7.org/3.1.0/ValueSet-v2-0003.html
                .addCoding(new Coding().setSystem("http://terminology.hl7.org/ValueSet/v2-0003").setCode("A04"))
                .addCoding(new Coding().setSystem("http://terminology.hl7.org/ValueSet/v2-0003").setCode("A28"))
                .addCoding(new Coding().setSystem("http://terminology.hl7.org/ValueSet/v2-0003").setCode("A31"))
        ).setResource("Patient");
        topic.addNotificationShape().setResource("OperationOutcome");

        return topic;
    }

}
