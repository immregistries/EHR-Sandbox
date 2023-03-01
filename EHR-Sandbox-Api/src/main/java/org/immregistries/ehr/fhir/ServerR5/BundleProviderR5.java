package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.AuditRevisionListener;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.EhrSubscription;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.repositories.EhrSubscriptionRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.immregistries.ehr.api.controllers.SubscriptionController.SECRET_HEADER_NAME;
import static org.immregistries.ehr.api.controllers.SubscriptionController.SECRET_PREFIX;

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
        EhrSubscriptionRepository ehrSubscriptionRepository;
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
                EhrSubscription ehrSubscription = null;
                if (subscriptionReference.getIdentifier() != null && subscriptionReference.getIdentifier().getValue() != null) {
                        ehrSubscription = ehrSubscriptionRepository.findById(subscriptionReference.getIdentifier().getValue())
                                .orElseThrow(() -> new InvalidRequestException("No active subscription found with identifier"));
                } else {
                        ehrSubscription = ehrSubscriptionRepository.findByExternalId(new IdType(subscriptionReference.getReference()).getIdPart())
                                .orElseThrow(() -> new InvalidRequestException("No active subscription found with id"));
                }

                /**
                 * Subscription Security check
                 */
                String secret = request.getHeader(SECRET_HEADER_NAME);
                logger.info("Subscription header is : {}", ehrSubscription.getHeader());
                logger.info("Received header is : {}", SECRET_HEADER_NAME + ":" + SECRET_PREFIX + secret);

                if (!ehrSubscription.getHeader().equals(SECRET_HEADER_NAME + ":" + SECRET_PREFIX + secret)) {
                        throw new AuthenticationException("Invalid header for subscription notification");
                }

                ImmunizationRegistry immunizationRegistry = ehrSubscription.getImmunizationRegistry();

                /**
                 * Done for Historical log un Audit revision tables
                 */
                request.setAttribute(AuditRevisionListener.IMMUNIZATION_REGISTRY_ID,immunizationRegistry.getId()); // TODO link with subscription for origin analysis
                request.setAttribute(AuditRevisionListener.SUBSCRIPTION_ID,ehrSubscription.getIdentifier()); // TODO link with subscription for origin analysis

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
