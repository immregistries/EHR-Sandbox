package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.immregistries.ehr.fhir.annotations.OnR4Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.immregistries.ehr.logic.mapping.PatientMapperR5.FEMALE_SEX;
import static org.immregistries.ehr.logic.mapping.PatientMapperR5.MALE_SEX;

/**
 * Maps the Database with FHIR for patient resources
 */
@Service
@Conditional(OnR4Condition.class)
public class PatientMapperR4 implements IPatientMapper<Patient> {

    @Autowired()
    IOrganizationMapper organizationMapper;
    private static Logger logger = LoggerFactory.getLogger(PatientMapperR4.class);

    public static final SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");

    public Patient toFhir(EhrPatient dbPatient, String identifier_system) {
        Patient fhirPatient = toFhir(dbPatient);
        Identifier identifier = fhirPatient.addIdentifier();
        identifier.setValue("" + dbPatient.getId());
        identifier.setSystem(identifier_system);
        return fhirPatient;
    }

    public Patient toFhir(EhrPatient ehrPatient, Facility facility) {
        Patient p = toFhir(ehrPatient);
        p.setManagingOrganization(new Reference().setIdentifier(((Organization) organizationMapper.toFhir(facility)).getIdentifierFirstRep()));
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

        switch (ehrPatient.getSex()) {
            case MALE_SEX:
                p.setGender(Enumerations.AdministrativeGender.MALE);
                break;
            case FEMALE_SEX:
                p.setGender(Enumerations.AdministrativeGender.FEMALE);
                break;
            default:
                p.setGender(Enumerations.AdministrativeGender.OTHER);
                break;
        }

        //Race and ethnicity
        Extension raceExtension = p.addExtension();
        raceExtension.setUrl(RACE);
        CodeableConcept race = new CodeableConcept().setText(RACE_SYSTEM);
        raceExtension.setValue(race);
        if (ehrPatient.getRace() != null && !ehrPatient.getRace().isBlank()) {
            race.addCoding().setCode(ehrPatient.getRace());
        }
//    if (dbPatient.getRace2() != null && !dbPatient.getRace2().isBlank()) {
//      race.addCoding().setCode(dbPatient.getRace2());
//    }
//    if (dbPatient.getRace3() != null && !dbPatient.getRace3().isBlank()) {
//      race.addCoding().setCode(dbPatient.getRace3());
//    }
//    if (dbPatient.getRace4() != null && !dbPatient.getRace4().isBlank()) {
//      race.addCoding().setCode(dbPatient.getRace4());
//    }
//    if (dbPatient.getRace5() != null && !dbPatient.getRace5().isBlank()) {
//      race.addCoding().setCode(dbPatient.getRace5());
//    }
//    if (dbPatient.getRace6() != null && !dbPatient.getRace6().isBlank()) {
//      race.addCoding().setCode(dbPatient.getRace6());
//    }
        p.addExtension(ETHNICITY_EXTENSION, new Coding().setSystem(ETHNICITY_SYSTEM).setCode(ehrPatient.getEthnicity()));
        // telecom
        for (EhrPhoneNumber phoneNumber : ehrPatient.getPhones()) {
            ContactPoint contactPoint = p.addTelecom()
                    .setValue(phoneNumber.getNumber())
                    .setSystem(ContactPoint.ContactPointSystem.PHONE);
            try {
                contactPoint.setUse(ContactPoint.ContactPointUse.valueOf(phoneNumber.getType()));
            } catch (IllegalArgumentException illegalArgumentException) {
            }
        }
        if (null != ehrPatient.getEmail()) {
            p.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(ehrPatient.getEmail());
        }


        if (ehrPatient.getDeathDate() != null) {
            p.setDeceased(new DateType(ehrPatient.getDeathDate()));
        } else if (ehrPatient.getDeathFlag().equals(YES)) {
            p.setDeceased(new BooleanType(true));
        } else if (ehrPatient.getDeathFlag().equals(NO)) {
            p.setDeceased(new BooleanType(false));
        }

        p.addAddress().addLine(ehrPatient.getAddressLine1())
                .addLine(ehrPatient.getAddressLine2())
                .setCity(ehrPatient.getAddressCity())
                .setCountry(ehrPatient.getAddressCountry())
                .setState(ehrPatient.getAddressState())
                .setDistrict(ehrPatient.getAddressCountyParish())
                .setPostalCode(ehrPatient.getAddressZip());

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

        Patient.ContactComponent contact = p.addContact();
        HumanName contactName = new HumanName();
        contact.setName(contactName);
        contact.addRelationship().setText(ehrPatient.getGuardianRelationship());
        contactName.setFamily(ehrPatient.getGuardianLast());
        contactName.addGivenElement().setValue(ehrPatient.getGuardianFirst());
        contactName.addGivenElement().setValue(ehrPatient.getGuardianMiddle());
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
        switch (p.getGender()) {
            case MALE:
                ehrPatient.setSex(MALE_SEX);
                break;
            case FEMALE:
                ehrPatient.setSex(FEMALE_SEX);
                break;
            case OTHER:
            default:
                ehrPatient.setSex("");
                break;
        }
        int raceNumber = 0;
        CodeableConcept races = MappingHelper.extensionGetCodeableConcept(p.getExtensionByUrl(RACE));
        if (races != null) {
            for (Coding coding : races.getCoding()) {
                raceNumber++;
                switch (raceNumber) {
                    case 1: {
                        ehrPatient.setRace(coding.getCode());
                    }
//        case 2: {
//          patient.setRace2(coding.getCode());
//        }
//        case 3: {
//          patient.setRace3(coding.getCode());
//        }
//        case 4:{
//          patient.setRace4(coding.getCode());
//        }
//        case 5:{
//          patient.setRace5(coding.getCode());
//        }
//        case 6:{
//          patient.setRace6(coding.getCode());
//        }
                }
            }

        }
        if (p.getExtensionByUrl(ETHNICITY_EXTENSION) != null) {
            Coding ethnicity = MappingHelper.extensionGetCoding(p.getExtensionByUrl(ETHNICITY_EXTENSION));
            ehrPatient.setEthnicity(ethnicity.getCode());
        }

        for (ContactPoint telecom : p.getTelecom()) {
            if (null != telecom.getSystem()) {
                if (telecom.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
                    EhrPhoneNumber ehrPhoneNumber = new EhrPhoneNumber(telecom.getValue());
                    if (telecom.getUse() != null) {
                        ehrPhoneNumber.setType(telecom.getUse().toCode());
                    }
                    ehrPatient.addPhoneNumbers(ehrPhoneNumber);
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
        Address address = p.getAddressFirstRep();
        if (address.getLine().size() > 0) {
            ehrPatient.setAddressLine1(address.getLine().get(0).getValueNotNull());
        }
        if (address.getLine().size() > 1) {
            ehrPatient.setAddressLine2(address.getLine().get(1).getValueNotNull());
        }
        ehrPatient.setAddressCity(address.getCity());
        ehrPatient.setAddressState(address.getState());
        ehrPatient.setAddressZip(address.getPostalCode());
        ehrPatient.setAddressCountry(address.getCountry());
        ehrPatient.setAddressCountyParish(address.getDistrict());

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
            Coding value = MappingHelper.extensionGetCoding(publicity);
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
            Coding value = MappingHelper.extensionGetCoding(protection);
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
            Coding value = MappingHelper.extensionGetCoding(registry);
            ehrPatient.setRegistryStatusIndicator(value.getCode());
            if (value.getVersion() != null && !value.getVersion().isBlank()) {
                try {
                    ehrPatient.setRegistryStatusIndicatorDate(MappingHelper.sdf.parse(value.getVersion()));
                } catch (ParseException e) {
//				throw new RuntimeException(e);
                }
            }
        }
        return ehrPatient;
    }
}
