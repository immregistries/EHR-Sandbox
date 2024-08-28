package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.NextOfKin;
import org.immregistries.ehr.api.entities.NextOfKinRelationship;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.immregistries.ehr.api.entities.embedabbles.EhrRace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;


/**
 * Maps the Database with FHIR for patient resources
 */
@Service

public class PatientMapperR4 implements IPatientMapper<Patient> {

    @Autowired
    CodeMapManager codeMapManager;
    @Autowired
    MappingHelperR4 mappingHelperR4;
    @Autowired()
    OrganizationMapperR4 organizationMapper;
    @Autowired()
    PractitionerMapperR4 practitionerMapper;

    private static Logger logger = LoggerFactory.getLogger(PatientMapperR4.class);

    public Patient toFhir(EhrPatient ehrPatient, String identifier_system) {
        Patient fhirPatient = toFhir(ehrPatient);
        Identifier identifier = fhirPatient.addIdentifier();
        identifier.setValue("" + ehrPatient.getId());
        identifier.setSystem(identifier_system);
        return fhirPatient;
    }

    public Patient toFhir(EhrPatient ehrPatient, Facility facility) {
        Patient p = toFhir(ehrPatient);
        p.setManagingOrganization(new Reference().setIdentifier(organizationMapper.toFhir(facility).getIdentifierFirstRep()));
        return p;
    }

    public Patient toFhir(EhrPatient ehrPatient) {
        Patient p = new Patient();

        p.setBirthDate(ehrPatient.getBirthDate());
        if (p.getNameFirstRep() != null) {
            HumanName name = p.addName()
                    .setFamily(ehrPatient.getNameLast())
                    .addGiven(ehrPatient.getNameFirst())
                    .addGiven(ehrPatient.getNameMiddle());
//			   .setUse(HumanName.NameUse.USUAL);
        }

        Extension motherMaidenName = p.addExtension()
                .setUrl(MOTHER_MAIDEN_NAME)
                .setValue(new StringType(ehrPatient.getMotherMaiden()));

        p.setGender(MappingHelperR4.toFhirGender(ehrPatient.getSex()));

        //Race and ethnicity
        if (!ehrPatient.getRaces().isEmpty()) {
            Extension raceExtension = p.addExtension();
            raceExtension.setUrl(RACE);
            CodeableConcept race = new CodeableConcept();
            raceExtension.setValue(race);
            for (EhrRace ehrRace : ehrPatient.getRaces()) {
                race.addCoding(mappingHelperR4.codingFromCodeset(ehrRace.getValue(), RACE_SYSTEM, CodesetType.PATIENT_RACE));
            }
        }
        p.addExtension(ETHNICITY_EXTENSION, new Coding().setSystem(ETHNICITY_SYSTEM).setCode(ehrPatient.getEthnicity()));
        // telecom
        for (EhrPhoneNumber phoneNumber : ehrPatient.getPhones()) {
            p.addTelecom(MappingHelperR4.toFhirContact(phoneNumber));
        }
        if (StringUtils.isNotBlank(ehrPatient.getEmail())) {
            p.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(ehrPatient.getEmail());
        }


        if (ehrPatient.getDeathDate() != null) {
            p.setDeceased(new DateTimeType(ehrPatient.getDeathDate()));
        } else if (ehrPatient.getDeathFlag().equals(YES)) {
            p.setDeceased(new BooleanType(true));
        } else if (ehrPatient.getDeathFlag().equals(NO)) {
            p.setDeceased(new BooleanType(false));
        }

        for (EhrAddress ehrAddress : ehrPatient.getAddresses()) {
            p.addAddress(MappingHelperR4.toFhirAddress(ehrAddress));
        }

        if (ehrPatient.getBirthOrder() != null && !ehrPatient.getBirthOrder().isBlank()) {
            p.setMultipleBirth(new IntegerType().setValue(Integer.parseInt(ehrPatient.getBirthOrder())));
        } else if (ehrPatient.getBirthFlag().equals(YES)) {
            p.setMultipleBirth(new BooleanType(true));
        }

        Extension publicity = p.addExtension();
        publicity.setUrl(PUBLICITY_EXTENSION);
        Coding publicityValue = new Coding()
                .setSystem(PUBLICITY_SYSTEM)
                .setCode(ehrPatient.getPublicityIndicator());
        publicity.setValue(publicityValue);
        if (ehrPatient.getPublicityIndicatorDate() != null) {
            publicityValue.setVersion(ehrPatient.getPublicityIndicatorDate().toString());
        }

        Extension protection = p.addExtension();
        protection.setUrl(PROTECTION_EXTENSION);
        Coding protectionValue = new Coding()
                .setSystem(PROTECTION_SYSTEM)
                .setCode(ehrPatient.getProtectionIndicator());
        protection.setValue(protectionValue);
        if (ehrPatient.getProtectionIndicatorDate() != null) {
            protectionValue.setVersion(ehrPatient.getProtectionIndicatorDate().toString());
        }

        Extension registryStatus = p.addExtension();
        registryStatus.setUrl(REGISTRY_STATUS_EXTENSION);
        Coding registryValue = new Coding()
                .setSystem(REGISTRY_STATUS_INDICATOR)
                .setCode(ehrPatient.getRegistryStatusIndicator());
        registryStatus.setValue(registryValue);
        if (ehrPatient.getRegistryStatusIndicatorDate() != null) {
            registryValue.setVersion(ehrPatient.getRegistryStatusIndicatorDate().toString());
        }

        for (NextOfKinRelationship nextOfKinRelationship : ehrPatient.getNextOfKinRelationships()) {
            p.addContact(toFhirContactComponent(nextOfKinRelationship));
        }

        if (ehrPatient.getGeneralPractitioner() != null) {
            p.addGeneralPractitioner(new Reference().setIdentifier(practitionerMapper.toFhir(ehrPatient.getGeneralPractitioner()).getIdentifierFirstRep()));
        }


        return p;
    }

    public EhrPatient toEhrPatient(Patient p) {
        EhrPatient ehrPatient = new EhrPatient();
//    patient.setId(Integer.valueOf(new IdType(p.getId()).getIdPart()));
        ehrPatient.setUpdatedDate(p.getMeta().getLastUpdated());

        ehrPatient.setBirthDate(p.getBirthDate());
        // Name
        HumanName name = p.getNameFirstRep();
        ehrPatient.setNameLast(name.getFamily());
        if (name.getGiven().size() > 0) {
            ehrPatient.setNameFirst(name.getGiven().get(0).getValueNotNull());
        }
        if (name.getGiven().size() > 1) {
            ehrPatient.setNameMiddle(name.getGiven().get(1).getValueNotNull());
        }

        Extension motherMaiden = p.getExtensionByUrl(MOTHER_MAIDEN_NAME);
        if (motherMaiden != null) {
            ehrPatient.setMotherMaiden(motherMaiden.getValue().toString());
        }
        ehrPatient.setSex(MappingHelperR4.toEhrSex(p.getGender()));

        CodeableConcept races = MappingHelperR4.extensionGetCodeableConcept(p.getExtensionByUrl(RACE));
        if (races != null) {
            for (Coding coding : races.getCoding()) {
                ehrPatient.addRace(new EhrRace(coding.getCode()));
            }
        }
        if (p.getExtensionByUrl(ETHNICITY_EXTENSION) != null) {
            Coding ethnicity = MappingHelperR4.extensionGetCoding(p.getExtensionByUrl(ETHNICITY_EXTENSION));
            ehrPatient.setEthnicity(ethnicity.getCode());
        }

        for (ContactPoint telecom : p.getTelecom()) {
            if (null != telecom.getSystem()) {
                if (telecom.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
                    ehrPatient.addPhoneNumber(MappingHelperR4.toEhrPhoneNumber(telecom));
                } else if (telecom.getSystem().equals(ContactPoint.ContactPointSystem.EMAIL)) {
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
            ehrPatient.addAddress(MappingHelperR4.toEhrAddress(address));
        }

        if (null != p.getMultipleBirth()) {
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
            Coding value = MappingHelperR4.extensionGetCoding(publicity);
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
            Coding value = MappingHelperR4.extensionGetCoding(protection);
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
            Coding value = MappingHelperR4.extensionGetCoding(registry);
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
        contact.addRelationship().setText(nextOfKinRelationship.getRelationshipKind()); //TODO SYSTEM

        NextOfKin nextOfKin = nextOfKinRelationship.getNextOfKin();
        HumanName contactName = new HumanName();
        contact.setName(contactName);
        contactName.setFamily(nextOfKin.getNameLast());
        contactName.addGivenElement().setValue(nextOfKin.getNameFirst());
        contactName.addGivenElement().setValue(nextOfKin.getNameMiddle());
        contactName.addSuffix(nextOfKin.getNameSuffix());
        contact.setGender(MappingHelperR4.toFhirGender(nextOfKin.getSex()));
        for (EhrAddress ehrAddress : nextOfKin.getAddresses()) {
            contact.setAddress(MappingHelperR4.toFhirAddress(ehrAddress)); //TODO extension for multiple NK1 addresses
        }
        for (EhrPhoneNumber phoneNumber : nextOfKin.getPhoneNumbers()) {
            contact.addTelecom(MappingHelperR4.toFhirContact(phoneNumber));
        }
        if (StringUtils.isNotBlank(nextOfKin.getEmail())) {
            contact.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(nextOfKin.getEmail());
        }
        return contact;
    }


    public NextOfKinRelationship toEhrNextOfKinRelationShip(Patient.ContactComponent contact) {
        NextOfKinRelationship nextOfKinRelationship = new NextOfKinRelationship();
        nextOfKinRelationship.setRelationshipKind(contact.getRelationshipFirstRep().getText());

        NextOfKin nextOfKin = new NextOfKin();
        nextOfKinRelationship.setNextOfKin(nextOfKin);

        HumanName contactName = contact.getName();
        nextOfKin.setNameLast(contactName.getFamily());
        nextOfKin.setNameFirst(contactName.getGiven().get(0).getValueNotNull());
        if (contactName.getGiven().size() > 1) {
            nextOfKin.setNameMiddle(contactName.getGiven().get(1).getValueNotNull());
        }
        nextOfKin.setNameSuffix(contactName.getSuffixAsSingleString());
        nextOfKin.setSex(MappingHelperR4.toEhrSex(contact.getGender()));
        nextOfKin.addAddress(MappingHelperR4.toEhrAddress(contact.getAddress()));
        for (ContactPoint telecom : contact.getTelecom()) {
            switch (telecom.getSystem()) {
                case PHONE: {
                    nextOfKin.addPhoneNumber(MappingHelperR4.toEhrPhoneNumber(telecom));
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
