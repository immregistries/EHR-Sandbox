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
    @Autowired
    HL7printer hl7printer;


    /**
     * Hybrid experiemental method using different mapping methods
     *
     * @param facility         Facility/ organization writing the message
     * @param vaccinationEvent Vaccination
     * @return
     */
    public IBaseBundle vxuBundleSingleVaccination(Facility facility, VaccinationEvent vaccinationEvent) {
        Map<Integer, String> clinicianUrlMap = new HashMap<>(facility.getTenant().getClinicians().size());
        String msh = "";
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        {
            StringBuilder sb = new StringBuilder();
            hl7printer.createMSH(sb, "VXU^V04^VXU_V04", "Z22", facility);
            msh = sb.toString();
        }


        MessageHeader messageHeader = messageHeaderFromMSH(msh);
        Provenance provenance = provenanceFromMsh(msh);

        String messageHeaderEntryUrl = bundle.addEntry()
                .setResource(messageHeader)
                .setFullUrl(URN_UUID + java.util.UUID.randomUUID())
                .setRequest(new Bundle.BundleEntryRequestComponent()
                        .setMethod(Bundle.HTTPVerb.POST)
                        .setUrl("MessageHeader"))
                .getFullUrl();
        String provenanceEntryUrl = bundle.addEntry()
                .setResource(provenance)
                .setFullUrl(URN_UUID + java.util.UUID.randomUUID())
                .setRequest(new Bundle.BundleEntryRequestComponent()
                        .setMethod(Bundle.HTTPVerb.POST)
                        .setUrl("Provenance"))
                .getFullUrl();

        String organizationEntryUrl = addOrganizationEntry(bundle, facility);
        String patientEntryUrl = addPatientEntry(bundle, organizationEntryUrl, vaccinationEvent.getPatient(), clinicianUrlMap);
        String vaccinationEntryUrl = addVaccinationEntry(bundle, patientEntryUrl, vaccinationEvent, clinicianUrlMap);

        messageHeader.setResponsible(new Reference(organizationEntryUrl));
        provenance.addAgent().setWho(new Reference(organizationEntryUrl)).getType().addCoding(PROVENANCE_PARTICIPANT_TYPE_SYSTEM, PROVENANCE_AUTHOR_CODE, PROVENANCE_AUTHOR_CODE);
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
                .setFullUrl(URN_UUID + java.util.UUID.randomUUID())
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
                .setFullUrl(URN_UUID + java.util.UUID.randomUUID())
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
                .setFullUrl(URN_UUID + java.util.UUID.randomUUID())
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
                    .setFullUrl(URN_UUID + java.util.UUID.randomUUID())
                    .setResource(practitioner)
                    .setRequest(new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.POST).setUrl(practitionerRequestUrl)).getFullUrl();
        } else {
            return null;
        }
    }

    /**
     * Filling information without other entries references
     *
     * @param msh V2 message containing a MSH
     * @return Provenance
     */
    public Provenance provenanceFromMsh(String msh) {
        HL7Reader reader = new HL7Reader(msh);
        Provenance provenance = null;
        if (reader.advanceToSegment("MSH")) {
            provenance = new Provenance();
//            provenance.setActivity(new CodeableConcept().addCoding(new Coding("", "v2-FHIR transformation", "")));
//            provenance.setRecorded(new Date());
            // TODO keep implementing as https://build.fhir.org/ig/HL7/v2-to-fhir/ConceptMap-segment-msh-transformation-to-provenance.html
            Provenance.ProvenanceEntityComponent entityComponent = provenance.addEntity().setRole(Provenance.ProvenanceEntityRole.SOURCE);
//            entityComponent.setWhat(new Reference()) reference to V2 message

            if (StringUtils.isNotBlank(reader.getValue(MSH_SENDING_APP)) && StringUtils.isNotBlank(reader.getValue(MSH_SENDING_NETWORK_ADDRESS))) {
                Identifier identifier = hdToIdentifier(reader, MSH_SENDING_FACILITY);
                entityComponent.setWhat(new Reference().setIdentifier(identifier));
            }

            if (StringUtils.isBlank(reader.getValue(MSH_SENDING_RESPONSIBLE_ORGANIZATION))) { // TODO better condition as here is only checking first field
                Identifier identifier = hdToIdentifier(reader, MSH_SENDING_FACILITY);
                provenance.addAgent().setWho(new Reference().setType("Organization").setIdentifier(identifier)).getType().addCoding(new Coding(PROVENANCE_PARTICIPANT_TYPE_SYSTEM, PROVENANCE_AUTHOR_CODE, PROVENANCE_AUTHOR_CODE));
            }
            if (StringUtils.isNotBlank(reader.getValue(MSH_DATE_TIME_OF_MESSAGE))) {
                try {
                    Date date = generateSimpleDateFormat().parse(reader.getValue(MSH_DATE_TIME_OF_MESSAGE));
                    provenance.setRecorded(date);
                    provenance.setOccurred(new DateTimeType(date));
                } catch (ParseException ignored) {
                }
            }
        }
        return provenance;
    }

    /**
     * Filling information without other entries references
     *
     * @param msh V2 message containing a MSH
     * @return MessageHeader
     */
    public MessageHeader messageHeaderFromMSH(String msh) {
        HL7Reader reader = new HL7Reader(msh);
        MessageHeader messageHeader = null;
        if (reader.advanceToSegment("MSH")) {
            messageHeader = new MessageHeader();
//            MSH 3 && 24
            if (StringUtils.isBlank(reader.getValue(MSH_SENDING_NETWORK_ADDRESS))) {
                messageHeader.setSource(hdToHeaderSource(reader, MSH_SENDING_APP));
            } else {
                messageHeader.setSource(hdToHeaderSource(reader, MSH_SENDING_NETWORK_ADDRESS));
            }

            // MSH 4
//            if (reader.getValue(SENDING_NETWORK_ADDRESS).isBlank()) {
//
//            }
            { // MSH-4
                Identifier identifier = hdToIdentifier(reader, MSH_SENDING_FACILITY);
                messageHeader.setSender(new Reference().setType("Organization").setIdentifier(identifier));
//            Organization organization = xonToOrganization(reader, MSH_SENDING_RESPONSIBLE_ORGANIZATION);
//            String organizationUrn = bundle
//                    .addEntry()
//                    .setResource(organization)
//                    .setFullUrl(URN_UUID + UUID.randomUUID())
//                    .setRequest(new Bundle.BundleEntryRequestComponent()
//                            .setMethod(Bundle.HTTPVerb.PUT)
//                            .setUrl(IFhirTransactionWriter.identifierUrl(MappingHelper.ORGANIZATION, new EhrIdentifier(organization.getIdentifierFirstRep())))
//                    ).getFullUrl();
//            messageHeader.setResponsible(new Reference(organizationUrn));
            }

            { // MSH-5 & 25
                MessageHeader.MessageDestinationComponent destinationComponent = messageHeader.addDestination();

                Identifier identifier = hdToIdentifier(reader, MSH_RECEIVING_APPLICATION);
                if (identifier.hasValue()) {
                    destinationComponent
                            .setTarget(new Reference().setType("Device").setIdentifier(identifier));
                } else if (StringUtils.isNotBlank(reader.getValue(MSH_RECEIVING_NETWORK_ADDRESS))) {
                    for (int i = 1; i <= reader.getRepeatCount(MSH_PROFILE_ID); i++) {
                        messageHeader.addExtension("profileIdExtension", new StringType(reader.getValueRepeat(21, 1, i)));
                    }
                } else {
                    messageHeader.addExtension().setUrl(DATA_ABSENT_REASON_EXTENSION).setValue(new Coding().setCode(UNKNOWN));
                }


            }

            {
                Identifier identifier = hdToIdentifier(reader, MSH_RECEIVING_FACILITY);
                messageHeader.getDestinationFirstRep()
                        .setReceiver(new Reference().setType("Organization").setIdentifier(identifier));
            }

//            if (!reader.getValue(MSH_DATE_TIME_OF_MESSAGE).isBlank()) {
//                try {
//                    bundle.setTimestamp(generateSimpleDateFormat().parse(reader.getValue(MSH_DATE_TIME_OF_MESSAGE)));
//                } catch (ParseException ignored) {
//                }
//            }

            messageHeader.setEvent(msgToCoding(reader, 9, EVENT_TYPE_SYSTEM));
            messageHeader.addExtension()
                    .setUrl(MSH_10_EXTENSION)
                    .setValue(new StringType(reader.getValue(10)));
//            reader.getValue(9)

            if (StringUtils.isNotBlank(reader.getValue(MSH_PROCESSING_ID, 1))) {
                messageHeader.getMeta().addTag()
                        .setCode(reader.getValue(MSH_PROCESSING_ID, 1))
                        .setSystem(PROCESSING_ID_SYSTEM);

            }
            if (StringUtils.isNotBlank(reader.getValue(MSH_PROCESSING_ID, 2))) {
                messageHeader.getMeta().addTag()
                        .setCode(reader.getValue(MSH_PROCESSING_ID, 2))
                        .setSystem(PROCESSING_MODE_SYSTEM);

            }
            // MSH 14 and 15 not mapped

            if (StringUtils.isNotBlank(reader.getValue(MSH_PROFILE_ID))) {
                for (int i = 1; i <= reader.getRepeatCount(MSH_PROFILE_ID); i++) {
                    messageHeader.addExtension(PROFILE_ID_EXTENSION, new StringType(reader.getValueRepeat(21, 1, i)));
                }
            }


        }
        return messageHeader;
    }

    private static MessageHeader.MessageSourceComponent hdToHeaderSource(HL7Reader reader, int index) {
        MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();
        String hdNamespaceId = reader.getValue(index, 1);
        String hdUid = reader.getValue(index, 2);
        String hdType = reader.getValue(index, 3);
        switch (hdType) {
            case TYPE_ISO: {
                source.setEndpoint(new UriType(URN_OID + hdUid));
                break;
            }
            case TYPE_UUID: {
                source.setEndpoint(new UriType(URN_UUID + hdUid));
                break;
            }
            case TYPE_DNS: {
                source.setEndpoint(new UriType(URN_DNS + hdUid));
                break;
            }
            case TYPE_URI: {
                source.setEndpoint(new UriType(URN_URI + hdUid));
                break;
            }
            default: {
                if (StringUtils.isBlank(hdUid)) {
                    source.setName(hdNamespaceId);
                } else {
                    source.setName(hdNamespaceId + " - " + hdType + ':' + hdUid);
                }
                source.setSoftware(hdUid);
                source.setEndpoint(new StringType()).addExtension().setUrl(DATA_ABSENT_REASON_EXTENSION).setValue(new Coding().setCode(UNKNOWN));
                break;
            }
        }
        return source;
    }

    private Identifier hdToIdentifier(HL7Reader reader, int index) {
        String hdNamespaceId = reader.getValue(index, 1);
        String hdUid = reader.getValue(index, 2);
        String hdType = reader.getValue(index, 3);
        return hdToIdentifier(hdNamespaceId, hdUid, hdType);
    }

    private Identifier hdToIdentifier(HL7Reader reader, int index, int componentNum) {
        String hdNamespaceId = reader.getValue(index, componentNum, 1);
        String hdUid = reader.getValue(index, componentNum, 2);
        String hdType = reader.getValue(index, componentNum, 3);
        return hdToIdentifier(hdNamespaceId, hdUid, hdType);
    }

    private static Identifier hdToIdentifier(String hdNamespaceId, String hdUid, String hdType) { // TODO improve, system is unsatisfying
        Identifier identifier = new Identifier();
        if (hdNamespaceId.isBlank() && StringUtils.isNotBlank(hdUid)) {
            identifier.setValue(hdUid);
        } else {
            identifier.setValue(hdNamespaceId);
        }
        if (StringUtils.isNotBlank(hdType)) {
            identifier.setType(new CodeableConcept().addCoding(new Coding().setCode(hdType).setSystem(IDENTIFIER_TYPE_SYSTEM)));
            identifier.setSystem(switch (hdType) {
                case TYPE_ISO, TYPE_UUID -> "urn:ietf:rfc:3986";
                default -> null;
            });
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
                case TYPE_ISO -> URN_OID + uri;
                case TYPE_UUID -> URN_UUID + uri;
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
        String display = value + '^' + system + '^' + reader.getValue(index, 3);
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
                    .setUrl(IDENTIFIER_CHECK_DIGIT_SYSTEM);
        }
        if (StringUtils.isNotBlank(reader.getValue(index, 5))) {
            identifier.addExtension().setValue(new StringType(reader.getValue(index, 5)))
                    .setUrl(NAMING_SYSTEM_CHECK_DIGIT_SYSTEM);
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
