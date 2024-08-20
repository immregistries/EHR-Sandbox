package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.Client.CustomNarrativeGenerator;
import org.immregistries.ehr.fhir.Server.ServerR4.EhrFhirServerR4;
import org.immregistries.ehr.fhir.Server.ServerR5.EhrFhirServerR5;
import org.immregistries.ehr.logic.mapping.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class FhirComponentsDispatcher {

    public static String R5_FLAVOUR = "R5";
    public static String R4_FLAVOUR = "R4";
    private final CustomNarrativeGenerator customNarrativeGenerator = new CustomNarrativeGenerator();
    Logger logger = LoggerFactory.getLogger(FhirComponentsDispatcher.class);

    @Autowired
    private PatientMapperR5 patientMapperR5;
    @Autowired
    private ImmunizationMapperR5 immunizationMapperR5;
    @Autowired
    private GroupMapperR5 groupMapperR5;
    @Autowired
    private OrganizationMapperR5 organizationMapperR5;
    @Autowired
    private PractitionerMapperR5 practitionerMapperR5;
    @Autowired
    private PatientMapperR4 patientMapperR4;
    @Autowired
    private ImmunizationMapperR4 immunizationMapperR4;
    @Autowired
    private GroupMapperR4 groupMapperR4;
    @Autowired
    private OrganizationMapperR4 organizationMapperR4;
    @Autowired
    private PractitionerMapperR4 practitionerMapperR4;
    @Autowired
    private EhrFhirServerR4 ehrFhirServerR5;
    @Autowired
    private EhrFhirServerR5 ehrFhirServerR4;

    private FhirContext fhirContextR5;
    private FhirContext fhirContextR4;

    private CustomClientFactory customClientFactoryR5;
    private CustomClientFactory customClientFactoryR4;
    private Map<Class, IEhrEntityFhirMapper> mappersR4 = new HashMap<Class, IEhrEntityFhirMapper>(10);
    private Map<Class, IEhrEntityFhirMapper> mappersR5 = new HashMap<Class, IEhrEntityFhirMapper>(10);

    public FhirComponentsDispatcher(@Qualifier("fhirContextR5") FhirContext fhirContextR5,
                                    @Qualifier("fhirContextR4") FhirContext fhirContextR4
//                                 ,ApplicationContext context
    ) {
        this.fhirContextR5 = fhirContextR5;
        this.fhirContextR5.setNarrativeGenerator(customNarrativeGenerator);
        customClientFactoryR5 = new CustomClientFactory();
        customClientFactoryR5.setFhirContext(fhirContextR5);
        customClientFactoryR5.setServerValidationMode(ServerValidationModeEnum.NEVER);

        this.fhirContextR4 = fhirContextR4;
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
        } else if (tenantName.contains(R5_FLAVOUR)) {
            return fhirContextR5;
        } else if (tenantName.contains(R4_FLAVOUR)) {
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
        } else if (tenantName.contains(R5_FLAVOUR)) {
            return customClientFactoryR5;
        } else if (tenantName.contains(R4_FLAVOUR)) {
            return customClientFactoryR4;
        } else return customClientFactoryR5;
    }


    public IEhrEntityFhirMapper mapper(Class type) {
        initMappers();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String tenantName = (String) request.getAttribute("TENANT_NAME");
        if (StringUtils.isBlank(tenantName)) {
            return mappersR5.get(type);
        } else if (tenantName.contains(R5_FLAVOUR)) {
            return mappersR5.get(type);
        } else if (tenantName.contains(R4_FLAVOUR)) {
            return mappersR4.get(type);
        } else return mappersR5.get(type);
    }

    private void initMappers() {
        if (mappersR5.isEmpty()) {
            mappersR5.put(EhrPatient.class, patientMapperR5);
            mappersR5.put(VaccinationEvent.class, immunizationMapperR5);
            mappersR5.put(EhrGroup.class, groupMapperR5);
            mappersR5.put(Facility.class, organizationMapperR5);
            mappersR5.put(Clinician.class, practitionerMapperR5);
            logger.info("Ignited FHIR R5 mappers registry");

        }
        if (mappersR4.isEmpty()) {
            mappersR4.put(EhrPatient.class, patientMapperR4);
            mappersR4.put(VaccinationEvent.class, immunizationMapperR4);
            mappersR4.put(EhrGroup.class, groupMapperR4);
            mappersR4.put(Facility.class, organizationMapperR4);
            mappersR4.put(Clinician.class, practitionerMapperR4);
            logger.info("Ignited FHIR R4 mappers registry");

        }
    }

    public IPatientMapper patientMapper() {
        return (IPatientMapper) mapper(EhrPatient.class);
    }

    public IImmunizationMapper immunizationMapper() {
        return (IImmunizationMapper) mapper(VaccinationEvent.class);
    }

    public IGroupMapper groupMapper() {
        return (IGroupMapper) mapper(EhrGroup.class);
    }

    public IPractitionerMapper practitionerMapper() {
        return (IPractitionerMapper) mapper(Clinician.class);
    }

    public IOrganizationMapper organizationMapper() {
        return (IOrganizationMapper) mapper(Facility.class);
    }

    public RestfulServer restfulServer() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String tenantName = (String) request.getAttribute("TENANT_NAME");
        if (StringUtils.isBlank(tenantName)) {
            return ehrFhirServerR5;
        } else if (tenantName.contains(R5_FLAVOUR)) {
            return ehrFhirServerR5;
        } else if (tenantName.contains(R4_FLAVOUR)) {
            return ehrFhirServerR4;
        } else return ehrFhirServerR4;
    }

    public IResourceProvider provider(String resourceType) {
        RestfulServer restfulServer = restfulServer();
        return restfulServer
                .getResourceProviders()
                .stream()
                .filter((p) -> p.getResourceType().getSimpleName().equals(resourceType))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Provider not found for resourceType " + resourceType + " in server " + restfulServer.getServerName()));
    }
}
