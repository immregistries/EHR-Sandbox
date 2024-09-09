package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.RecommendationService;
import org.immregistries.ehr.logic.mapping.IImmunizationMapper;
import org.immregistries.ehr.logic.mapping.IPatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.immregistries.ehr.api.controllers.EhrPatientController.GOLDEN_RECORD;
import static org.immregistries.ehr.api.controllers.EhrPatientController.GOLDEN_SYSTEM_TAG;

@Service
public class MatchAndEverythingService {

    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;


    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private RecommendationService recommendationService;


    public Set<VaccinationEvent> fetchAndLoadImmunizationsFromIIS(String facilityId,
                                                                  String patientId,
                                                                  String registryId,
                                                                  Optional<Long> _since) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistry);
        Facility facility = facilityRepository.findById(facilityId).get();
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId).get();
        IPatientMapper patientMapper = fhirComponentsDispatcher.patientMapper();
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser();

        String message = parser.encodeResourceToString(patientMapper.toFhir(ehrPatient));
        String id = null;
        List<IBaseResource> matches = matchPatient(registryId, message);
        for (IBaseResource iBaseResource : matches) {
            if (iBaseResource.getMeta().getTag(GOLDEN_SYSTEM_TAG, GOLDEN_RECORD) != null) {
                id = iBaseResource.getIdElement().getIdPart();
                break;
            }
        }
//        EhrIdentifier ehrIdentifier = ehrPatient.getMrnEhrIdentifier();

//        IBaseBundle searchBaseBundle = client.search()
//                .forResource("Patient")
//                .where(Patient.IDENTIFIER.exactly().systemAndCode(ehrIdentifier.getSystem(), ehrIdentifier.getValue()))
//                .returnBundle(IBaseBundle.class).execute();
//        if (r4Flavor()) {
//
//        } else {
//            Bundle searchBundle = (Bundle) searchBaseBundle;
//            for (Bundle.BundleEntryComponent entry : searchBundle.getEntry()) {
//                if (entry.getResource().getMeta().hasTag()) { // TODO better condition to check if golden record
//                    id = new IdType(entry.getResource().getId()).getIdPart();
//                    break;
//                }
//            }
//
//        }

        if (id == null) {
            throw new RuntimeException("NO MATCH FOUND"); // TODO better exception
        }

        return everythingOperation(_since, immunizationRegistry, client, facility, ehrPatient, id);
    }

    private Set<VaccinationEvent> everythingOperation(Optional<Long> _since, ImmunizationRegistry immunizationRegistry, IGenericClient client, Facility facility, EhrPatient ehrPatient, String id) {
        IImmunizationMapper immunizationMapper = fhirComponentsDispatcher.immunizationMapper();
//        RequestDetails requestDetails = new ServletRequestDetails();
//        requestDetails.setTenantId(facility.getId()); // NOTE: THIS IS NOT A MISTAKE DON'T TOUCH

        List<IDomainResource> everything = everythingResources(_since, client, id);
        Set<VaccinationEvent> set = new HashSet<>(everything.size());

        for (IDomainResource iDomainResource : everything) {
            if (iDomainResource.fhirType().equals("Immunization")) {
                if (iDomainResource.getMeta().getTag(GOLDEN_SYSTEM_TAG, GOLDEN_RECORD) != null) {
                    VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(iDomainResource);
                    vaccinationEvent.setPatient(ehrPatient);
                    vaccinationEvent.setAdministeringFacility(facility);
                    set.add(vaccinationEvent);
                }
            }
            if (iDomainResource.fhirType().equals("ImmunizationRecommendation")) { // TODO extract this part
                ImmunizationRecommendation recommendation = (ImmunizationRecommendation) iDomainResource;
                recommendationService.saveInStore(iDomainResource, facility, ehrPatient.getId(), immunizationRegistry);
            }
        }
        return set;
    }

    private static List<IDomainResource> everythingResources(Optional<Long> _since, IGenericClient client, String id) {
        IBaseParameters in;
        if (FhirComponentsDispatcher.r4Flavor()) {
            in = new org.hl7.fhir.r4.model.Parameters()
                    .addParameter("_mdm", "true")
                    .addParameter("_type", "Immunization,ImmunizationRecommendation");
            if (_since.isPresent()) {
                ((org.hl7.fhir.r4.model.Parameters) in).addParameter("_since", String.valueOf(_since.get()));
            }
        } else {
            in = new org.hl7.fhir.r5.model.Parameters()
                    .addParameter("_mdm", "true")
                    .addParameter("_type", "Immunization,ImmunizationRecommendation");
            if (_since.isPresent()) {
                ((org.hl7.fhir.r5.model.Parameters) in).addParameter("_since", String.valueOf(_since.get()));
            }
        }

        IBaseBundle iBaseBundle = (IBaseBundle) client.operation()
                .onInstance("Patient/" + id)
                .named("$everything")
                .withParameters(in)
                .useHttpGet()
                .returnResourceType(FhirComponentsDispatcher.bundleClass()).execute();
        return FhirComponentsDispatcher.domainResourcesFromBaseBundleEntries(iBaseBundle);
    }

//
//    private static Set<IDomainResource> everythingResourcesR5(Optional<Long> _since, IGenericClient client, String id) {
//        org.hl7.fhir.r5.model.Parameters in = new org.hl7.fhir.r5.model.Parameters()
//                .addParameter("_mdm", "true")
//                .addParameter("_type", "Immunization,ImmunizationRecommendation");
//        if (_since.isPresent()) {
//            in.addParameter("_since", String.valueOf(_since.get()));
//        }
//        org.hl7.fhir.r5.model.Bundle outBundle = client.operation()
//                .onInstance("Patient/" + id)
//                .named("$everything")
//                .withParameters(in)
//                .useHttpGet()
//                .returnResourceType(org.hl7.fhir.r5.model.Bundle.class).execute();
//        return outBundle.getEntry().stream().map(bundleEntryComponent -> (IDomainResource) bundleEntryComponent.getResource()).collect(Collectors.toSet());
//    }
//
//    private static Set<IDomainResource> everythingResourcesR4(Optional<Long> _since, IGenericClient client, String id) {
//        org.hl7.fhir.r4.model.Parameters in = new org.hl7.fhir.r4.model.Parameters()
//                .addParameter("_mdm", "true")
//                .addParameter("_type", "Immunization,ImmunizationRecommendation");
//        if (_since.isPresent()) {
//            in.addParameter("_since", String.valueOf(_since.get()));
//        }
//        org.hl7.fhir.r4.model.Bundle outBundle = client.operation()
//                .onInstance("Patient/" + id)
//                .named("$everything")
//                .withParameters(in)
//                .useHttpGet()
//                .returnResourceType(org.hl7.fhir.r4.model.Bundle.class).execute();
//        return outBundle.getEntry().stream().map(bundleEntryComponent -> (IDomainResource) bundleEntryComponent.getResource()).collect(Collectors.toSet());
//    }

//    private static IBaseBundle everythingResourcesAll(Optional<Long> _since, IGenericClient client, String id) {
//        IBaseParameters in = FhirComponentsDispatcher.parametersObject();
//        Class paramClass = FhirComponentsDispatcher.parametersClass();
//        IOperationUntypedWithInputAndPartialOutput iOperation = client.operation()
//                .onInstance("Patient/" + id)
//                .named("$everything")
//                .withParameters(in)
//                .andParameter("_mdm", "true")
//                .andParameter("_type", "Immunization,ImmunizationRecommendation");
//        if (_since.isPresent()) {
//            iOperation = iOperation.andParameter("_since", String.valueOf(_since.get()));
//        }
//        org.hl7.fhir.r4.model.Bundle outBundle = client.operation()
//                .onInstance("Patient/" + id)
//                .named("$everything")
////                .withParameters(in)
//                .withParameter()
//                .useHttpGet()
//                .returnResourceType(org.hl7.fhir.r4.model.Bundle.class).execute();
//        return outBundle;
//    }

    public List<String> matchPatientIdParts(
            String registryId,
            String message) {
        return matchPatient(registryId, message)
                .stream().map(iBaseResource -> iBaseResource.getIdElement().getIdPart()).collect(Collectors.toList());
    }

    public List<IBaseResource> matchPatient(
            String registryId,
            String message) {
        IBaseBundle iBaseBundle = matchPatientOperation(registryId, message);
        return FhirComponentsDispatcher.baseResourcesFromBaseBundleEntries(iBaseBundle);
    }

    public IBaseBundle matchPatientOperation(
            String registryId,
            String message) {
        IParser parser = fhirComponentsDispatcher.parser(message);
        IBaseResource patient = parser.parseResource(message);
        return matchPatientOperation(registryId, patient);
    }

    public IBaseBundle matchPatientOperation(
            String registryId,
            IBaseResource patient) {
        return (IBaseBundle) fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistryService.getImmunizationRegistry(registryId))
                .operation().onType("Patient").named("match").withParameter(FhirComponentsDispatcher.parametersClass(), "resource", patient).returnResourceType(FhirComponentsDispatcher.bundleClass()).execute();
    }
}
