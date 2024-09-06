package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.RecommendationService;
import org.immregistries.ehr.logic.mapping.IImmunizationMapper;
import org.immregistries.ehr.logic.mapping.IPatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    private RandomGenerator randomGenerator;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private RecommendationService recommendationService;


    public Set<VaccinationEvent> fetchAndLoadImmunizationsFromIIS(@PathVariable() String tenantId,
                                                                  @PathVariable() String facilityId,
                                                                  @PathVariable() String patientId,
                                                                  @PathVariable() String registryId,
                                                                  @RequestParam Optional<Long> _since) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow();
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistry);
        Facility facility = facilityRepository.findById(facilityId).get();
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId).get();
        IImmunizationMapper immunizationMapper = fhirComponentsDispatcher.immunizationMapper();
        IPatientMapper patientMapper = fhirComponentsDispatcher.patientMapper();
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser();

        String message = parser.encodeResourceToString(patientMapper.toFhir(ehrPatient));
        String id = null;
        List<IBaseResource> matches = matchPatient(tenant, registryId, message);
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
//        if (facility.getTenant().getNameDisplay().contains("R4")) {
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


        RequestDetails requestDetails = new ServletRequestDetails();
        requestDetails.setTenantId(facility.getId()); // NOTE: THIS IS NOT A MISTAKE DON'T TOUCH


        Set<IDomainResource> everything = everythingResources(facility.getTenant(), _since, client, id);
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

    private static Set<IDomainResource> everythingResourcesR5(Optional<Long> _since, IGenericClient client, String id) {
        org.hl7.fhir.r5.model.Parameters in = new org.hl7.fhir.r5.model.Parameters()
                .addParameter("_mdm", "true")
                .addParameter("_type", "Immunization,ImmunizationRecommendation");
        if (_since.isPresent()) {
            in.addParameter("_since", String.valueOf(_since.get()));
        }
        org.hl7.fhir.r5.model.Bundle outBundle = client.operation()
                .onInstance("Patient/" + id)
                .named("$everything")
                .withParameters(in)
                .prettyPrint()
                .useHttpGet()
                .returnResourceType(org.hl7.fhir.r5.model.Bundle.class).execute();
        return outBundle.getEntry().stream().map(bundleEntryComponent -> (IDomainResource) bundleEntryComponent.getResource()).collect(Collectors.toSet());
    }

    private static Set<IDomainResource> everythingResources(Tenant tenant, Optional<Long> _since, IGenericClient client, String id) {
        if (tenant.getNameDisplay().contains("R4")) {
            return everythingResourcesR4(_since, client, id);
        } else {
            return everythingResourcesR5(_since, client, id);
        }
    }

    private static Set<IDomainResource> everythingResourcesR4(Optional<Long> _since, IGenericClient client, String id) {
        org.hl7.fhir.r4.model.Parameters in = new org.hl7.fhir.r4.model.Parameters()
                .addParameter("_mdm", "true")
                .addParameter("_type", "Immunization,ImmunizationRecommendation");
        if (_since.isPresent()) {
            in.addParameter("_since", String.valueOf(_since.get()));
        }
        org.hl7.fhir.r4.model.Bundle outBundle = client.operation()
                .onInstance("Patient/" + id)
                .named("$everything")
                .withParameters(in)
                .prettyPrint()
                .useHttpGet()
                .returnResourceType(org.hl7.fhir.r4.model.Bundle.class).execute();
        return outBundle.getEntry().stream().map(bundleEntryComponent -> (IDomainResource) bundleEntryComponent.getResource()).collect(Collectors.toSet());
    }

    public List<String> matchPatientIdParts(
            String tenantId,
            String registryId,
            String message) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow();
        return matchPatient(tenant, registryId, message)
                .stream().map(iBaseResource -> iBaseResource.getIdElement().getIdPart()).collect(Collectors.toList());
    }

    public List<IBaseResource> matchPatient(
            Tenant tenant,
            String registryId,
            String message) {
        if (tenant.getNameDisplay().contains("R4")) {
            org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) matchPatientOperation(registryId, message);
            return
                    bundle.getEntry().stream().filter(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent::hasResource).map(bundleEntryComponent -> bundleEntryComponent.getResource()).collect(Collectors.toList()
                    );
        } else {
            org.hl7.fhir.r5.model.Bundle bundle = (org.hl7.fhir.r5.model.Bundle) matchPatientOperation(registryId, message);
            return bundle.getEntry().stream().filter(org.hl7.fhir.r5.model.Bundle.BundleEntryComponent::hasResource).map(bundleEntryComponent -> bundleEntryComponent.getResource()).collect(Collectors.toList()
            );
        }

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
        IBaseParameters parameters = new org.hl7.fhir.r5.model.Parameters();
        return fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistryService.getImmunizationRegistry(registryId))
                .operation().onType("Patient").named("match").withParameters(parameters).andParameter("resource", patient).returnResourceType(IBaseBundle.class).execute();
    }
}
