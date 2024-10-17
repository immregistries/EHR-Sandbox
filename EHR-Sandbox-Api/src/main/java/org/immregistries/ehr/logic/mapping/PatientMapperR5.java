package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.NextOfKin;
import org.immregistries.ehr.api.entities.NextOfKinRelationship;
import org.immregistries.ehr.api.entities.embedabbles.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Objects;

/**
 * Maps the Database with FHIR for patient resources
 */
@Service("patientMapperR5")
public class PatientMapperR5 implements IPatientMapper<Patient> {

    @Autowired
    MappingHelperR5 mappingHelperR5;
    private static Logger logger = LoggerFactory.getLogger(PatientMapperR5.class);

    public Patient toFhir(EhrPatient ehrPatient, Facility facility) {
        Patient p = toFhir(ehrPatient);
        p.setManagingOrganization(new Reference().setIdentifier(IOrganizationMapper.facilityIdToEhrIdentifier(facility).toR5()));
        return p;
    }

    public EhrIdentifier getPatientIdentifier(IBaseResource iBaseResource) {
        Patient patient = (Patient) iBaseResource;
        return new EhrIdentifier(patient.getIdentifierFirstRep());
    }

    public Patient toFhir(EhrPatient ehrPatient) {
        Patient p = new Patient();
//    p.setId("" + ehrPatient.getId());

        for (EhrIdentifier ehrIdentifier : ehrPatient.getIdentifiers()) {
            p.addIdentifier(ehrIdentifier.toR5());
        }

        if (Objects.nonNull(ehrPatient.getFacility())) {
            p.setManagingOrganization(new Reference().setType("Organization").setIdentifier(IOrganizationMapper.facilityIdToEhrIdentifier(ehrPatient.getFacility()).toR5())); // TODO include id ?
        }

        for (EhrHumanName name : ehrPatient.getNames()
        ) {
            p.addName(MappingHelperR5.toFhirName(name));
        }


        for (EhrAddress ehrAddress : ehrPatient.getAddresses()) {
            p.addAddress(MappingHelperR5.toFhirAddress(ehrAddress));
        }

        for (EhrPhoneNumber phoneNumber : ehrPatient.getPhones()) {
            p.addTelecom(MappingHelperR5.toFhirContact(phoneNumber));
        }

        p.addTelecom()
                .setValue(ehrPatient.getEmail())
                .setSystem(ContactPointSystem.EMAIL);

        p.setBirthDate(ehrPatient.getBirthDate());
        if (ehrPatient.getBirthOrder() != null && !ehrPatient.getBirthOrder().equals("")) {
            p.setMultipleBirth(new IntegerType().setValue(Integer.parseInt(ehrPatient.getBirthOrder())));
        } else if (ehrPatient.getBirthFlag().equals(YES)) {
            p.setMultipleBirth(new BooleanType(true));
        }
        p.setGender(MappingHelperR5.toFhirGender(ehrPatient.getSex()));

        /**
         * Race
         */
        if (!ehrPatient.getRaces().isEmpty()) {
            Extension raceExtension = p.addExtension();
            raceExtension.setUrl(RACE_EXTENSION);
//            Extension raceOmb = raceExtension.addExtension();
//            raceOmb.setUrl(RACE_EXTENSION_OMB); // TODO clarify MustSupport
            Extension raceText = raceExtension.addExtension();
            raceText.setUrl(RACE_EXTENSION_TEXT);
            StringBuilder textBuilder = new StringBuilder();
            for (EhrRace ehrRace : ehrPatient.getRaces()) {
                textBuilder.append(ehrRace.getValue()).append(" ");
                Extension raceDetailed = raceExtension.addExtension();
                raceDetailed.setUrl(RACE_EXTENSION_DETAILED);
                raceDetailed.setValue(mappingHelperR5.codingFromCodeset(ehrRace.getValue(), RACE_SYSTEM, CodesetType.PATIENT_RACE));
            }
            raceText.setValue(new StringType(textBuilder.toString()));
        }
        /**
         * Ethnicity
         */
        if (StringUtils.isNotBlank(ehrPatient.getEthnicity())) {
            Extension ethnicityExtension = p.addExtension();
            ethnicityExtension.setUrl(ETHNICITY_EXTENSION);
            Extension ethnicityText = ethnicityExtension.addExtension();
            ethnicityText.setUrl(ETHNICITY_EXTENSION_TEXT);
            ethnicityText.setValue(new StringType(ehrPatient.getEthnicity()));
            Extension ethnicityOmb = ethnicityExtension.addExtension();
            ethnicityOmb.setUrl(ETHNICITY_EXTENSION_OMB);
            ethnicityOmb.setValue((new Coding().setSystem(ETHNICITY_SYSTEM).setCode(ehrPatient.getEthnicity()))); //TODO sort if actually part of the codeSet ?
        }

        if (ehrPatient.getDeathDate() != null) {
            p.getDeceasedDateTimeType().setValue(ehrPatient.getDeathDate());
        } else if (ehrPatient.getDeathFlag().equals(YES)) {
            p.setDeceased(new BooleanType(true));
        } else if (ehrPatient.getDeathFlag().equals(NO)) {
            p.setDeceased(new BooleanType(false));
        }

        if (StringUtils.isNotBlank(ehrPatient.getPublicityIndicator())) {
            Extension publicity = p.addExtension();
            publicity.setUrl(PUBLICITY_EXTENSION);
            publicity.setValue(
                    new Coding().setSystem(PUBLICITY_SYSTEM)
                            .setCode(ehrPatient.getPublicityIndicator()));
            if (ehrPatient.getPublicityIndicatorDate() != null) {
                publicity.getValueCoding().setVersion(ehrPatient.getPublicityIndicatorDate().toString());
            }
        }

        if (StringUtils.isNotBlank(ehrPatient.getProtectionIndicator())) {
            Extension protection = p.addExtension();
            protection.setUrl(PROTECTION_EXTENSION);
            protection.setValue(
                    new Coding().setSystem(PROTECTION_SYSTEM)
                            .setCode(ehrPatient.getProtectionIndicator()));
            if (ehrPatient.getProtectionIndicatorDate() != null) {
                protection.getValueCoding().setVersion(ehrPatient.getProtectionIndicatorDate().toString());
            }
        }

        if (StringUtils.isNotBlank(ehrPatient.getRegistryStatusIndicator())) {
            Extension registryStatus = p.addExtension();
            registryStatus.setUrl(REGISTRY_STATUS_EXTENSION);
            registryStatus.setValue(
                    new Coding().setSystem(REGISTRY_STATUS_INDICATOR)
                            .setCode(ehrPatient.getRegistryStatusIndicator()));
            if (ehrPatient.getRegistryStatusIndicatorDate() != null) {
                registryStatus.getValueCoding().setVersion(ehrPatient.getRegistryStatusIndicatorDate().toString());
            }
        }

        for (NextOfKinRelationship nextOfKinRelationship : ehrPatient.getNextOfKinRelationships()) {
            p.addContact(toFhirContactComponent(nextOfKinRelationship));
        }

        if (ehrPatient.getGeneralPractitioner() != null) {
            p.addGeneralPractitioner(new Reference().setIdentifier(IPractitionerMapper.clinicianEhrIdentifier(ehrPatient.getGeneralPractitioner()).toR5()));
        }

        return p;
    }

    public EhrPatient toEhrPatient(Patient p) {
        EhrPatient ehrPatient = new EhrPatient();
        // Identifiers are dealt with in the providers

        ehrPatient.setUpdatedDate(p.getMeta().getLastUpdated());

        ehrPatient.setBirthDate(p.getBirthDate());
        // Name
        for (HumanName humanName : p.getName()) {
            ehrPatient.addName(MappingHelperR5.toEhrName(humanName));
        }

        for (Identifier identifier : p.getIdentifier()) {
            EhrIdentifier ehrIdentifier = new EhrIdentifier(identifier);
            ehrPatient.getIdentifiers().add(ehrIdentifier);
        }

        Extension motherMaiden = p.getExtensionByUrl(MOTHER_MAIDEN_NAME_EXTENSION);
        if (motherMaiden != null) {
            ehrPatient.setMotherMaiden(motherMaiden.getValue().toString());
        }
        ehrPatient.setSex(MappingHelperR5.toEhrSex(p.getGender()));

        Extension races = p.getExtensionByUrl(RACE_EXTENSION);
        if (races != null) {
            for (Extension ext : races.getExtensionsByUrl(RACE_EXTENSION_OMB)) {
                ehrPatient.addRace(new EhrRace(ext.getValueCoding().getCode()));
            }
            for (Extension ext : races.getExtensionsByUrl(RACE_EXTENSION_DETAILED)) {
                ehrPatient.addRace(new EhrRace(ext.getValueCoding().getCode()));
            }
        }
        Extension ethnicityExtension = p.getExtensionByUrl(ETHNICITY_EXTENSION);
        if (ethnicityExtension != null) {
            Extension ethnicityDetailed = ethnicityExtension.getExtensionByUrl(ETHNICITY_EXTENSION_DETAILED);
            Extension ethnicityOmb = ethnicityExtension.getExtensionByUrl(ETHNICITY_EXTENSION_OMB);
            /**
             * By default takes Omb value
             */
            if (ethnicityOmb != null) {
                ehrPatient.setEthnicity(ethnicityOmb.getValueCoding().getCode());
            } else if (ethnicityDetailed != null) {
                ehrPatient.setEthnicity(ethnicityDetailed.getValueCoding().getCode());
            }
        }

        for (ContactPoint telecom : p.getTelecom()) {
            if (null != telecom.getSystem()) {
                if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {
                    ehrPatient.addPhoneNumber(MappingHelperR5.toEhrPhoneNumber(telecom));
                } else if (telecom.getSystem().equals(ContactPointSystem.EMAIL)) {
                    ehrPatient.setEmail(telecom.getValue());
                }
            }
        }

        if (null != p.getDeceased()) {
            if (p.getDeceased().isBooleanPrimitive()) {
                if (p.getDeceasedBooleanType().booleanValue()) {
                    ehrPatient.setDeathFlag(YES);
                } else {
                    ehrPatient.setDeathFlag(NO);
                }
            }
            if (p.getDeceased().isDateTime()) {
                ehrPatient.setDeathDate(p.getDeceasedDateTimeType().getValue());
            }
        }
        // Address
        for (Address address : p.getAddress()) {
            ehrPatient.addAddress(MappingHelperR5.toEhrAddress(address));
        }

        if (null != p.getMultipleBirth() && !p.getMultipleBirth().isEmpty()) {
            if (p.getMultipleBirth().isBooleanPrimitive()) {
                if (p.getMultipleBirthBooleanType().booleanValue()) {
                    ehrPatient.setBirthFlag(YES);
                } else {
                    ehrPatient.setBirthFlag(NO);
                }
            } else {
                ehrPatient.setBirthOrder(String.valueOf(p.getMultipleBirthIntegerType()));
            }
        }

        Extension publicity = p.getExtensionByUrl(PUBLICITY_EXTENSION);
        if (publicity != null) {
            Coding value = publicity.getValueCoding();
            ehrPatient.setPublicityIndicator(value.getCode());
            if (value.getVersion() != null && !value.getVersion().isBlank()) {
                try {
                    ehrPatient.setPublicityIndicatorDate(MappingHelper.sdf.parse(value.getVersion()));
                } catch (ParseException e) {
//					throw new RuntimeException(e);
                }
            }
        }
        Extension protection = p.getExtensionByUrl(PROTECTION_EXTENSION);
        if (protection != null) {
            Coding value = protection.getValueCoding();
            ehrPatient.setProtectionIndicator(value.getCode());
            if (value.getVersion() != null && !value.getVersion().isBlank()) {
                try {
                    ehrPatient.setProtectionIndicatorDate(MappingHelper.sdf.parse(value.getVersion()));
                } catch (ParseException e) {
//					throw new RuntimeException(e);
                }
            }
        }
        Extension registry = p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION);
        if (registry != null) {
            Coding value = registry.getValueCoding();
            ehrPatient.setRegistryStatusIndicator(value.getCode());
            if (value.getVersion() != null && !value.getVersion().isBlank()) {
                try {
                    ehrPatient.setRegistryStatusIndicatorDate(MappingHelper.sdf.parse(value.getVersion()));
                } catch (ParseException e) {
//				throw new RuntimeException(e);
                }
            }
        }

        for (Patient.ContactComponent contact : p.getContact()) {
            ehrPatient.addNextOfKinRelationship(toEhrNextOfKinRelationShip(contact));
        }

        return ehrPatient;
    }


    public Patient.ContactComponent toFhirContactComponent(NextOfKinRelationship nextOfKinRelationship) {
        Patient.ContactComponent contact = new Patient.ContactComponent();
        //TODO SYSTEM
        contact.addRelationship().addCoding(mappingHelperR5.codingFromCodeset(nextOfKinRelationship.getRelationshipKind(), "", CodesetType.PERSON_RELATIONSHIP));

        NextOfKin nextOfKin = nextOfKinRelationship.getNextOfKin();
        HumanName contactName = new HumanName();
        contact.setName(contactName);
        contactName.setFamily(nextOfKin.getNameLast());
        contactName.addGivenElement().setValue(nextOfKin.getNameFirst());
        contactName.addGivenElement().setValue(nextOfKin.getNameMiddle());
        contactName.addSuffix(nextOfKin.getNameSuffix());
        contact.setGender(MappingHelperR5.toFhirGender(nextOfKin.getSex()));
        for (EhrAddress ehrAddress : nextOfKin.getAddresses()) {
            contact.setAddress(MappingHelperR5.toFhirAddress(ehrAddress)); //TODO extension for multiple NK1 addresses
        }
        for (EhrPhoneNumber phoneNumber : nextOfKin.getPhoneNumbers()) {
            contact.addTelecom(MappingHelperR5.toFhirContact(phoneNumber));
        }
        if (StringUtils.isNotBlank(nextOfKin.getEmail())) {
            contact.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(nextOfKin.getEmail());
        }
        return contact;
    }


    public NextOfKinRelationship toEhrNextOfKinRelationShip(Patient.ContactComponent contact) {
        NextOfKinRelationship nextOfKinRelationship = new NextOfKinRelationship();
        nextOfKinRelationship.setRelationshipKind(contact.getRelationshipFirstRep().getCodingFirstRep().getCode());

        NextOfKin nextOfKin = new NextOfKin();
        nextOfKinRelationship.setNextOfKin(nextOfKin);

        HumanName contactName = contact.getName();
        nextOfKin.setNameLast(contactName.getFamily());
        nextOfKin.setNameFirst(contactName.getGiven().get(0).getValueNotNull());
        if (contactName.getGiven().size() > 1) {
            nextOfKin.setNameMiddle(contactName.getGiven().get(1).getValueNotNull());
        }
        nextOfKin.setNameSuffix(contactName.getSuffixAsSingleString());
        nextOfKin.setSex(MappingHelperR5.toEhrSex(contact.getGender()));
        nextOfKin.addAddress(MappingHelperR5.toEhrAddress(contact.getAddress()));
        for (ContactPoint telecom : contact.getTelecom()) {
            switch (telecom.getSystem()) {
                case PHONE: {
                    nextOfKin.addPhoneNumber(MappingHelperR5.toEhrPhoneNumber(telecom));
                    break;
                }
                case EMAIL: {
                    nextOfKin.setEmail(telecom.getValue());
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return nextOfKinRelationship;
    }


}
