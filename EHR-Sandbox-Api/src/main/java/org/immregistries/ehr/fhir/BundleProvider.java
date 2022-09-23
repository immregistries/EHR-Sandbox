package org.immregistries.ehr.fhir;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.ResourceType;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.immregistries.ehr.EhrApiApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public class BundleProvider implements IResourceProvider {
        private static final Logger logger = LoggerFactory.getLogger(BundleProvider.class);

        private OperationOutcomeProvider operationOutcomeProvider;
        private SubscriptionStatusProvider subscriptionStatusProvider;

        public BundleProvider(OperationOutcomeProvider operationOutcomeProvider, SubscriptionStatusProvider subscriptionStatusProvider) {
                this.operationOutcomeProvider = operationOutcomeProvider;
                this.subscriptionStatusProvider = subscriptionStatusProvider;
        }

        /**
         * The getResourceType method comes from IResourceProvider, and must
         * be overridden to indicate what type of resource this provider
         * supplies.
         */

        @Override
        public Class<Bundle> getResourceType() {
            return Bundle.class;
        }

        @Create()
        public MethodOutcome create(@ResourceParam Bundle bundle, RequestDetails requestDetails, HttpServletRequest request) {
                // TODO Security checks, secrets ib headers or bundle (maybe in interceptors)

                IParser parser = EhrApiApplication.fhirContext.newJsonParser();
                logger.info("BUNDLE " + parser.encodeResourceToString(bundle));
//
                MethodOutcome outcome = new MethodOutcome();
                Bundle outcomeBundle = new Bundle();
                if (!bundle.getType().equals(Bundle.BundleType.SUBSCRIPTIONNOTIFICATION)) {
                      throw new InvalidRequestException("Bundles other than Subscription notification not supported");
                }

                SubscriptionStatus subscriptionStatus = (SubscriptionStatus) bundle.getEntryFirstRep().getResource();
                outcome = subscriptionStatusProvider.create(subscriptionStatus ,requestDetails);
                if (subscriptionStatus.getType().equals(SubscriptionStatus.SubscriptionNotificationType.EVENTNOTIFICATION)){
                        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
                                outcome = processPostEntry(entry,requestDetails, request);
//                        switch (entry.getRequest().getMethod()) {
//                                case POST: {
//                                        outcome = processPostEntry(entry,requestDetails);
//                                        // TODO combine outcomes ?
////                                        outcomeBundle.addEntry().setResource(processPostEntry(entry,requestDetails));
//                                        break;
//                                }
//                        }
                        }
                }
                return outcome;
        }

        private MethodOutcome processPostEntry(Bundle.BundleEntryComponent entry, RequestDetails requestDetails, HttpServletRequest request) {
                MethodOutcome methodOutcome = new MethodOutcome();
                switch (entry.getResource().getResourceType()){
                        case OperationOutcome: {
                                methodOutcome = operationOutcomeProvider.registerOperationOutcome((OperationOutcome) entry.getResource(),requestDetails, request);
                                break;
                        }
//                        case SubscriptionStatus: {
//                                methodOutcome = subscriptionStatusProvider.create((SubscriptionStatus) entry.getResource(),requestDetails);
//                                break;
//                        }
                }
                return methodOutcome;
        }

//        private IResourceProvider getProvider(ResourceType type) {
//                return  server.getResourceProviders().stream().filter(
//                        iResourceProvider -> iResourceProvider.getResourceType().getName().equals(type.name())
//                ).findFirst().get();
//        }

}
