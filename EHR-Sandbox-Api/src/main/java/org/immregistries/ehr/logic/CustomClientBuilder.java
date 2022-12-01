package org.immregistries.ehr.logic;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.client.interceptor.UrlTenantSelectionInterceptor;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * CustomClientBuilder
 * 
 */
public class CustomClientBuilder {

    // Needs to be static object and built only one time in whole project
    @Autowired
    FhirContext fhirContext;

    private final IGenericClient client;


    public CustomClientBuilder(ImmunizationRegistry registry){
        this(registry.getIisFhirUrl(), registry.getIisFacilityId(), registry.getIisUsername(), registry.getIisPassword());
    }

    public CustomClientBuilder(String serverURL, String tenantId, String username, String password){
        // Deactivate the request for server metadata
        fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        // Create a client
        this.client = fhirContext.newRestfulGenericClient(serverURL);

        // Register a logging interceptor
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLogRequestSummary(true);
        loggingInterceptor.setLogRequestBody(true);
        this.client.registerInterceptor(loggingInterceptor);

        // Register a tenancy interceptor to add /$tenantid to the url
        UrlTenantSelectionInterceptor tenantSelection = new UrlTenantSelectionInterceptor(tenantId);
        this.client.registerInterceptor(tenantSelection);
        // Create an HTTP basic auth interceptor
        IClientInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
        this.client.registerInterceptor(authInterceptor);
    }

    public IGenericClient getClient() {
        return client;
    }

}
