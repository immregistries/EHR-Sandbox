package org.immregistries.ehr.logic;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.ImmunizationIdentifierRepository;
import org.immregistries.ehr.api.repositories.PatientIdentifierRepository;
import org.immregistries.ehr.fhir.Server.ServerR4.ImmunizationProviderR4;
import org.immregistries.ehr.fhir.Server.ServerR4.PatientProviderR4;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR4;
import org.immregistries.ehr.logic.mapping.PatientMapperR4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.immregistries.ehr.api.controllers.EhrPatientController.GOLDEN_RECORD;
import static org.immregistries.ehr.api.controllers.EhrPatientController.GOLDEN_SYSTEM_TAG;

@Service()
public class BundleImportServiceR4 implements IBundleImportService {
    private static final Logger logger = LoggerFactory.getLogger(BundleImportServiceR4.class);


    @Autowired
    @Qualifier("fhirContextR4")
    FhirContext fhirContextR4;

    @Autowired
    PatientProviderR4 patientProvider;
    @Autowired
    ImmunizationProviderR4 immunizationProvider;

    @Autowired
    ImmunizationMapperR4 immunizationMapper;
    @Autowired
    PatientMapperR4 patientMapper;

    @Autowired
    ImmunizationIdentifierRepository immunizationIdentifierRepository;
    @Autowired
    PatientIdentifierRepository patientIdentifierRepository;
    @Autowired
    EhrPatientRepository ehrPatientRepository;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;

    @Override
    public ResponseEntity<String> importBundle(ImmunizationRegistry immunizationRegistry, Facility facility, IBaseBundle iBaseBundle) {
        Bundle bundle = (Bundle) iBaseBundle;
        StringBuilder responseBuilder = new StringBuilder();
        int count = 0;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            switch (entry.getResource().getResourceType()) {
                case Patient: {
                    Patient patient = (Patient) entry.getResource();
                    String receivedId = new IdType(patient.getId()).getIdPart();
                    Integer localPatientId = resourceIdentificationService.getLocalPatientId(patient, immunizationRegistry, facility);

                    MethodOutcome methodOutcome;
                    methodOutcome = patientProvider.update(patient, facility, immunizationRegistry);
                    Integer dbId = EhrUtils.convert(methodOutcome.getId().getValue());
                    if (localPatientId == null) {
                        patientIdentifierRepository.save(new PatientExternalIdentifier(dbId, immunizationRegistry.getId(), receivedId));
                    }
                    responseBuilder.append("\nPatient ").append(receivedId).append(" loaded as patient ").append(dbId);
                    logger.info("Patient  {}  loaded as patient  {}", receivedId, dbId);
                    count++;
                    break;
                }
                case Immunization: {
                    Immunization immunization = (Immunization) entry.getResource();
                    String receivedId = new IdType(immunization.getId()).getIdPart();
                    Integer localPatientId = resourceIdentificationService.getLocalPatientId(immunization.getPatient(), immunizationRegistry, facility);
                    if (Objects.nonNull(localPatientId)) {
                        immunization.setPatient(new Reference("Patient/" + localPatientId));
                        MethodOutcome methodOutcome = immunizationProvider.create(immunization, facility);
                        Integer dbId = EhrUtils.convert(methodOutcome.getId().getValue());
                        immunizationIdentifierRepository.save(new ImmunizationIdentifier(dbId, immunizationRegistry.getId(), receivedId));
                        responseBuilder.append("\nImmunization ").append(receivedId).append(" loaded as Immunization ").append(dbId);
                        logger.info("Immunization {} loaded as Immunization {}", receivedId, dbId);
                        count++;
                    } else {
                        responseBuilder.append("\nERROR : ").append(immunization.getPatient().getReference()).append(" Unknown");
                        logger.info("ERROR : Patient  {}  Unknown", immunization.getPatient().getReference());
                    }
                    break;
                }
            }
        }
        responseBuilder.append("\nNumber of successful load in facility ").append(facility.getNameDisplay()).append(": ").append(count);
        return ResponseEntity.ok(responseBuilder.toString());
    }


    @Override
    public Set<EhrEntity> viewBundleAndMatchIdentifiers(ImmunizationRegistry immunizationRegistry, Facility facility, IBaseBundle iBaseBundle, Boolean includeOnlyGolden) {
        Bundle bundle = (Bundle) iBaseBundle;

        Set<EhrEntity> entities = new HashSet<>(bundle.getEntry().size());
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            switch (entry.getResource().getResourceType()) {
                case Patient: {
                    Patient patient = (Patient) entry.getResource();
                    String receivedId = new IdType(patient.getId()).getIdPart();
                    Integer localPatientId = resourceIdentificationService.getLocalPatientId(patient, immunizationRegistry, facility);
                    patientIdentifierRepository.save(new PatientExternalIdentifier(localPatientId, immunizationRegistry.getId(), receivedId));

                    if (includeOnlyGolden && patient.getMeta().getTag(GOLDEN_SYSTEM_TAG, GOLDEN_RECORD) == null) {
                        break;
                    }
                    EhrPatient ehrPatient = patientMapper.toEhrPatient(patient);
                    ehrPatient.setFacility(facility);
                    ehrPatient.setUpdatedDate(new Date());
                    entities.add(ehrPatient);
                    break;
                }
                case Immunization: {
                    Immunization immunization = (Immunization) entry.getResource();
                    String receivedId = new IdType(immunization.getId()).getIdPart();
                    Integer localPatientId = resourceIdentificationService.getLocalPatientId(immunization.getPatient(), immunizationRegistry, facility);
                    if (includeOnlyGolden && immunization.getMeta().getTag(GOLDEN_SYSTEM_TAG, GOLDEN_RECORD) == null) {
                        break;
                    }
                    if (localPatientId != null) {
                        immunization.setPatient(new Reference("Patient/" + localPatientId));
                        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(facility, immunization);
                        vaccinationEvent.setPatient(ehrPatientRepository.findByFacilityIdAndId(facility.getId(), localPatientId).orElseThrow());
                        Integer localVaccinationId = resourceIdentificationService.getImmunizationLocalId(immunization, immunizationRegistry, facility);
                        immunizationIdentifierRepository.save(new ImmunizationIdentifier(localVaccinationId, immunizationRegistry.getId(), receivedId));
                        entities.add(vaccinationEvent);
                    } else {
                        logger.info("ERROR : Patient  {}  Unknown", immunization.getPatient().getReference());
                    }
                    break;
                }
            }
        }
        return entities;
    }

    public List<IBaseResource> baseResourcesFromBaseBundleEntries(String resource) {
        Bundle bundle = fhirContextR4.newJsonParser().parseResource(Bundle.class, resource);
        return baseResourcesFromBaseBundleEntries(bundle);
    }

    public List<IDomainResource> domainResourcesFromBaseBundleEntries(String resource) {
        Bundle bundle = fhirContextR4.newJsonParser().parseResource(Bundle.class, resource);
        return domainResourcesFromBaseBundleEntries(bundle);
    }

    public List<IBaseResource> baseResourcesFromBaseBundleEntries(IBaseBundle iBaseBundle) {
        org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) iBaseBundle;
        return bundle.getEntry().stream().filter(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent::hasResource).map(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent::getResource).collect(Collectors.toList());
    }

    public List<IDomainResource> domainResourcesFromBaseBundleEntries(IBaseBundle iBaseBundle) {
        org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) iBaseBundle;
        return bundle.getEntry().stream().filter(org.hl7.fhir.r4.model.Bundle.BundleEntryComponent::hasResource).map(bundleEntryComponent -> (IDomainResource) bundleEntryComponent.getResource()).collect(Collectors.toList());
    }
}
