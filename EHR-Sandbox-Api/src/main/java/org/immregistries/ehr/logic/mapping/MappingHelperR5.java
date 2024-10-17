package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrHumanName;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MappingHelperR5 extends MappingHelper {
    @Autowired
    CodeMapManager codeMapManager;

    public Coding codingFromCodeset(String value, String system, CodesetType codesetType) {
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

    public static Enumerations.AdministrativeGender toFhirGender(String sex) {
        switch (sex) {
            case MALE_SEX:
                return Enumerations.AdministrativeGender.MALE;
            case FEMALE_SEX:
                return Enumerations.AdministrativeGender.FEMALE;
            default:
                return Enumerations.AdministrativeGender.OTHER;
        }
    }

    public static ContactPoint toFhirContact(EhrPhoneNumber phoneNumber) {
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

    public static EhrPhoneNumber toEhrPhoneNumber(ContactPoint phoneContact) {
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

    public static Address toFhirAddress(EhrAddress ehrAddress) {
        return new Address()
                .addLine(ehrAddress.getAddressLine1())
                .addLine(ehrAddress.getAddressLine2())
                .setCity(ehrAddress.getAddressCity())
                .setCountry(ehrAddress.getAddressCountry())
                .setState(ehrAddress.getAddressState())
                .setPostalCode(ehrAddress.getAddressZip());
    }

    public static EhrAddress toEhrAddress(Address address) {
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

    public static String codeFromSystemOrDefault(CodeableConcept codeableConcept, String system) {
        String value = null;
        if (codeableConcept != null) {
            value = codeableConcept.getCode(system);
            if (value == null && codeableConcept.getCoding().size() == 1) {
                value = codeableConcept.getCodingFirstRep().getCode();
            }
        }
        return value;
    }

    public static EhrHumanName toEhrName(HumanName name) {
        EhrHumanName ehrHumanName = new EhrHumanName();
        ehrHumanName.setNameLast(name.getFamily());
        if (name.getGiven().size() > 0) {
            ehrHumanName.setNameFirst(name.getGiven().get(0).getValueNotNull());
        }
        if (name.getGiven().size() > 1) {
            ehrHumanName.setNameMiddle(name.getGiven().get(1).getValueNotNull());
        }
        ehrHumanName.setNameSuffix(name.getSuffixAsSingleString());
        ehrHumanName.setNamePrefix(name.getPrefixAsSingleString());
        return ehrHumanName;
    }

    public static String toEhrSex(Enumerations.AdministrativeGender gender) {
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


    public static HumanName toFhirName(EhrHumanName name) {
        HumanName humanName = new HumanName()
                .setFamily(name.getNameLast())
                .addGiven(name.getNameFirst())
                .addGiven(name.getNameMiddle())
                .addSuffix(name.getNameSuffix())
                .addPrefix(name.getNamePrefix());
        if (StringUtils.isNotBlank(name.getNameType())) {
            humanName.setUse(HumanName.NameUse.OFFICIAL); // TODO MAPPING
        }
        return humanName;
    }

}
