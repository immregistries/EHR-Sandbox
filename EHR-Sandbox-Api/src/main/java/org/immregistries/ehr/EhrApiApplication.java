package org.immregistries.ehr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@SpringBootApplication
public class EhrApiApplication extends SpringBootServletInitializer {
	public static String VERSION = "1.2.0";
	public static final FhirContext fhirContext = new FhirContext(FhirVersionEnum.R5);
	private static final Logger logger = LoggerFactory.getLogger(EhrApiApplication.class);

	public static void main(String[] args) {
//		fhirContext =  new FhirContext(FhirVersionEnum.R5);
		SpringApplication.run(EhrApiApplication.class, args);

		final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		logger.info("Base Url for deployment : {}", baseUrl);
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

}
