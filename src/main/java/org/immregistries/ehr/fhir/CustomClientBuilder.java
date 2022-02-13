package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.client.interceptor.UrlTenantSelectionInterceptor;


/**
 * CustomClientBuilder
 * 
 * Generates the FHIR Context of the skeleton
 * 
 * 
 */
public class CustomClientBuilder {

    private static final String TENANT_A = "TENANT-A";
    private static final String LOCALHOST_9091 = "http://localhost:9091/iis-sandbox/fhir";
    private static final String FLORENCE = "https://florence.immregistries.org/iis-sandbox/fhir";
    // Needs to be static object and built only one time in whole project
    private static final FhirContext CTX = FhirContext.forR4();

    private IGenericClient client = CTX.newRestfulGenericClient(FLORENCE);

    public CustomClientBuilder(){
        this(FLORENCE, TENANT_A, TENANT_A, TENANT_A);
    }

    public CustomClientBuilder(String serverURL){
        this(serverURL, TENANT_A, TENANT_A, TENANT_A);
    }

    public CustomClientBuilder( String tenantId, String username, String password){
        this(FLORENCE, tenantId, username, password);
    }

    public CustomClientBuilder(String serverURL, String tenantId, String username, String password){
        // Deactivate the request for server metadata
        CTX.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        // Create a client
        this.client = CTX.newRestfulGenericClient(serverURL);

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

    public static FhirContext getCTX() {
        return CTX;
    }
    
    
}
