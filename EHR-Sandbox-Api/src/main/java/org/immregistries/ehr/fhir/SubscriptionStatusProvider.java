package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionStatusProvider implements IResourceProvider {
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
    public MethodOutcome create(@ResourceParam SubscriptionStatus status) {
        MethodOutcome methodOutcome = new MethodOutcome();
        switch (status.getType()){
            case HANDSHAKE: {
                processHandshake(status, methodOutcome);
                break;
            }
            case HEARTBEAT: {
                processHeartbeat(status,methodOutcome);
                break;
            }
            case NULL: {
                break;
            }
        }
        return methodOutcome;
    }

    private void processHeartbeat(SubscriptionStatus status, MethodOutcome methodOutcome) {

    }

    private void processHandshake(SubscriptionStatus status, MethodOutcome methodOutcome) {

    }

}
