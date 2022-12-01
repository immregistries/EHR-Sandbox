package org.immregistries.ehr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.immregistries.ehr.fhir.EhrFhirServer;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
@ServletComponentScan
public class EhrApiApplication extends SpringBootServletInitializer {
	@Autowired
	AutowireCapableBeanFactory beanFactory;
	@Autowired
	private ApplicationContext context;

	public static String VERSION = "1.2.0";
	@Bean
	public FhirContext fhirContext() {
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
	public ServletRegistrationBean<EhrFhirServer> popServletRegistrationBean() {
		ServletRegistrationBean<EhrFhirServer>  registrationBean = new ServletRegistrationBean<>();
		EhrFhirServer servlet = new EhrFhirServer(context.getBean(FhirContext.class));
		beanFactory.autowireBean(servlet);
		registrationBean.setServlet(servlet);
		registrationBean.addUrlMappings( "/fhir/*");
		registrationBean.setLoadOnStartup(1);
		return registrationBean;
	}

}
