package org.immregistries.ehr;

import ca.uhn.fhir.context.FhirVersionEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "hapi.fhir")
@Configuration
@EnableConfigurationProperties
public class FhirConfig {

    private FhirVersionEnum fhir_version = FhirVersionEnum.R5;

}
