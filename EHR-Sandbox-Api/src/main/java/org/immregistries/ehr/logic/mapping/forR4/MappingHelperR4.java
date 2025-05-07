package org.immregistries.ehr.logic.mapping.forR4;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.*;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrHumanName;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.immregistries.ehr.logic.mapping.MappingHelper;
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

    public ContactPoint toFhirContact(EhrPhoneNumber phoneNumber) {
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue(phoneNumber.getNumber());
        String use = phoneNumber.getUse();
        if (use != null) {
            try {
                contactPoint.setUse(ContactPoint.ContactPointUse.fromCode(use));
            } catch (FHIRException ignored) {
                CodeMap codeMap = codeMapManager.getCodeMap();
                Code useCode = codeMap.getCodeForCodeset(CodesetType.TELECOMMUNICATION_USE, use);
                if (useCode != null) {
                    contactPoint.addExtension(USE_EXTENSION_URL, new org.hl7.fhir.r4.model.Coding().setSystem(PHONE_USE_V2_SYSTEM).setCode(use));
                    switch (use) {
                        case "": {
                            break;
                        }
                        case "PRN":
                        case "ORN":
                        case "VHN": {
                            contactPoint.setUse(ContactPoint.ContactPointUse.HOME);
                            break;
                        }
                        case "WPN": {
                            contactPoint.setUse(ContactPoint.ContactPointUse.WORK);
                            break;
                        }
                        case "PRS": {
                            contactPoint.setUse(ContactPoint.ContactPointUse.MOBILE);
                            break;
                        }
                    }
                }
            }
        }
        return contactPoint;
    }

    public EhrPhoneNumber toEhrPhoneNumber(ContactPoint contactPoint) {
        EhrPhoneNumber ehrPhoneNumber = new EhrPhoneNumber();
        ehrPhoneNumber.setNumber(contactPoint.getValue());
        org.hl7.fhir.r4.model.Extension useExtension = contactPoint.getExtensionByUrl(USE_EXTENSION_URL);
        if (useExtension != null) {
            org.hl7.fhir.r4.model.Coding coding = extensionGetCoding(useExtension);
            if (coding != null && StringUtils.isNotBlank(coding.getCode())) {
                ehrPhoneNumber.setUse(coding.getCode());
            } else {
                ehrPhoneNumber.setUse("");
            }
        } else if (contactPoint.getUse() != null) {
            ehrPhoneNumber.setUse(contactPoint.getUse().toCode());
        } else {
            ehrPhoneNumber.setUse(null);
        }
        return ehrPhoneNumber;
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

    public Enumerations.AdministrativeGender toFhirGender(String sex) {
        if (sex == null) {
            return null;
        }
        switch (sex) {
            case MALE_SEX:
                return Enumerations.AdministrativeGender.MALE;
            case FEMALE_SEX:
                return Enumerations.AdministrativeGender.FEMALE;
            case OTHER_SEX:
                return Enumerations.AdministrativeGender.OTHER;
            case UNKNOWN_SEX:
                return Enumerations.AdministrativeGender.UNKNOWN;
            default:
                return Enumerations.AdministrativeGender.NULL;
        }
    }

    public String toEhrSex(Enumerations.AdministrativeGender gender) {
        if (gender == null) {
            return "";
        }
        switch (gender) {
            case MALE:
                return MALE_SEX;
            case FEMALE:
                return FEMALE_SEX;
            case OTHER:
                return OTHER_SEX;
            case UNKNOWN:
                return UNKNOWN_SEX;
            default:
                return "";
        }
    }

    public org.hl7.fhir.r4.model.CodeableConcept extensionGetCodeableConcept(org.hl7.fhir.r4.model.Extension extension) {
        if (extension != null) {
            return extension.castToCodeableConcept(extension.getValue());
        } else return null;
    }

    public String codeFromSystemOrDefault(CodeableConcept codeableConcept, String system) {
        String value = null;
        if (codeableConcept != null) {
            for (Coding coding : codeableConcept.getCoding()) {
                if (system.equals(coding.getSystem())) {
                    value = coding.getCode();
                    break;
                }
            }
            if (value == null && codeableConcept.getCoding().size() == 1) {
                value = codeableConcept.getCodingFirstRep().getCode();
            }
        }
        return value;
    }

    public EhrHumanName toEhrName(HumanName name) {
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


    public HumanName toFhirName(EhrHumanName ehrHumanName) {
        if (ProcessingFlavor.UPPERCASE.isActive()) {
            ehrHumanName.setNameLast(ehrHumanName.getNameLast().toUpperCase());
            ehrHumanName.setNameFirst(ehrHumanName.getNameFirst().toUpperCase());
            ehrHumanName.setNameMiddle(ehrHumanName.getNameMiddle().toUpperCase());
        }
        HumanName humanName = new HumanName()
                .setFamily(ehrHumanName.getNameLast())
                .addGiven(ehrHumanName.getNameFirst())
                .addGiven(ehrHumanName.getNameMiddle())
                .addSuffix(ehrHumanName.getNameSuffix())
                .addPrefix(ehrHumanName.getNamePrefix());
        if (StringUtils.isNotBlank(ehrHumanName.getNameType())) {
            humanName.setUse(HumanName.NameUse.OFFICIAL); // TODO MAPPING
        }
        return humanName;
    }


}
