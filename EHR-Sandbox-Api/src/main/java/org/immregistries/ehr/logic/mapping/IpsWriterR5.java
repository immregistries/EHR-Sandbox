package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class IpsWriterR5 implements IIpsWriter {
    @Autowired
    OrganizationMapperR5 organizationMapperR5;
    @Autowired
    PatientMapperR5 patientMapperR5;
    @Autowired
    ImmunizationMapperR5 immunizationMapperR5;
    @Autowired
    PractitionerMapperR5 practitionerMapperR5;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;

    public Bundle ipsBundle(EhrPatient ehrPatient, Facility facility) {
        /**
         *
         * Map<ClinicianId, EntryUrl>
         */
        Map<String, Reference> addedClinicianReference = new HashMap<>(ehrPatient.getVaccinationEvents().size() * 3 + 1);
        String immunizationFacilitySystem = resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility);

        Integer entryId = 0;
        Bundle bundle = new Bundle();
        bundle.setIdentifier(ehrPatient.getMrnEhrIdentifier().toR5());
        bundle.setType(Bundle.BundleType.DOCUMENT);
        bundle.setTimestamp(new Date());
        Bundle.BundleEntryComponent organizationEntry = bundle.addEntry()
                .setFullUrl(IIpsWriter.entryUrl(entryId++))
                .setResource(ipsOrganization(facility));
        ;

        Patient patient = ipsPatient(ehrPatient);
        Bundle.BundleEntryComponent patientEntry = bundle.addEntry()
                .setFullUrl(IIpsWriter.entryUrl(entryId++))
                .setResource(patient);
        patient.setManagingOrganization(new Reference(organizationEntry.getFullUrl()));
        patient.setGeneralPractitioner(new ArrayList<>(1));
        patient.addGeneralPractitioner(addClinicianEntry(bundle, ehrPatient.getGeneralPractitioner(), addedClinicianReference));

        Reference facilityReference = new Reference(organizationEntry.getFullUrl());


        Composition composition = ipsComposition();
        Bundle.BundleEntryComponent compositionEntry = bundle.addEntry()
                .setFullUrl(IIpsWriter.entryUrl(entryId++))
                .setResource(composition);
        composition.addAuthor(facilityReference);
        composition.addAttester()
//                .setMode(Composition.CompositionAttestationMode.PERSONAL)
                .setTime(new Date())
                .setParty(facilityReference);
        composition.setCustodian(facilityReference);
        composition.addIdentifier(ehrPatient.getMrnEhrIdentifier().toR5());
        composition.addSubject(new Reference(patientEntry.getFullUrl()));

        Composition.SectionComponent sectionComponent = composition.addSection()
                .setTitle("Immunization History")
                .setCode(new CodeableConcept(new Coding().setSystem("http://loinc.org").setCode("11369-6")
//                        .setDisplay("History of Immunization Narrative")
                ));
        for (VaccinationEvent vaccinationEvent : ehrPatient.getVaccinationEvents()) {
            Immunization immunization = ipsImmunization(vaccinationEvent, immunizationFacilitySystem);
            immunization.setPatient(new Reference(patientEntry.getFullUrl()));
            Bundle.BundleEntryComponent immunizationEntry = bundle.addEntry()
                    .setFullUrl(IIpsWriter.entryUrl(entryId++))
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
        composition.setType(new CodeableConcept(new Coding().setSystem("http://loinc.org").setCode("60591-5")
//                .setDisplay( "Patient summary Document")
        ));
        composition.setDate(new Date());
        return composition;
    }

    private Patient ipsPatient(EhrPatient ehrPatient) {
        Patient patient = patientMapperR5.toFhir(ehrPatient);
        patient.setExtension(new ArrayList<>(0));
        return patient;
    }

    private Organization ipsOrganization(Facility facility) {
        Organization organization = organizationMapperR5.toFhir(facility);
        return organization;
    }


    private Practitioner ipsPractitioner(Clinician clinician) {
        Practitioner practitioner = practitionerMapperR5.toFhir(clinician);
        return practitioner;
    }


    private Immunization ipsImmunization(VaccinationEvent vaccinationEvent, String facilitySystem) {
        Immunization immunization = immunizationMapperR5.toFhir(vaccinationEvent, facilitySystem);
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
                    .setFullUrl(IIpsWriter.entryUrl())
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
            component = immunizationMapperR5.fhirPerformer(clinician, role);
            component.setActor(reference);
            immunization.addPerformer(component);
        }
        return component;
    }
}
