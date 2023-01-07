package org.immregistries.ehr.logic;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.apache.ApacheRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.*;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;


/**
 * CustomClientBuilder
 * 
 */
@Component
public class CustomClientBuilder extends ApacheRestfulClientFactory implements ITestingUiClientFactory {

    // Needs to be static object and built only one time in whole project
    @Autowired
    FhirContext fhirContext;

    LoggingInterceptor loggingInterceptor;
    private static final Logger logger = LoggerFactory.getLogger(CustomClientBuilder.class);

    public IGenericClient newGenericClient(ImmunizationRegistry registry){
         return newGenericClient(registry.getIisFhirUrl(), registry.getIisFacilityId(), registry.getIisUsername(), registry.getIisPassword());
    }

    public IGenericClient newGenericClient(String serverURL, String tenantId, String username, String password){
        IGenericClient client = newGenericClient(serverURL);
        // Register a tenancy interceptor to add /$tenantid to the url
        UrlTenantSelectionInterceptor tenantSelection = new UrlTenantSelectionInterceptor(tenantId);
        client.registerInterceptor(tenantSelection);
        // Create an HTTP basic auth interceptor
        IClientInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
        client.registerInterceptor(authInterceptor);
        // TODO ADD TOKEN MANUALLY ?
        return client;
    }

    public IGenericClient newGenericClient(String serverURL, String username, String password){
        IGenericClient client = newGenericClient(serverURL);
        IClientInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
        client.registerInterceptor(authInterceptor);
        return client;
    }

    @Override
    public synchronized IGenericClient newGenericClient(String theServerBase) {
        asynchInit();
        IGenericClient client = super.newGenericClient(theServerBase);
        client.registerInterceptor(loggingInterceptor);
//        AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
//        interceptor.addHeaderValue("Cache-Control", "no-cache");
//        client.registerInterceptor(interceptor);
        return client;
    }

    private void asynchInit() {
        if (this.getFhirContext() == null ){
            setFhirContext(fhirContext);
            setServerValidationMode(ServerValidationModeEnum.NEVER);
            loggingInterceptor = new LoggingInterceptor();
            loggingInterceptor.setLogger(logger);
            loggingInterceptor.setLogRequestSummary(true);
            loggingInterceptor.setLogRequestBody(true);
        }
    }

    @Override
    public IGenericClient newClient(FhirContext fhirContext, HttpServletRequest httpServletRequest, String s) {
        return null;
    }
}
