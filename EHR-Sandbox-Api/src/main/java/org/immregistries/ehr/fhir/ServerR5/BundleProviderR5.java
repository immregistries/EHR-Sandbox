package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.SubscriptionStore;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.repositories.SubscriptionStoreRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@Conditional(OnR5Condition.class)
public class BundleProviderR5 implements IResourceProvider {
        private static final Logger logger = LoggerFactory.getLogger(BundleProviderR5.class);

        @Autowired
        private OperationOutcomeProviderR5 operationOutcomeProvider;
        @Autowired
        private ImmunizationProviderR5 immunizationProvider;
        @Autowired
        private PatientProviderR5 patientProvider;
        @Autowired
        private SubscriptionStatusProviderR5 subscriptionStatusProvider;
        @Autowired
        SubscriptionStoreRepository subscriptionStoreRepository;
        @Autowired
        FhirContext fhirContext;
        @Autowired
        private ImmunizationRegistryRepository immunizationRegistryRepository;

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
                logger.info("BUNDLE " + fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

                MethodOutcome outcome = new MethodOutcome();
                List<MethodOutcome> outcomeList = new ArrayList<>();
//                Bundle outcomeBundle = new Bundle();
                if (!bundle.getType().equals(Bundle.BundleType.SUBSCRIPTIONNOTIFICATION)) {
                      throw new InvalidRequestException("Bundles other than Subscription notification not supported");
                }

                /**
                 * Subscription Bundle first entry required to be Subscription Status
                 */
                SubscriptionStatus subscriptionStatus = (SubscriptionStatus) bundle.getEntryFirstRep().getResource();
                Reference subscriptionReference = subscriptionStatus.getSubscription();
                SubscriptionStore subscriptionStore = null;
                if (subscriptionReference.getIdentifier() != null && subscriptionReference.getIdentifier().getValue() != null) {
                        subscriptionStore = subscriptionStoreRepository.findById(subscriptionReference.getIdentifier().getValue())
                                .orElseThrow(() -> new InvalidRequestException("No active subscription found with identifier"));
                } else {
                        subscriptionStore = subscriptionStoreRepository.findByExternalId(new IdType(subscriptionReference.getReference()).getIdPart())
                                .orElseThrow(() -> new InvalidRequestException("No active subscription found with id"));
                }

                /**
                 * Subscription Security check
                 * TODO maybe move to an interceptor
                 */
//                String secret = (String) requestDetails.getAttribute("subscription-header");
//                if (!secret.equals(subscriptionStore.getHeader())) {
//                        throw new AuthenticationException("Invalid header for subscription notification");
//                }


                ImmunizationRegistry immunizationRegistry = subscriptionStore.getImmunizationRegistry();
                outcome = subscriptionStatusProvider.create(subscriptionStatus ,requestDetails);
                switch (subscriptionStatus.getTopic()) { //TODO check topic
                        default: {
                                if (subscriptionStatus.getType().equals(SubscriptionStatus.SubscriptionNotificationType.EVENTNOTIFICATION)){
                                        /**
                                         * First load patients, then immunization to solve references
                                         * TODO distinct POST AND PUT
                                         */

                                        bundle.getEntry().stream().filter((entry -> entry.getResource().getResourceType().equals(ResourceType.Patient))).iterator().forEachRemaining(entry -> {
                                                outcomeList.add(
                                                        patientProvider.updatePatient((Patient) entry.getResource(),requestDetails, immunizationRegistry)
                                                );
                                        });
                                        bundle.getEntry().stream().filter((entry -> entry.getResource().getResourceType().equals(ResourceType.Immunization))).iterator().forEachRemaining(entry -> {
                                                outcomeList.add(
                                                        immunizationProvider.updateImmunization((Immunization) entry.getResource(),requestDetails, immunizationRegistry)
                                                );
                                        });
                                        bundle.getEntry().stream().filter((entry -> entry.getResource().getResourceType().equals(ResourceType.OperationOutcome))).iterator().forEachRemaining(entry -> {
                                                outcomeList.add(
                                                        operationOutcomeProvider.registerOperationOutcome((OperationOutcome) entry.getResource(),requestDetails, request)
                                                );
                                        });;
                                }
                        }
                }
                // TODO make proper outcome
//                for (MethodOutcome methodOutcome: outcomeList) {
//
//                }
                return outcome;
        }



}
