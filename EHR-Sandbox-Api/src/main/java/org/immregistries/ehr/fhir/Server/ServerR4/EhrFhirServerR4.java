package org.immregistries.ehr.fhir.Server.ServerR4;

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
import org.immregistries.ehr.fhir.Server.FhirAuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

@Component
public class EhrFhirServerR4 extends RestfulServer {
    private static final Logger logger = LoggerFactory.getLogger(EhrFhirServerR4.class);


    @Autowired
    OperationOutcomeProviderR4 operationOutcomeProvider;
    @Autowired
    PatientProviderR4 patientProvider;
    @Autowired
    ImmunizationProviderR4 immunizationProvider;
    @Autowired
    ImmunizationRecommendationProviderR4 immunizationRecommendationProvider;
    @Autowired
    GroupProviderR4 groupProvider;
    @Autowired
    FhirAuthInterceptor fhirAuthInterceptor;

    public EhrFhirServerR4(@Qualifier("fhirContextR4") FhirContext ctx) {
        super(ctx);
        setServerName("EHR Sandbox FHIR R4");
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
        setDefaultResponseEncoding(EncodingEnum.XML);

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLoggerName("FHIR Server R4");
        registerInterceptor(loggingInterceptor);

        ResponseHighlighterInterceptor responseHighlighterInterceptor = new ResponseHighlighterInterceptor();
        registerInterceptor(responseHighlighterInterceptor);

        registerInterceptor(fhirAuthInterceptor);

//        setTenantIdentificationStrategy(new CustomIdentificationStrategy());
        setTenantIdentificationStrategy(new UrlBaseTenantIdentificationStrategy());
//        setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBaseUrl));
        setServerAddressStrategy(ApacheProxyAddressStrategy.forHttps());

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(operationOutcomeProvider);
        resourceProviders.add(patientProvider);
        resourceProviders.add(immunizationProvider);
        resourceProviders.add(immunizationRecommendationProvider);
        resourceProviders.add(groupProvider);
        setResourceProviders(resourceProviders);

        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);
    }
}