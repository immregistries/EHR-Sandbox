package org.immregistries.ehr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.User;
import org.immregistries.ehr.fhir.ServerR4.EhrFhirServerR4;
import org.immregistries.ehr.fhir.ServerR5.EhrFhirServerR5;
import org.immregistries.ehr.fhir.annotations.OnR4Condition;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
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
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


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

	public static String VERSION = "1.2.3-SNAPSHOT";
	@Bean
	@Conditional(OnR4Condition.class)
	public FhirContext fhirContextR4() {
		return new FhirContext(FhirVersionEnum.R4);
	}
	@Bean
	@Conditional(OnR5Condition.class)
	public FhirContext fhirContextR5() {
		return new FhirContext(FhirVersionEnum.R5);
	}

	private static final Logger logger = LoggerFactory.getLogger(EhrApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(EhrApiApplication.class, args);
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
	@Conditional(OnR4Condition.class)
	public ServletRegistrationBean<EhrFhirServerR4> ServerR4RegistrationBean() {
		ServletRegistrationBean<EhrFhirServerR4>  registrationBean = new ServletRegistrationBean<>();
		EhrFhirServerR4 servlet = new EhrFhirServerR4(context.getBean(FhirContext.class)); //TODO change to R4 specifically
		beanFactory.autowireBean(servlet);
		registrationBean.setServlet(servlet);
		registrationBean.addUrlMappings( "/fhir/*","/ehr-sandbox/fhir/*");
		registrationBean.setLoadOnStartup(1);
		return registrationBean;
	}

	@Bean
	@Conditional(OnR5Condition.class)
	public ServletRegistrationBean<EhrFhirServerR5> ServerR5RegistrationBean() {
		ServletRegistrationBean<EhrFhirServerR5>  registrationBean = new ServletRegistrationBean<>();
		EhrFhirServerR5 servlet = new EhrFhirServerR5(context.getBean(FhirContext.class));
		beanFactory.autowireBean(servlet);
		registrationBean.setServlet(servlet);
		registrationBean.addUrlMappings( "/fhir/*","/ehr-sandbox/fhir/*");
		registrationBean.setLoadOnStartup(1);
		return registrationBean;
	}


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
	/**
	 * Map<FacilityId,Map<EhrPatientId, Set<ImmunizationRecommendation>>>
	 */
	public Map<Integer, Map<String, Set<ImmunizationRecommendation>>> immunizationRecommendations() {
		Map<Integer,Map<String, Set<ImmunizationRecommendation>>> map = new HashMap<>(10);
		return map;
	}

}
