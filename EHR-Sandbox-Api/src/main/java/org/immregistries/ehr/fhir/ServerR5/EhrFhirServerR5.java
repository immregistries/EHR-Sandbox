package org.immregistries.ehr.fhir.ServerR5;

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
import org.immregistries.ehr.fhir.FhirAuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

//@WebServlet(urlPatterns = {"/fhir/R5/*"}, displayName = "FHIR Server for subscription endpoint")
//
public class EhrFhirServerR5 extends RestfulServer {
    private static final Logger logger = LoggerFactory.getLogger(EhrFhirServerR5.class);

    @Autowired
    BundleProviderR5 bundleProvider;
    @Autowired
    OperationOutcomeProviderR5 operationOutcomeProvider;
    @Autowired
    SubscriptionStatusProviderR5 subscriptionStatusProvider;
    @Autowired
    SubscriptionProviderR5 subscriptionProvider;
    @Autowired
    PatientProviderR5 patientProvider;
    @Autowired
    ImmunizationProviderR5 immunizationProvider;
    @Autowired
    ImmunizationRecommendationProviderR5 immunizationRecommendationProvider;
    @Autowired
    FhirAuthInterceptor fhirAuthInterceptor;

    public EhrFhirServerR5(FhirContext ctx) {
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

        registerInterceptor(fhirAuthInterceptor);

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
        resourceProviders.add(immunizationProvider);
        resourceProviders.add(immunizationRecommendationProvider);
        setResourceProviders(resourceProviders);

        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);
    }
}