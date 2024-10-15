package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.fhir.Client.CustomNarrativeGenerator;
import org.immregistries.ehr.fhir.Client.EhrFhirClientFactory;
import org.immregistries.ehr.fhir.Server.ServerR4.EhrFhirServerR4;
import org.immregistries.ehr.fhir.Server.ServerR5.EhrFhirServerR5;
import org.immregistries.ehr.logic.*;
import org.immregistries.ehr.logic.mapping.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FhirComponentsDispatcher {
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

    private EhrFhirClientFactory ehrFhirClientFactoryR5;
    private EhrFhirClientFactory ehrFhirClientFactoryR4;
    private Map<Class, IEhrEntityFhirMapper> mappersR4 = new HashMap<Class, IEhrEntityFhirMapper>(10);
    private Map<Class, IEhrEntityFhirMapper> mappersR5 = new HashMap<Class, IEhrEntityFhirMapper>(10);

    public FhirComponentsDispatcher(@Qualifier("fhirContextR5") FhirContext fhirContextR5,
                                    @Qualifier("fhirContextR4") FhirContext fhirContextR4
//                                 ,ApplicationContext context
    ) {
        this.fhirContextR5 = fhirContextR5;
        CustomNarrativeGenerator customNarrativeGenerator = new CustomNarrativeGenerator();
        this.fhirContextR5.setNarrativeGenerator(customNarrativeGenerator);
        ehrFhirClientFactoryR5 = new EhrFhirClientFactory();
        ehrFhirClientFactoryR5.setFhirContext(fhirContextR5);
        ehrFhirClientFactoryR5.setServerValidationMode(ServerValidationModeEnum.NEVER);

        this.fhirContextR4 = fhirContextR4;
        fhirContextR4.setNarrativeGenerator(null);
        ehrFhirClientFactoryR4 = new EhrFhirClientFactory();
        ehrFhirClientFactoryR4.setFhirContext(fhirContextR4);
        ehrFhirClientFactoryR4.setServerValidationMode(ServerValidationModeEnum.NEVER);
    }


    public FhirContext fhirContext() {
        if (ProcessingFlavor.R5.isActive()) {
            return fhirContextR5;
        } else if (ProcessingFlavor.R4.isActive()) {
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

    public EhrFhirClientFactory clientFactory() {
        if (ProcessingFlavor.R5.isActive()) {
            return ehrFhirClientFactoryR5;
        } else if (ProcessingFlavor.R4.isActive()) {
            return ehrFhirClientFactoryR4;
        }
        return ehrFhirClientFactoryR5;
    }


    public IIpsWriter ipsWriter() {
        if (ProcessingFlavor.R5.isActive()) {
            return ipsWriterR5;
        } else if (ProcessingFlavor.R4.isActive()) {
            return ipsWriterR4;
        }
        return ipsWriterR5;
    }


    public IEhrEntityFhirMapper mapper(Class type) {
        initMappers();
        if (ProcessingFlavor.R5.isActive()) {
            return mappersR5.get(type);
        } else if (ProcessingFlavor.R4.isActive()) {
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
        if (ProcessingFlavor.R5.isActive()) {
            return ehrFhirServerR5;
        } else if (ProcessingFlavor.R4.isActive()) {
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

    public IFhirTransactionWriter fhirTransactionWriter() {
        if (ProcessingFlavor.R5.isActive()) {
            return fhirTransactionWriterR5;
        } else if (ProcessingFlavor.R4.isActive()) {
            return fhirTransactionWriterR4;
        }
        return fhirTransactionWriterR5;
    }

    public IBundleImportService bundleImportService() {
        if (ProcessingFlavor.R5.isActive()) {
            return bundleImportServiceR5;
        } else if (ProcessingFlavor.R4.isActive()) {
            return bundleImportServiceR4;
        }
        return bundleImportServiceR5;
    }

    public static IBaseParameters parametersObject() {
        if (ProcessingFlavor.R5.isActive()) {
            return new org.hl7.fhir.r5.model.Parameters();
        } else if (ProcessingFlavor.R4.isActive()) {
            return new org.hl7.fhir.r4.model.Parameters();
        }
        return new org.hl7.fhir.r5.model.Parameters();
    }

    public static Class parametersClass() {
        if (ProcessingFlavor.R5.isActive()) {
            return org.hl7.fhir.r5.model.Parameters.class;
        } else if (ProcessingFlavor.R4.isActive()) {
            return org.hl7.fhir.r4.model.Parameters.class;
        }
        return org.hl7.fhir.r5.model.Parameters.class;
    }

    public static Class bundleClass() {
        if (ProcessingFlavor.R5.isActive()) {
            return org.hl7.fhir.r5.model.Bundle.class;
        } else if (ProcessingFlavor.R4.isActive()) {
            return org.hl7.fhir.r4.model.Bundle.class;
        }
        return org.hl7.fhir.r5.model.Bundle.class;
    }

}
