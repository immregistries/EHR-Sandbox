package org.immregistries.ehr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.immregistries.ehr.fhir.Server.ServerR4.EhrFhirServerR4;
import org.immregistries.ehr.fhir.Server.ServerR5.EhrFhirServerR5;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {

    @Bean("fhirContextR4")
    public FhirContext fhirContextR4() {
        FhirContext fhirContext = new FhirContext(FhirVersionEnum.R4);
//        fhirContext.setNarrativeGenerator(customNarrativeGenerator);
        return fhirContext;
    }

    @Bean("fhirContextR5")
    public FhirContext fhirContextR5() {
        FhirContext fhirContext = new FhirContext(FhirVersionEnum.R5);
//        fhirContext.setNarrativeGenerator(customNarrativeGenerator);
//		fhirContext.setValidationSupport();
        return fhirContext;
    }

    @Bean
    public ServletRegistrationBean<EhrFhirServerR4> ServerR4RegistrationBean(EhrFhirServerR4 ehrFhirServerR4) {
        ServletRegistrationBean<EhrFhirServerR4> registrationBean = new ServletRegistrationBean<>();
        registrationBean.setServlet(ehrFhirServerR4);
        registrationBean.addUrlMappings("/fhir/R4/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<EhrFhirServerR5> ServerR5RegistrationBean(EhrFhirServerR5 ehrFhirServerR5) {
        ServletRegistrationBean<EhrFhirServerR5> registrationBean = new ServletRegistrationBean<>();
        registrationBean.setServlet(ehrFhirServerR5);
        registrationBean.addUrlMappings("/fhir/R5/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

}
