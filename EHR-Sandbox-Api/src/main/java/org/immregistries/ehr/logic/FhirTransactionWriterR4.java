package org.immregistries.ehr.logic;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.logic.mapping.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FhirTransactionWriterR4 implements IFhirTransactionWriter {

    @Autowired
    OrganizationMapperR4 organizationMapper;
    @Autowired
    PatientMapperR4 patientMapper;
    @Autowired
    ImmunizationMapperR4 immunizationMapper;
    @Autowired
    PractitionerMapperR4 practitionerMapper;

    @Autowired
    ResourceIdentificationService resourceIdentificationService;


    public IBaseBundle vxuBundle(Facility facility, VaccinationEvent vaccinationEvent) {
        Map<Integer, String> clinicianUrlMap = new HashMap<>(facility.getTenant().getClinicians().size());

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        String organizationEntryUrl = addOrganizationEntry(bundle, facility);
        String patientEntryUrl = addPatientEntry(bundle, organizationEntryUrl, vaccinationEvent.getPatient(), clinicianUrlMap);
        String vaccinationEntryUrl = addVaccinationEntry(bundle, patientEntryUrl, vaccinationEvent, clinicianUrlMap);
        return bundle;
    }

    public IBaseBundle qpdBundle(Facility facility, EhrPatient ehrPatient) {
        Map<Integer, String> clinicianUrlMap = new HashMap<>(facility.getTenant().getClinicians().size());

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        String organizationEntryUrl = addOrganizationEntry(bundle, facility);
        String patientEntryUrl = addPatientEntry(bundle, organizationEntryUrl, ehrPatient, clinicianUrlMap);
        return bundle;
    }

    @Override
    public Bundle transactionBundle(Facility facility) {
        // Map<clinicianId, entryUrl>
        Map<Integer, String> clinicianUrlMap = new HashMap<>(facility.getTenant().getClinicians().size());
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        String organizationEntryUrl = addOrganizationEntry(bundle, facility);
        for (EhrPatient ehrPatient : facility.getPatients()) {
            String patientEntryUrl = addPatientEntry(bundle, organizationEntryUrl, ehrPatient, clinicianUrlMap);
            for (VaccinationEvent vaccinationEvent : ehrPatient.getVaccinationEvents()) {
                String immunizationEntryUrl = addVaccinationEntry(bundle, patientEntryUrl, vaccinationEvent, clinicianUrlMap);

            }
        }
        return bundle;
    }

    @Override
    public String addOrganizationEntry(IBaseBundle iBaseBundle, Facility facility) {
        Bundle bundle = (Bundle) iBaseBundle;
        Organization organization = organizationMapper.toFhir(facility);
        return bundle.addEntry().setResource(organization)
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setRequest(new Bundle.BundleEntryRequestComponent()
                        .setMethod(Bundle.HTTPVerb.PUT)
                        .setUrl(IFhirTransactionWriter.identifierUrl("Organization", new EhrIdentifier(organization.getIdentifierFirstRep())))
                ).getFullUrl();
    }

    @Override
    public String addPatientEntry(IBaseBundle iBaseBundle, String organizationUrl, EhrPatient ehrPatient, Map<Integer, String> clinicianUrlMap) {
        Bundle bundle = (Bundle) iBaseBundle;
        Patient patient = patientMapper.toFhir(ehrPatient);
        patient.setManagingOrganization(new Reference(organizationUrl));
//            String patientRequestUrl = identifierUrl("Patient", patient.getIdentifierFirstRep());
        String patientRequestUrl = "Patient";
        Bundle.BundleEntryComponent patientEntry = bundle.addEntry()
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setResource(patient)
                .setRequest(new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST).setUrl(patientRequestUrl));
        String clinicianUrl = addOrGetClinicianEntry(bundle, ehrPatient.getGeneralPractitioner(), clinicianUrlMap);
        patient.setGeneralPractitioner(new ArrayList<>(1)).addGeneralPractitioner(new Reference(clinicianUrl));
        return patientEntry.getFullUrl();
    }

    @Override
    public String addVaccinationEntry(IBaseBundle iBaseBundle, String patientUrl, VaccinationEvent vaccinationEvent, Map<Integer, String> clinicianUrlMap) {
        Bundle bundle = (Bundle) iBaseBundle;

        Immunization immunization = immunizationMapper.toFhir(vaccinationEvent,
                resourceIdentificationService.getFacilityImmunizationIdentifierSystem(vaccinationEvent.getAdministeringFacility()));
        immunization.setPatient(new Reference(patientUrl));
        String immunizationRequestUrl = "Immunization";
        Bundle.BundleEntryComponent entryComponent = bundle.addEntry()
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setResource(immunization)
                .setRequest(new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST).setUrl(immunizationRequestUrl));
        addImmunizationPerformer(bundle, immunization, vaccinationEvent.getOrderingClinician(), IImmunizationMapper.ORDERING, clinicianUrlMap);
        addImmunizationPerformer(bundle, immunization, vaccinationEvent.getEnteringClinician(), IImmunizationMapper.ENTERING, clinicianUrlMap);
        addImmunizationPerformer(bundle, immunization, vaccinationEvent.getAdministeringClinician(), IImmunizationMapper.ADMINISTERING, clinicianUrlMap);

        return entryComponent.getFullUrl();
    }

    private void addImmunizationPerformer(IBaseBundle bundle, Immunization immunization, Clinician clinician, String role, Map<Integer, String> clinicianUrlMap) {
        String clinicianEntryUrl = addOrGetClinicianEntry(bundle, clinician, clinicianUrlMap);
        Immunization.ImmunizationPerformerComponent component;
        if (StringUtils.isNotBlank(clinicianEntryUrl)) {
            component = immunizationMapper.fhirPerformer(clinician, role);
            component.setActor(new Reference(clinicianEntryUrl));
            immunization.addPerformer(component);
        }
    }

    public String addClinicianEntry(IBaseBundle iBaseBundle, Clinician clinician) {
        Bundle bundle = (Bundle) iBaseBundle;

        if (Objects.nonNull(clinician)) {
            String practitionerRequestUrl = "Practitioner";
            Practitioner practitioner = practitionerMapper.toFhir(clinician);

            return bundle.addEntry()
                    .setFullUrl("urn:uuid:" + UUID.randomUUID())
                    .setResource(practitioner)
                    .setRequest(new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST).setUrl(practitionerRequestUrl)).getFullUrl();
        } else {
            return null;
        }
    }

}
