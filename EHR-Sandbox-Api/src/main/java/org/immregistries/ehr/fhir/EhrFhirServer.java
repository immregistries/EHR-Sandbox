package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.ApacheProxyAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.rest.server.tenant.UrlBaseTenantIdentificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server for subscription endpoint")
public class EhrFhirServer extends RestfulServer {
    private static final Logger logger = LoggerFactory.getLogger(EhrFhirServer.class);

    @Autowired
    BundleProvider bundleProvider;
    @Autowired
    OperationOutcomeProvider operationOutcomeProvider;
    @Autowired
    SubscriptionStatusProvider subscriptionStatusProvider;
    @Autowired
    SubscriptionProvider subscriptionProvider;
    @Autowired
    PatientProvider patientProvider;

    public EhrFhirServer(FhirContext ctx) {
        super(ctx);
    }

    private static final String DISPATCHER_CONTEXT_ATTRIBUTE_NAME = FrameworkServlet.SERVLET_CONTEXT_PREFIX + "EhrApiApplication";
    private static final long serialVersionUID = 1L;

    /**
     * The initialize method is automatically called when the servlet is starting up, so it can
     * be used to configure the servlet to define resource providers, or set up
     * configuration, interceptors, etc.
     */

    @Override
    protected void initialize() throws ServletException {
//        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
//        try {
//            SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
//        } catch (NoSuchBeanDefinitionException e) {
//            WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext(), DISPATCHER_CONTEXT_ATTRIBUTE_NAME);
////            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this, context);
//        }
        setDefaultResponseEncoding(EncodingEnum.XML);

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLoggerName("FHIR Server");
        registerInterceptor(loggingInterceptor);

        ResponseHighlighterInterceptor responseHighlighterInterceptor = new ResponseHighlighterInterceptor();
        registerInterceptor(responseHighlighterInterceptor);

//        setTenantIdentificationStrategy(new CustomIdentificationStrategy());
        setTenantIdentificationStrategy(new UrlBaseTenantIdentificationStrategy());
//        setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBaseUrl));
        setServerAddressStrategy(ApacheProxyAddressStrategy.forHttps());

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(subscriptionProvider);
        resourceProviders.add(subscriptionStatusProvider);
        resourceProviders.add(operationOutcomeProvider);
        resourceProviders.add(bundleProvider);
        resourceProviders.add(patientProvider);
        setResourceProviders(resourceProviders);

        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);
    }
}