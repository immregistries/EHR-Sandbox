package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.Client.CustomNarrativeGenerator;
import org.immregistries.ehr.fhir.Server.ServerR4.EhrFhirServerR4;
import org.immregistries.ehr.fhir.Server.ServerR5.EhrFhirServerR5;
import org.immregistries.ehr.logic.*;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.immregistries.ehr.api.AuditRevisionListener.TENANT_NAME;

@Service
public class FhirComponentsDispatcher {

    public static String R5_FLAVOUR = "R5";
    public static String R4_FLAVOUR = "R4";
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
    private EhrFhirServerR5 ehrFhirServerR5;
    @Autowired
    private EhrFhirServerR4 ehrFhirServerR4;
    @Autowired
    private IpsWriterR4 ipsWriterR4;
    @Autowired
    private IpsWriterR5 ipsWriterR5;
    @Autowired
    private FhirTransactionWriterR5 fhirTransactionWriterR5;
    @Autowired
    private BundleImportServiceR5 bundleImportServiceR5;
    @Autowired
    private FhirTransactionWriterR4 fhirTransactionWriterR4;
    @Autowired
    private BundleImportServiceR4 bundleImportServiceR4;

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
        CustomNarrativeGenerator customNarrativeGenerator = new CustomNarrativeGenerator();
        this.fhirContextR5.setNarrativeGenerator(customNarrativeGenerator);
        customClientFactoryR5 = new CustomClientFactory();
        customClientFactoryR5.setFhirContext(fhirContextR5);
        customClientFactoryR5.setServerValidationMode(ServerValidationModeEnum.NEVER);

        this.fhirContextR4 = fhirContextR4;
        fhirContextR4.setNarrativeGenerator(null);
        customClientFactoryR4 = new CustomClientFactory();
        customClientFactoryR4.setFhirContext(fhirContextR4);
        customClientFactoryR4.setServerValidationMode(ServerValidationModeEnum.NEVER);
    }


    public FhirContext fhirContext() {
        if (r5Flavor()) {
            return fhirContextR5;
        } else if (r4Flavor()) {
            return fhirContextR4;
        }
        return fhirContextR5;
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
        if (r5Flavor()) {
            return customClientFactoryR5;
        } else if (r4Flavor()) {
            return customClientFactoryR4;
        }
        return customClientFactoryR5;
    }


    public IIpsWriter ipsWriter() {
        if (r5Flavor()) {
            return ipsWriterR5;
        } else if (r4Flavor()) {
            return ipsWriterR4;
        }
        return ipsWriterR5;
    }


    public IEhrEntityFhirMapper mapper(Class type) {
        initMappers();
        if (r5Flavor()) {
            return mappersR5.get(type);
        } else if (r4Flavor()) {
            return mappersR4.get(type);
        }
        return mappersR5.get(type);
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
        if (r5Flavor()) {
            return ehrFhirServerR5;
        } else if (r4Flavor()) {
            return ehrFhirServerR4;
        }
        return ehrFhirServerR5;
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

    private static String tenantName() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return (String) request.getAttribute(TENANT_NAME);
    }

    public static boolean r4Flavor() {
        return StringUtils.defaultIfBlank(tenantName(), "").contains(R4_FLAVOUR);
    }

    public static boolean r5Flavor() {
        return StringUtils.defaultIfBlank(tenantName(), "").contains(R5_FLAVOUR);
    }

    public IFhirTransactionWriter fhirTransactionWriter() {
        if (r5Flavor()) {
            return fhirTransactionWriterR5;
        } else if (r4Flavor()) {
            return fhirTransactionWriterR4;
        }
        return fhirTransactionWriterR5;
    }

    public IBundleImportService bundleImportService() {
        if (r5Flavor()) {
            return bundleImportServiceR5;
        } else if (r4Flavor()) {
            return bundleImportServiceR4;
        }
        return bundleImportServiceR5;
    }

    public static IBaseParameters parametersObject() {
        if (r5Flavor()) {
            return new org.hl7.fhir.r5.model.Parameters();
        } else if (r4Flavor()) {
            return new org.hl7.fhir.r4.model.Parameters();
        }
        return new org.hl7.fhir.r5.model.Parameters();
    }

    public static Class parametersClass() {
        if (r5Flavor()) {
            return org.hl7.fhir.r5.model.Parameters.class;
        } else if (r4Flavor()) {
            return org.hl7.fhir.r4.model.Parameters.class;
        }
        return org.hl7.fhir.r5.model.Parameters.class;
    }

    public static Class bundleClass() {
        if (r5Flavor()) {
            return org.hl7.fhir.r5.model.Bundle.class;
        } else if (r4Flavor()) {
            return org.hl7.fhir.r4.model.Bundle.class;
        }
        return org.hl7.fhir.r5.model.Bundle.class;
    }

    public static List<IBaseResource> baseResourcesFromBaseBundleEntries(IBaseBundle iBaseBundle) {
        if (r4Flavor()) {
            org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) iBaseBundle;
            return bundle.getEntry().stream().filter(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent::hasResource).map(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent::getResource).collect(Collectors.toList());
        } else {
            org.hl7.fhir.r5.model.Bundle bundle = (org.hl7.fhir.r5.model.Bundle) iBaseBundle;
            return bundle.getEntry().stream().filter(org.hl7.fhir.r5.model.Bundle.BundleEntryComponent::hasResource).map(org.hl7.fhir.r5.model.Bundle.BundleEntryComponent::getResource).collect(Collectors.toList());
        }
    }

    public static List<IDomainResource> domainResourcesFromBaseBundleEntries(IBaseBundle iBaseBundle) {
        if (r4Flavor()) {
            org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) iBaseBundle;
            return bundle.getEntry().stream().filter(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent::hasResource).map(bundleEntryComponent -> (IDomainResource) bundleEntryComponent.getResource()).collect(Collectors.toList());
        } else {
            org.hl7.fhir.r5.model.Bundle bundle = (org.hl7.fhir.r5.model.Bundle) iBaseBundle;
            return bundle.getEntry().stream().filter(org.hl7.fhir.r5.model.Bundle.BundleEntryComponent::hasResource).map(bundleEntryComponent -> (IDomainResource) bundleEntryComponent.getResource()).collect(Collectors.toList());
        }
    }
}
