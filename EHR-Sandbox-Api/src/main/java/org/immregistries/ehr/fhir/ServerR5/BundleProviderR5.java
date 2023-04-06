package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.AuditRevisionListener;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.EhrSubscription;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.repositories.EhrSubscriptionRepository;
import org.immregistries.ehr.fhir.EhrFhirProvider;
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
    private ImmunizationRecommendationProviderR5 immunizationRecommendationProvider;
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
    public MethodOutcome create(@ResourceParam Bundle bundle, ServletRequestDetails requestDetails) {
        HttpServletRequest request = requestDetails.getServletRequest();
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

        if (!ehrSubscription.getHeader().equals(SECRET_HEADER_NAME + ":" + SECRET_PREFIX + secret)) {
            throw new AuthenticationException("Invalid header for subscription notification");
        }

        ImmunizationRegistry immunizationRegistry = ehrSubscription.getImmunizationRegistry();

        /**
         * Done for Historical log in Audit revision tables, as it is done in a spring bean
         */
        request.setAttribute(AuditRevisionListener.IMMUNIZATION_REGISTRY_ID, immunizationRegistry.getId()); // TODO link with subscription for origin analysis
        request.setAttribute(AuditRevisionListener.SUBSCRIPTION_ID, ehrSubscription.getIdentifier());
        request.setAttribute(AuditRevisionListener.USER_ID, immunizationRegistry.getUser().getId());

        MethodOutcome statusOutcome = subscriptionStatusProvider.create(subscriptionStatus, requestDetails);
        //TODO check topic, do a different topic for operationOutcome?
        Bundle outcomeBundle = new Bundle().setType(Bundle.BundleType.SUBSCRIPTIONNOTIFICATION); // TODO
        outcomeBundle.addEntry().setResource((Resource) statusOutcome.getOperationOutcome());
        if (subscriptionStatus.getType().equals(SubscriptionStatus.SubscriptionNotificationType.EVENTNOTIFICATION)) {
            /**
             * First load patients, then immunization to solve references, then immunizationRecommendations
             *
             */
            EhrFhirProvider[] providers = new EhrFhirProvider[]{
                    patientProvider,
                    immunizationProvider,
                    immunizationRecommendationProvider,
                    operationOutcomeProvider
            };
            for (EhrFhirProvider provider : providers) {
                bundle.getEntry().stream().filter((entry -> entry.getResource().getResourceType().equals(provider.getResourceName()))).iterator().forEachRemaining(entry -> {
                    switch (entry.getRequest().getMethodElement().getValue()) {
                        case POST:
                        case PUT: {
                            outcomeBundle.addEntry().setRequest(entry.getRequest())
                                    .setResource((Resource) provider.update(entry.getResource(), requestDetails, immunizationRegistry)
                                            .getOperationOutcome());
                            break;
                        }
//                        case DELETE: {
//                            break;
//                        }
                    }
                });
            }
        }
        MethodOutcome aglomeratedOutcome = new MethodOutcome().setResource(outcomeBundle);
        return aglomeratedOutcome;
    }


}
