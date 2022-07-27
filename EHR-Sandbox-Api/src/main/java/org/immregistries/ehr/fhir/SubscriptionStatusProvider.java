package org.immregistries.ehr.fhir;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.SubscriptionStore;
import org.immregistries.ehr.repositories.SubscriptionStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Controller
public class SubscriptionStatusProvider implements IResourceProvider {
    @Autowired
    SubscriptionStoreRepository subscriptionStoreRepository;
    @Autowired
    OperationOutcomeProvider operationOutcomeProvider;

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionStatusProvider.class);
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
        IParser parser = EhrApiApplication.fhirContext.newJsonParser();
        switch (status.getType()){
            case HANDSHAKE: {
                processHandshake(status,theRequestDetails, methodOutcome);
                break;
            }
            case HEARTBEAT: {
                processHeartbeat(status,theRequestDetails, methodOutcome);
                break;
            }
            case NULL: {
                break;
            }
            case EVENTNOTIFICATION: {
//                operationOutcomeProvider.registerOperationOutcome(theRequestDetails, status)
                break;
            }
        }
        return methodOutcome;
    }

    private void processHandshake(SubscriptionStatus status, RequestDetails theRequestDetails, MethodOutcome methodOutcome) {
        logger.info("Handshake {} {}", status.getSubscription(), status.getStatus());

        Optional<SubscriptionStore> subscriptionStore = subscriptionStoreRepository.findById(theRequestDetails.getTenantId());
        if (subscriptionStore.isPresent()) {
            subscriptionStore.get().setStatus(status.getStatus().toCode());
            subscriptionStoreRepository.save(subscriptionStore.get());
            methodOutcome.setCreated(true);
        } else {
            throw new InvalidRequestException("No subscription of this id");
        }
    }

    private void processHeartbeat(SubscriptionStatus status, RequestDetails theRequestDetails, MethodOutcome methodOutcome) {
//        checking if subscription still exists and is active on this side
        logger.info("Heartbeat {} {}", status.getSubscription(), status.getStatus());
        Optional<SubscriptionStore> subscriptionStore = subscriptionStoreRepository.findByIdentifier(theRequestDetails.getTenantId());
        if (subscriptionStore.isPresent()) {
            Subscription subscription = subscriptionStore.get().toSubscription();
            switch(subscription.getStatus()) {
                case ACTIVE: {
                    methodOutcome.setCreated(true);
                    break;
                }
                case REQUESTED:
                case OFF:
                case ERROR:
                case NULL:
                case ENTEREDINERROR: {
                    throw new InvalidRequestException("Subscription no longer active");
                }
            }
        } else {
            throw new InvalidRequestException("");
        }

    }



}
