package org.immregistries.ehr.fhir;

import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.tenant.UrlBaseTenantIdentificationStrategy;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
public class Server extends RestfulServer {
    private static final long serialVersionUID = 1L;
    protected static  final String serverBaseUrl = "florence.immregistries.org/ehr-sandbox/fhir";


    /**
     * The initialize method is automatically called when the servlet is starting up, so it can
     * be used to configure the servlet to define resource providers, or set up
     * configuration, interceptors, etc.
     */

    @Override
    protected void initialize() throws ServletException {
        this.setDefaultResponseEncoding(EncodingEnum.XML);

        setTenantIdentificationStrategy(new UrlBaseTenantIdentificationStrategy());
        setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBaseUrl));

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(new SubscriptionProvider());
        setResourceProviders(resourceProviders);

        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);


    }
}