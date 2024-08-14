package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.NextOfKin;
import org.immregistries.ehr.api.entities.NextOfKinRelationship;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.immregistries.ehr.api.entities.embedabbles.EhrRace;
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
    CodeMapManager codeMapManager;
    @Autowired
    OrganizationMapperR5 organizationMapperR5;
    private static Logger logger = LoggerFactory.getLogger(PatientMapperR5.class);

    public Patient toFhir(EhrPatient ehrPatient, Facility facility) {
        Patient p = toFhir(ehrPatient);
        p.setManagingOrganization(new Reference().setIdentifier(organizationMapperR5.toFhir(facility).getIdentifierFirstRep()));
        return p;
    }

    public Patient toFhir(EhrPatient ehrPatient) {
        Patient p = new Patient();
//    p.setId("" + ehrPatient.getId());
        EhrIdentifier mrnIdentifier = ehrPatient.getMrnEhrIdentifier();
        if (mrnIdentifier != null) {
            p.addIdentifier(mrnIdentifier.toR5());
        }

        if (Objects.nonNull(ehrPatient.getFacility())) {
            p.setManagingOrganization(organizationMapperR5.facilityReference(ehrPatient.getFacility()));
        }

        p.addName()
                .addGiven(ehrPatient.getNameFirst())
                .addGiven(ehrPatient.getNameMiddle())
                .setFamily(ehrPatient.getNameLast());

        for (EhrAddress ehrAddress : ehrPatient.getAddresses()) {
            p.addAddress()
                    .addLine(ehrAddress.getAddressLine1())
                    .addLine(ehrAddress.getAddressLine2())
                    .setCity(ehrAddress.getAddressCity())
                    .setCountry(ehrAddress.getAddressCountry())
                    .setState(ehrAddress.getAddressState())
                    .setPostalCode(ehrAddress.getAddressZip());
        }


        for (EhrPhoneNumber phoneNumber : ehrPatient.getPhones()) {
            ContactPoint contactPoint = p.addTelecom()
                    .setValue(phoneNumber.getNumber())
                    .setSystem(ContactPointSystem.PHONE);
            try {
                if (StringUtils.isNotBlank(phoneNumber.getType())) {
                    contactPoint.setUse(ContactPoint.ContactPointUse.valueOf(phoneNumber.getType()));
                }
            } catch (IllegalArgumentException illegalArgumentException) {
            }
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
        p.setGender(toFhirGender(ehrPatient.getSex()));

        /**
         * Race
         */
        if (!ehrPatient.getRaces().isEmpty()) {
            Extension raceExtension = p.addExtension();
            raceExtension.setUrl(RACE);
            CodeableConcept race = new CodeableConcept();
            raceExtension.setValue(race);
            for (EhrRace ehrRace : ehrPatient.getRaces()) {
                race.addCoding(codingFromCodeset(ehrRace.getValue(), RACE_SYSTEM, CodesetType.PATIENT_RACE));
            }
        }


        /**
         * Ethnicity
         */
        if (StringUtils.isNotBlank(ehrPatient.getEthnicity())) {
            p.addExtension(ETHNICITY_EXTENSION, codingFromCodeset(ehrPatient.getEthnicity(), ETHNICITY_SYSTEM, CodesetType.PATIENT_ETHNICITY));
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

        return p;
    }

    public EhrPatient toEhrPatient(Patient p) {
        EhrPatient ehrPatient = new EhrPatient();
        // Identifiers are dealt with in the providers

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

        for (Identifier identifier : p.getIdentifier()) {
            EhrIdentifier ehrIdentifier = new EhrIdentifier(identifier);
            ehrPatient.getIdentifiers().add(ehrIdentifier);
        }

        Extension motherMaiden = p.getExtensionByUrl(MOTHER_MAIDEN_NAME);
        if (motherMaiden != null) {
            ehrPatient.setMotherMaiden(motherMaiden.getValue().toString());
        }
        ehrPatient.setSex(toEhrSex(p.getGender()));
        if (p.getExtensionByUrl(RACE) != null) {
            for (Coding coding : p.getExtensionByUrl(RACE).getValueCodeableConcept().getCoding()) {
                ehrPatient.addRace(new EhrRace(coding.getCode()));
            }
        }

        Extension ethnicity = p.getExtensionByUrl(ETHNICITY_EXTENSION);
        if (ethnicity != null) {
            ehrPatient.setEthnicity(ethnicity.getValueCoding().getCode());
        }

        for (ContactPoint telecom : p.getTelecom()) {
            if (null != telecom.getSystem()) {
                if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {
                    ehrPatient.addPhoneNumber(toEhrPhoneNumber(telecom));
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
            ehrPatient.addAddress(toEhrAddress(address));
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

    private Coding codingFromCodeset(String value, String system, CodesetType codesetType) {
        Coding coding = null;
        if (StringUtils.isNotBlank(value)) {
            coding = new Coding().setCode(value).setSystem(system);
            Code code = codeMapManager.getCodeMap().getCodeForCodeset(codesetType, value);
            if (code != null) {
                coding.setDisplay(code.getLabel());
            }
        }
        return coding;
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
        contact.setGender(toFhirGender(nextOfKin.getSex()));
        for (EhrAddress ehrAddress : nextOfKin.getAddresses()) {
            contact.setAddress(toFhirAddress(ehrAddress)); //TODO extension for multiple NK1 addresses
        }
        for (EhrPhoneNumber phoneNumber : nextOfKin.getPhoneNumbers()) {
            contact.addTelecom(toFhirContact(phoneNumber));
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
        nextOfKin.setSex(toEhrSex(contact.getGender()));
        nextOfKin.addAddress(toEhrAddress(contact.getAddress()));
        for (ContactPoint telecom : contact.getTelecom()) {
            switch (telecom.getSystem()) {
                case PHONE: {
                    nextOfKin.addPhoneNumber(toEhrPhoneNumber(telecom));
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

    public ContactPoint toFhirContact(EhrPhoneNumber phoneNumber) {
        ContactPoint contactPoint = new ContactPoint()
                .setValue(phoneNumber.getNumber())
                .setSystem(ContactPoint.ContactPointSystem.PHONE);
        try {
            if (StringUtils.isNotBlank(phoneNumber.getType())) {
                contactPoint.setUse(ContactPoint.ContactPointUse.valueOf(phoneNumber.getType()));
            }
        } catch (IllegalArgumentException illegalArgumentException) {
        }
        return contactPoint;
    }

    public EhrPhoneNumber toEhrPhoneNumber(ContactPoint phoneContact) {
        if (phoneContact.hasSystem() && phoneContact.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
            EhrPhoneNumber ehrPhoneNumber = new EhrPhoneNumber(phoneContact.getValue());
            if (phoneContact.hasUse()) {
                ehrPhoneNumber.setType(phoneContact.getUse().toCode());
            }
            return ehrPhoneNumber;
        } else {
            return null;
        }
    }

    public Address toFhirAddress(EhrAddress ehrAddress) {
        return new Address()
                .addLine(ehrAddress.getAddressLine1())
                .addLine(ehrAddress.getAddressLine2())
                .setCity(ehrAddress.getAddressCity())
                .setCountry(ehrAddress.getAddressCountry())
                .setState(ehrAddress.getAddressState())
                .setPostalCode(ehrAddress.getAddressZip());
    }

    public EhrAddress toEhrAddress(Address address) {
        EhrAddress ehrAddress = new EhrAddress();
        if (address.getLine().size() > 0) {
            ehrAddress.setAddressLine1(address.getLine().get(0).getValueNotNull());
        }
        if (address.getLine().size() > 1) {
            ehrAddress.setAddressLine2(address.getLine().get(1).getValueNotNull());
        }
        ehrAddress.setAddressCity(address.getCity());
        ehrAddress.setAddressState(address.getState());
        ehrAddress.setAddressZip(address.getPostalCode());
        ehrAddress.setAddressCountry(address.getCountry());
        ehrAddress.setAddressCountyParish(address.getDistrict());
        return ehrAddress;
    }

    private Enumerations.AdministrativeGender toFhirGender(String sex) {
        switch (sex) {
            case MALE_SEX:
                return Enumerations.AdministrativeGender.MALE;
            case FEMALE_SEX:
                return Enumerations.AdministrativeGender.FEMALE;
            default:
                return Enumerations.AdministrativeGender.OTHER;
        }
    }

    private String toEhrSex(Enumerations.AdministrativeGender gender) {
        switch (gender) {
            case MALE:
                return MALE_SEX;
            case FEMALE:
                return FEMALE_SEX;
            case OTHER:
            default:
                return "";
        }
    }
}
