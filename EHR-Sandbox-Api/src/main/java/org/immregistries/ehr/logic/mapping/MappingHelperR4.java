package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MappingHelperR4 extends MappingHelper {
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

    public static org.hl7.fhir.r4.model.CodeableConcept extensionGetCodeableConcept(org.hl7.fhir.r4.model.Extension extension) {
        if (extension != null) {
            return extension.castToCodeableConcept(extension.getValue());
        } else return null;
    }

    public static org.hl7.fhir.r4.model.Coding extensionGetCoding(org.hl7.fhir.r4.model.Extension extension) {
        if (extension != null) {
            return extension.castToCoding(extension.getValue());
        } else {
            return null;
        }
    }

}
