package org.immregistries.ehr.logic;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.logic.mapping.MappingHelper;
import org.immregistries.ehr.logic.mapping.forR5.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.forR5.OrganizationMapperR5;
import org.immregistries.ehr.logic.mapping.forR5.PatientMapperR5;
import org.immregistries.ehr.logic.mapping.forR5.PractitionerMapperR5;
import org.immregistries.ehr.logic.mapping.interfaces.IImmunizationMapper;
import org.immregistries.smm.tester.manager.HL7Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

import static org.immregistries.ehr.logic.mapping.interfaces.IPatientMapper.IDENTIFIER_TYPE_SYSTEM;

@Service
public class FhirTransactionWriterR5 implements IFhirTransactionWriter {


    public static final int PROCESSING_ID = 11;
    public static final int PROFILE_ID = 21;
    public static final String PROCESSING_ID_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0103";
    public static final String PROCESSING_MODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0207";
    @Autowired
    OrganizationMapperR5 organizationMapper;
    @Autowired
    PatientMapperR5 patientMapper;
    @Autowired
    ImmunizationMapperR5 immunizationMapper;
    @Autowired
    PractitionerMapperR5 practitionerMapper;

    @Autowired
    ResourceIdentificationService resourceIdentificationService;


    public IBaseBundle vxuBundleSingleVaccination(Facility facility, VaccinationEvent vaccinationEvent) {
        Map<Integer, String> clinicianUrlMap = new HashMap<>(facility.getTenant().getClinicians().size());

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        String organizationEntryUrl = addOrganizationEntry(bundle, facility);
        String patientEntryUrl = addPatientEntry(bundle, organizationEntryUrl, vaccinationEvent.getPatient(), clinicianUrlMap);
        String vaccinationEntryUrl = addVaccinationEntry(bundle, patientEntryUrl, vaccinationEvent, clinicianUrlMap);
        return bundle;
    }

    public IBaseBundle vxuBundleAll(Facility facility, EhrPatient ehrPatient) {
        Map<Integer, String> clinicianUrlMap = new HashMap<>(facility.getTenant().getClinicians().size());

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        String organizationEntryUrl = addOrganizationEntry(bundle, facility);
        String patientEntryUrl = addPatientEntry(bundle, organizationEntryUrl, ehrPatient, clinicianUrlMap);

        for (VaccinationEvent vaccinationEvent : ehrPatient.getVaccinationEvents()) {
            String vaccinationEntryUrl = addVaccinationEntry(bundle, patientEntryUrl, vaccinationEvent, clinicianUrlMap);
        }
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
                        .setUrl(IFhirTransactionWriter.identifierUrl(MappingHelper.ORGANIZATION, new EhrIdentifier(organization.getIdentifierFirstRep())))
                ).getFullUrl();
    }

    @Override
    public String addPatientEntry(IBaseBundle iBaseBundle, String organizationUrl, EhrPatient ehrPatient, Map<Integer, String> clinicianUrlMap) {
        Bundle bundle = (Bundle) iBaseBundle;
        Patient patient = patientMapper.toFhir(ehrPatient);
        patient.setManagingOrganization(new Reference(organizationUrl));
//            String patientRequestUrl = identifierUrl(MappingHelper.PATIENT, patient.getIdentifierFirstRep());
        String patientRequestUrl = MappingHelper.PATIENT;
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
        String immunizationRequestUrl = MappingHelper.IMMUNIZATION;
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
            String practitionerRequestUrl = MappingHelper.PRACTITIONER;
            Practitioner practitioner = practitionerMapper.toFhir(clinician);

            return bundle.addEntry()
                    .setFullUrl("urn:uuid:" + UUID.randomUUID())
                    .setResource(practitioner)
                    .setRequest(new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST).setUrl(practitionerRequestUrl)).getFullUrl();
        } else {
            return null;
        }
    }

    public IBaseBundle fromMSH(IBaseBundle iBaseBundle, String msh) {
        Bundle bundle = (Bundle) iBaseBundle;
        HL7Reader reader = new HL7Reader(msh);
        if (reader.advanceToSegment("MSH")) {
            MessageHeader messageHeader = new MessageHeader();
            messageHeader.getSource()
                    .setName(reader.getValue(SENDING_APP, 1))
                    .setSoftware(reader.getValue(SENDING_APP, 2));
            // MSH 4
//            if (reader.getValue(SENDING_NETWORK_ADDRESS).isBlank()) {
//
//            }
            {
                Identifier identifier = hdToIdentifier(reader, SENDING_FACILITY);
                messageHeader.getSender().setType("Organization").setIdentifier(identifier);
            }

            Provenance provenance = new Provenance();

            {
                Identifier identifier = hdToIdentifier(reader, RECEIVING_APPLICATION);
                messageHeader.getDestinationFirstRep()
                        .setTarget(new Reference().setType("Device").setIdentifier(identifier));
            }

            {
                Identifier identifier = hdToIdentifier(reader, RECEIVING_FACILITY);
                messageHeader.getDestinationFirstRep()
                        .setReceiver(new Reference().setType("Organization").setIdentifier(identifier));
            }

            if (!reader.getValue(7).isBlank()) {
                try {
                    bundle.setTimestamp(generateSimpleDateFormat().parse(reader.getValue(7)));
                } catch (ParseException ignored) {
                }
            }

            messageHeader.setEvent(msgToCoding(reader, 9, "http://terminology.hl7.org/CodeSystem/v2-0003"));
            messageHeader.addExtension()
                    .setUrl("msh10")
                    .setValue(new StringType(reader.getValue(10)));
//            reader.getValue(9)

            if (StringUtils.isNotBlank(reader.getValue(PROCESSING_ID, 1))) {
                messageHeader.getMeta().addTag()
                        .setCode(reader.getValue(PROCESSING_ID, 1))
                        .setSystem(PROCESSING_ID_SYSTEM);

            }
            if (StringUtils.isNotBlank(reader.getValue(PROCESSING_ID, 2))) {
                messageHeader.getMeta().addTag()
                        .setCode(reader.getValue(PROCESSING_ID, 2))
                        .setSystem(PROCESSING_MODE_SYSTEM);

            }
            // MSH 14 and 15 not mapped

            if (StringUtils.isNotBlank(reader.getValue(PROFILE_ID))) {
                for (int i = 1; i <= reader.getRepeatCount(PROFILE_ID); i++) {
                    messageHeader.addExtension("profileIdExtension", new StringType(reader.getValueRepeat(21, 1, i)));
                }
            }

            Organization organization = xonToOrganization(reader, 22);
            if (organization)


        }


    }

    private Identifier hdToIdentifier(HL7Reader reader, int index) {
        if (reader.getValue(index, 1).isBlank() && StringUtils.isNotBlank(reader.getValue(index, 2))) {
            Identifier identifier = new Identifier().setValue(reader.getValue(index, 2));
            if (StringUtils.isNotBlank(reader.getValue(index, 3))) {
                identifier.setType(new CodeableConcept().addCoding(new Coding().setCode(reader.getValue(index, 3))));
            }
            return identifier;
        }
        return new Identifier();
    }

    private Identifier hdToIdentifier(HL7Reader reader, int index, int componentNum) {
        if (reader.getValue(index, componentNum, 1).isBlank() && StringUtils.isNotBlank(reader.getValue(index, componentNum, 2))) {
            Identifier identifier = new Identifier().setValue(reader.getValue(index, componentNum, 2));
            if (StringUtils.isNotBlank(reader.getValue(index, componentNum, 3))) {
                identifier.setType(new CodeableConcept().addCoding(new Coding().setCode(reader.getValue(index, componentNum, 3))));
            }
            return identifier;
        }
        return new Identifier();
    }

    private String hdToUri(HL7Reader reader, int index, int componentNum) {
        String uri = reader.getValue(index, componentNum, 1);
        if (StringUtils.isNotBlank(uri)) {
            return uri;
        }
        uri = reader.getValue(index, componentNum, 2);
        if (StringUtils.isNotBlank(uri)) {
            return switch (reader.getValue(index, componentNum, 3)) {
                case "ISO" -> "urn:oid:" + uri;
                case "UUID" -> "urn:uuid:" + uri;
                default -> uri;
            };
        }
        return "";
    }

    private Coding msgToCoding(HL7Reader reader, int index, String system) {
        Coding coding = new Coding();
        String value = reader.getValue(index, 1);
        coding.setCode(value);
        if (system == null) {
            system = reader.getValue(index, 2);
        }
        coding.setSystem(system);
        String display = value + "^" + system + "^" + reader.getValue(index, 3);
        coding.setDisplay(display);
        return coding;
    }

    public Organization xonToOrganization(HL7Reader reader, int index) {
        Organization managingOrganization = new Organization();
        managingOrganization.setName(reader.getValue(index, 1));
        Identifier identifier = new Identifier();
        identifier.setValue(reader.getValue(index, 10));
//        identifier.setSystem(reader.getValue(index, 7));
        if (StringUtils.isBlank(identifier.getValue())) {
            identifier.setValue(reader.getValue(index, 3));
        }

        if (StringUtils.isNotBlank(reader.getValue(index, 4))) {
            identifier.addExtension().setValue(new StringType(reader.getValue(index, 4)))
                    .setUrl("http://hl7.org/fhir/StructureDefinition/identifier-checkDigit");
        }
        if (StringUtils.isNotBlank(reader.getValue(index, 5))) {
            identifier.addExtension().setValue(new StringType(reader.getValue(index, 5)))
                    .setUrl("http://hl7.org/fhir/StructureDefinition/namingsystem-checkDigit");
        }
        if (true) {
            identifier.setSystem(hdToUri(reader, index, 6));
        } else {
            Identifier assigner = new Identifier();
            if (StringUtils.isNotBlank(reader.getValue(index, 6, 1))) {

            }
            identifier.setAssigner(new Reference().setIdentifier(assigner));
        }

        if (StringUtils.isNotBlank(reader.getValue(index, 7))) {
            identifier.setType(new CodeableConcept()
                    .addCoding(new Coding()
                            .setSystem(IDENTIFIER_TYPE_SYSTEM)
                            .setCode(reader.getValue(index, 7))));
        }

        managingOrganization.addIdentifier(identifier);
        return managingOrganization;
    }

}
