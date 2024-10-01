package org.immregistries.ehr;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.immregistries.ehr.api.entities.BulkImportStatus;
import org.immregistries.ehr.fhir.Server.ServerR4.EhrFhirServerR4;
import org.immregistries.ehr.fhir.Server.ServerR5.EhrFhirServerR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
@ServletComponentScan
@Import({
        FhirConfig.class
})
public class EhrApiApplication extends SpringBootServletInitializer {
    @Autowired
    AutowireCapableBeanFactory beanFactory;
    @Autowired
    private ApplicationContext context;

    public static String VERSION = "1.2.3-SNAPSHOT-5";

    private static final Logger logger = LoggerFactory.getLogger(EhrApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EhrApiApplication.class, args);
    }

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
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOriginPatterns("*")
//						.allowedOrigins("http://localhost:8080", "http://localhost:4200", "http://localhost:9091")
                        .allowedMethods("GET", "POST", "PUT");
            }
        };
    }

    @Bean
    public ServletRegistrationBean<EhrFhirServerR4> ServerR4RegistrationBean(EhrFhirServerR4 ehrFhirServerR4) {
        ServletRegistrationBean<EhrFhirServerR4> registrationBean = new ServletRegistrationBean<>();
        registrationBean.setServlet(ehrFhirServerR4);
        registrationBean.addUrlMappings("/fhir/R4/*", "/ehr/fhir/R4/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<EhrFhirServerR5> ServerR5RegistrationBean(EhrFhirServerR5 ehrFhirServerR5) {
        ServletRegistrationBean<EhrFhirServerR5> registrationBean = new ServletRegistrationBean<>();
        registrationBean.setServlet(ehrFhirServerR5);
        registrationBean.addUrlMappings("/fhir/R5/*", "/ehr/fhir/R5/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

//    @Bean
//    public ServletWebServerFactory servletWebServerFactory() {
//        ServletWebServerFactory servletWebServerFactory = new ServletWebServerFactory() {
//            @Override
//            public WebServer getWebServer(ServletContextInitializer... initializers) {
//                return null;
//            }
//        }
//    }

    /**
     * Required to get access to httpRequest qnd session through spring, important to use the fhir client inside the servlets
     *
     * @return
     */
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }


    @Bean
    public Map<String, BulkImportStatus> resultCacheStore() {
        return new HashMap<>(30);
    }

}
