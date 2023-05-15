package org.immregistries.ehr.fhir;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;

/**
 * Incomplet, currently used for tracking of modifying users in envers
 */
@Interceptor
@Component
public class FhirAuthInterceptor {
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void incomingRequestPreHandled(
            ServletRequestDetails theRequestDetails,
            Pointcut thePointcut,
            HttpRequest httpRequest) {
        /**
         * attributes are currently set in BundleProvider, TODO clarify where it must be done
         *         theRequestDetails.getServletRequest().setAttribute(AuditRevisionListener.IMMUNIZATION_REGISTRY_ID,-1);
         *         theRequestDetails.getServletRequest().setAttribute(AuditRevisionListener.SUBSCRIPTION_ID,"-1");
         *         theRequestDetails.getServletRequest().setAttribute(AuditRevisionListener.USER_ID,"-1");
         */
    }

}
