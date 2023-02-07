package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.immregistries.ehr.api.entities.EhrSubscription;
import org.immregistries.ehr.api.repositories.EhrSubscriptionRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@Conditional(OnR5Condition.class)
public class SubscriptionStatusProviderR5 implements IResourceProvider {
    @Autowired
    EhrSubscriptionRepository ehrSubscriptionRepository;
    @Autowired
    OperationOutcomeProviderR5 operationOutcomeProvider;
    @Autowired
    FhirContext fhirContext;

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionStatusProviderR5.class);
    /**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
    @Override
    public Class<SubscriptionStatus> getResourceType() {
        return SubscriptionStatus.class;
    }

    @Create
    public MethodOutcome create(@ResourceParam SubscriptionStatus status, RequestDetails theRequestDetails) {
        logger.info("facility id {} status type {}", theRequestDetails.getTenantId(), status.getType());
        MethodOutcome methodOutcome = new MethodOutcome();
        IParser parser = fhirContext.newJsonParser();
        Optional<EhrSubscription> ehrSubscription;
        if (status.getSubscription().getId() != null) {
            ehrSubscription = ehrSubscriptionRepository.findById(status.getSubscription().getId());
        } else  {
            ehrSubscription = ehrSubscriptionRepository.findByIdentifier(theRequestDetails.getTenantId());
        }

        if (ehrSubscription.isPresent()) {
            switch (status.getType()){
                case HANDSHAKE: {
                    processHandshake(status, ehrSubscription.get(), theRequestDetails, methodOutcome);
                    break;
                }
                case HEARTBEAT: {
                    processHeartbeat(status, ehrSubscription.get(),theRequestDetails, methodOutcome);
                    break;
                }
                case NULL: {
                    break;
                }
                case EVENTNOTIFICATION: {
//                operationOutcomeProvider.registerOperationOutcome(theRequestDetails, status);
//                    if (!ehrSubscription.get().getStatus().equals("Active")) {
//                        throw new InvalidRequestException("No active  subscription registered with this id");
//                    }
                    logger.info("events number {}",status.getEventsSinceSubscriptionStartElement().getValue());
                    if (status.getEventsSinceSubscriptionStartElement().getValue().intValue() != ehrSubscription.get().getSubscriptionInfo().getEventsSinceSubscriptionStart() + 1) {
                        // TODO trigger problem when HAPI FHIR actually implements it
                    }
                    ehrSubscription.get().getSubscriptionInfo().setEventsSinceSubscriptionStart(status.getEventsSinceSubscriptionStartElement().getValue().intValue());
                    ehrSubscriptionRepository.save(ehrSubscription.get());
                    break;
                }
            }
        } else {
            throw new InvalidRequestException("No subscription registered with this id");
        }
        return methodOutcome;
    }


    private void processHandshake(SubscriptionStatus status, EhrSubscription ehrSubscription, RequestDetails theRequestDetails, MethodOutcome methodOutcome) {
        logger.info("Handshake {} {}", status.getSubscription(), status.getStatus());
        if (!ehrSubscription.getStatus().equals(Enumerations.SubscriptionStatusCodes.REQUESTED.toCode())) {
            throw new InvalidRequestException("Subscription not requested, " + ehrSubscription.getStatus());
        }
        ehrSubscription.setStatus(status.getStatus().toCode());
        ehrSubscriptionRepository.save(ehrSubscription);
        methodOutcome.setCreated(true);

    }

    private void processHeartbeat(SubscriptionStatus status, EhrSubscription ehrSubscription, RequestDetails theRequestDetails, MethodOutcome methodOutcome) {
//        checking if subscription still exists and is active on this side
        logger.info("Heartbeat {} {}", status.getSubscription(), status.getStatus());
        if (!ehrSubscription.getStatus().equals(Enumerations.SubscriptionStatusCodes.ACTIVE.toCode())) {
            throw new InvalidRequestException("Subscription no longer active");
        }
    }
}
