package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.ApacheProxyAddressStrategy;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.tenant.UrlBaseTenantIdentificationStrategy;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.repositories.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

//@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server Endpoint")
@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server for subscription endpoint")
//@Controller
//@WebServlet(urlPatterns = {"/fhir/*", "/fhir/**", "/fhir/tenants/{tenantId}/facilities/{facilityId}/*"}, displayName = "FHIR Server")
public class Server extends RestfulServer {
    @Autowired
    AutowireCapableBeanFactory beanFactory;
    private static final String DISPATCHER_CONTEXT_ATTRIBUTE_NAME = FrameworkServlet.SERVLET_CONTEXT_PREFIX + "EhrApiApplication";

    private static final long serialVersionUID = 1L;
    private static final FhirContext ctx = EhrApiApplication.fhirContext;

    /**
     * The initialize method is automatically called when the servlet is starting up, so it can
     * be used to configure the servlet to define resource providers, or set up
     * configuration, interceptors, etc.
     */

    @Override
    protected void initialize() throws ServletException {
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());

        } catch (NoSuchBeanDefinitionException e) {
            WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext(), DISPATCHER_CONTEXT_ATTRIBUTE_NAME);
//            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this, context);

        }
        this.setDefaultResponseEncoding(EncodingEnum.XML);
        setFhirContext(ctx);

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLoggerName("FHIR Server");
        registerInterceptor(loggingInterceptor);

//        setTenantIdentificationStrategy(new CustomIdentificationStrategy());
        setTenantIdentificationStrategy(new UrlBaseTenantIdentificationStrategy());
//        setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBaseUrl));
        setServerAddressStrategy(ApacheProxyAddressStrategy.forHttps());

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        SubscriptionStatusProvider subscriptionStatusProvider = new SubscriptionStatusProvider();
        OperationOutcomeProvider operationOutcomeProvider = new OperationOutcomeProvider();
        BundleProvider bundleProvider = new BundleProvider(operationOutcomeProvider, subscriptionStatusProvider);
        beanFactory.autowireBean(operationOutcomeProvider);
        beanFactory.autowireBean(subscriptionStatusProvider);
        beanFactory.autowireBean(bundleProvider);
        resourceProviders.add(new SubscriptionProvider());
        resourceProviders.add(subscriptionStatusProvider);
        resourceProviders.add(operationOutcomeProvider);
        resourceProviders.add(bundleProvider);
        setResourceProviders(resourceProviders);

        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);
    }
}