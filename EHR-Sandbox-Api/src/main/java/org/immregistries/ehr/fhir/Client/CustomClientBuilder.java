package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.apache.ApacheRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.*;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


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
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;


    public IGenericClient newGenericClient(Integer registryId){
         return newGenericClient(immunizationRegistryRepository.findByIdAndUserId(registryId, userDetailsServiceImpl.currentUserId()).orElseThrow(
                 ()-> new RuntimeException("Invalid registry id") //TODO better exception message
         ));
    }

    public IGenericClient newGenericClient(ImmunizationRegistry registry){
         return newGenericClient(registry.getIisFhirUrl(), registry.getIisFacilityId(), registry.getIisUsername(), registry.getIisPassword(), registry.getHeaders());
    }

    public IGenericClient newGenericClient(String serverURL, String tenantId, String username, String password, String headers) {
        IGenericClient client = newGenericClient(serverURL, tenantId,  username,  password);
        AdditionalRequestHeadersInterceptor additionalRequestHeadersInterceptor = new AdditionalRequestHeadersInterceptor();
        for (String header: headers.split("\n")) {
            String[] headsplit = header.split(":",1);
            if (headsplit.length > 1) {
                additionalRequestHeadersInterceptor.addHeaderValue(headsplit[0], headsplit[1]);
            }
        }
        client.registerInterceptor(additionalRequestHeadersInterceptor);
        return  client;
    }
    public IGenericClient newGenericClient(String serverURL, String tenantId, String username, String password){
        IGenericClient client = newGenericClient(serverURL);
        // Register a tenancy interceptor to add /$tenantid to the url
        UrlTenantSelectionInterceptor tenantSelection = new UrlTenantSelectionInterceptor(tenantId);

        client.registerInterceptor(tenantSelection);
        IClientInterceptor authInterceptor;
        if (username == null || username.isBlank()) {
            /**
             * If username is blank : use token bearer auth
             */
            authInterceptor = new BearerTokenAuthInterceptor(password);
        }else {
            // Create an HTTP basic auth interceptor
            authInterceptor = new BasicAuthInterceptor(username, password);
        }
        client.registerInterceptor(authInterceptor);
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

    public String authorisationTokenContent(ImmunizationRegistry ir) {
        if (ir.getIisUsername() == null || ir.getIisUsername().isBlank()) {
            /**
             * If username is blank : use token bearer auth
             */
            return "Bearer " + ir.getIisPassword();
        }else {
            // Create an HTTP basic auth interceptor
            String encoded = Base64.getEncoder()
                    .encodeToString((ir.getIisUsername() + ":" + ir.getIisPassword())
                            .getBytes(StandardCharsets.UTF_8));  //Java 8
            return "Basic " + encoded;
        }
    }
}
