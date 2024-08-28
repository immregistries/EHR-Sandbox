package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IpsWriterR4 implements IIpsWriter {
    @Autowired
    OrganizationMapperR4 organizationMapperR4;
    @Autowired
    PatientMapperR4 patientMapperR4;
    @Autowired
    ImmunizationMapperR4 immunizationMapperR4;
    @Autowired
    PractitionerMapperR4 practitionerMapperR4;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;

    public Bundle ipsBundle(EhrPatient ehrPatient, Facility facility) {
        /**
         *
         * Map<ClinicianId, EntryUrl>
         */
        Map<String, Reference> addedClinicianReference = new HashMap<>(ehrPatient.getVaccinationEvents().size() * 3 + 1);
        String immunizationFacilitySystem = resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility);

        Bundle bundle = new Bundle();
        bundle.setIdentifier(ehrPatient.getMrnEhrIdentifier().toR4());
        bundle.setType(Bundle.BundleType.DOCUMENT);
        bundle.setTimestamp(new Date());
        Bundle.BundleEntryComponent organizationEntry = bundle.addEntry()
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setResource(ipsOrganization(facility));
        ;

        Patient patient = ipsPatient(ehrPatient);
        Bundle.BundleEntryComponent patientEntry = bundle.addEntry()
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setResource(patient);
        patient.setManagingOrganization(new Reference(organizationEntry.getFullUrl()));
        patient.setGeneralPractitioner(new ArrayList<>(1));
        patient.addGeneralPractitioner(addClinicianEntry(bundle, ehrPatient.getGeneralPractitioner(), addedClinicianReference));

        Reference facilityReference = new Reference(organizationEntry.getFullUrl());


        Composition composition = ipsComposition();
        Bundle.BundleEntryComponent compositionEntry = bundle.addEntry()
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setResource(composition);
        composition.addAuthor(facilityReference);
        composition.addAttester()
                .setMode(Composition.CompositionAttestationMode.PERSONAL)
                .setTime(new Date())
                .setParty(facilityReference);
        composition.setCustodian(facilityReference);
        composition.setIdentifier(ehrPatient.getMrnEhrIdentifier().toR4());
        composition.setSubject(new Reference(patientEntry.getFullUrl()));

        Composition.SectionComponent sectionComponent = composition.addSection()
                .setTitle("Immunization History")
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "11369-6", "History of Immunization Narrative")));
        for (VaccinationEvent vaccinationEvent : ehrPatient.getVaccinationEvents()) {
            Immunization immunization = ipsImmunization(vaccinationEvent, immunizationFacilitySystem);
            immunization.setPatient(new Reference(patientEntry.getFullUrl()));
            Bundle.BundleEntryComponent immunizationEntry = bundle.addEntry()
                    .setFullUrl("urn:uuid:" + UUID.randomUUID())
                    .setResource(immunization);
            immunization.setPerformer(new ArrayList<>(3));
            addImmunizationPerformer(bundle, immunization, vaccinationEvent.getOrderingClinician(), IImmunizationMapper.ORDERING, addedClinicianReference);
            addImmunizationPerformer(bundle, immunization, vaccinationEvent.getEnteringClinician(), IImmunizationMapper.ENTERING, addedClinicianReference);
            addImmunizationPerformer(bundle, immunization, vaccinationEvent.getAdministeringClinician(), IImmunizationMapper.ADMINISTERING, addedClinicianReference);
            sectionComponent.addEntry(new Reference(immunizationEntry.getFullUrl()));
        }

        return bundle;
    }

    private Composition ipsComposition() {
        Composition composition = new Composition();
        composition.setType(new CodeableConcept(new Coding("http://loinc.org", "60591-5", "Patient summary Document")));
        composition.setDate(new Date());
        return composition;
    }

    private Patient ipsPatient(EhrPatient ehrPatient) {
        Patient patient = patientMapperR4.toFhir(ehrPatient);
        patient.setExtension(new ArrayList<>(0));
        patient.setText(null);
        return patient;
    }

    private Organization ipsOrganization(Facility facility) {
        Organization organization = organizationMapperR4.toFhir(facility);
        organization.setExtension(new ArrayList<>(0));
        return organization;
    }


    private Practitioner ipsPractitioner(Clinician clinician) {
        Practitioner practitioner = practitionerMapperR4.toFhir(clinician);
        practitioner.setExtension(new ArrayList<>(0));
        return practitioner;
    }


    private Immunization ipsImmunization(VaccinationEvent vaccinationEvent, String facilitySystem) {
        Immunization immunization = immunizationMapperR4.toFhir(vaccinationEvent, facilitySystem);
        immunization.setExtension(new ArrayList<>(0));
        return immunization;
    }

    private Reference addClinicianEntry(Bundle bundle, Clinician clinician, Map<String, Reference> addedClinicianReference) {
        Reference reference = null;
        if (clinician == null) {
            return null;
        }
        reference = addedClinicianReference.get(clinician.getId());
        if (reference == null) {
            Practitioner practitioner = ipsPractitioner(clinician);
            Bundle.BundleEntryComponent clinicianEntry = bundle.addEntry()
                    .setFullUrl("urn:uuid:" + UUID.randomUUID())
                    .setResource(practitioner);
            reference = new Reference(clinicianEntry.getFullUrl());
            addedClinicianReference.put(clinician.getId(), reference);
        }
        return reference;

    }

    private Immunization.ImmunizationPerformerComponent addImmunizationPerformer(Bundle bundle, Immunization immunization, Clinician clinician, String role, Map<String, Reference> addedClinicianReference) {
        Reference reference = addClinicianEntry(bundle, clinician, addedClinicianReference);
        Immunization.ImmunizationPerformerComponent component = null;
        if (reference != null) {
            component = immunizationMapperR4.fhirPerformer(clinician, role);
            component.setActor(reference);
            immunization.addPerformer(component);
        }
        return component;
    }
}
