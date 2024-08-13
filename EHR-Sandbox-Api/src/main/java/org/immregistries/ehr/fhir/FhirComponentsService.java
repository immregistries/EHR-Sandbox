package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.Client.CustomNarrativeGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Service
public class FhirComponentsService {
    private final CustomNarrativeGenerator customNarrativeGenerator = new CustomNarrativeGenerator();

    //    @Autowired
//    TenantRepository tenantRepository;
    private FhirContext fhirContextR5;
    private FhirContext fhirContextR4;
    private CustomClientFactory customClientFactoryR5;
    private CustomClientFactory customClientFactoryR4;

    public FhirComponentsService(@Qualifier("fhirContextR5") FhirContext fhirContextR5, @Qualifier("fhirContextR4") FhirContext fhirContextR4) {
        fhirContextR5.setNarrativeGenerator(customNarrativeGenerator);
        customClientFactoryR5 = new CustomClientFactory();
        customClientFactoryR5.setFhirContext(fhirContextR5);
        customClientFactoryR5.setServerValidationMode(ServerValidationModeEnum.NEVER);

        fhirContextR4.setNarrativeGenerator(customNarrativeGenerator);
        customClientFactoryR4 = new CustomClientFactory();
        customClientFactoryR4.setFhirContext(fhirContextR4);
        customClientFactoryR4.setServerValidationMode(ServerValidationModeEnum.NEVER);
    }

    private FhirContext defaultContext() {
        return fhirContextR5;
    }

    public FhirContext fhirContext() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return fhirContext((String) request.getAttribute("TENANT_NAME"));
    }

    private FhirContext fhirContext(String tenantName) {
        if (StringUtils.isBlank(tenantName)) {
            return defaultContext();
        } else if (tenantName.contains("R5")) {
            return fhirContextR5;
        } else if (tenantName.contains("R4")) {
            return fhirContextR4;
        } else return defaultContext();
    }

    public IParser parser(String message) {
        IParser parser;
        if (message.startsWith("<")) {
            parser = fhirContext().newXmlParser().setPrettyPrint(true);
        } else {
            parser = fhirContext().newJsonParser().setPrettyPrint(true);
        }
        return parser;
    }

    public CustomClientFactory clientFactory() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String tenantName = (String) request.getAttribute("TENANT_NAME");
        if (StringUtils.isBlank(tenantName)) {
            return customClientFactoryR5;
        } else if (tenantName.contains("R5")) {
            return customClientFactoryR5;
        } else if (tenantName.contains("R4")) {
            return customClientFactoryR4;
        } else return customClientFactoryR5;
    }

}
