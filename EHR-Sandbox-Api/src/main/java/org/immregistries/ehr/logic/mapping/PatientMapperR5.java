package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r5.model.Enumerations.AdministrativeGender;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Maps the Database with FHIR for patient resources
 */
@Service
public class PatientMapperR5 {
  private  static Logger logger = LoggerFactory.getLogger(PatientMapperR5.class);
  public static final String MRN_SYSTEM = "urn:mrns";
  private static final String REGISTRY_STATUS_EXTENSION = "registryStatus";
  private static final String REGISTRY_STATUS_INDICATOR = "registryStatusIndicator";
  private static final String ETHNICITY_EXTENSION = "ethnicity";
  private static final String ETHNICITY_SYSTEM = "ethnicity";
  private static final String RACE = "race";
  private static final String RACE_SYSTEM = "race";
  private static final String PUBLICITY_EXTENSION = "publicity";
  private static final String PUBLICITY_SYSTEM = "publicityIndicator";
  private static final String PROTECTION_EXTENSION = "protection";
  private static final String PROTECTION_SYSTEM = "protectionIndicator";
  private static final String YES = "Y";
  private static final String NO = "N";

  public static final SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");

  public Patient toFhirPatient(EhrPatient dbPatient, String identifier_system) {
    Patient fhirPatient = toFhirPatient(dbPatient);
    Identifier identifier = fhirPatient.addIdentifier();
    identifier.setValue(""+dbPatient.getId());
    identifier.setSystem(identifier_system);
    return fhirPatient;
  }

  public Patient toFhirPatient(EhrPatient ehrPatient) {
    Patient p = new Patient();
//    p.setId("" + ehrPatient.getId());
    if (!ehrPatient.getMrn().isBlank()){
      p.addIdentifier().setSystem(MRN_SYSTEM).setValue(ehrPatient.getMrn());
    }

    p.addName()
            .addGiven(ehrPatient.getNameFirst())
            .addGiven(ehrPatient.getNameMiddle())
            .setFamily(ehrPatient.getNameLast());

    p.addAddress()
            .addLine(ehrPatient.getAddressLine1())
            .addLine(ehrPatient.getAddressLine2())
            .setCity(ehrPatient.getAddressCity())
            .setCountry(ehrPatient.getAddressCountry())
            .setState(ehrPatient.getAddressState())
            .setPostalCode(ehrPatient.getAddressZip());

    p.addTelecom()
            .setValue(ehrPatient.getPhone())
            .setSystem(ContactPointSystem.PHONE);
    p.addTelecom()
            .setValue(ehrPatient.getEmail())
            .setSystem(ContactPointSystem.EMAIL);

    p.setBirthDate(ehrPatient.getBirthDate());
    if (ehrPatient.getBirthOrder() != null && !ehrPatient.getBirthOrder().equals("")) {
      p.setMultipleBirth(new IntegerType().setValue(Integer.parseInt(ehrPatient.getBirthOrder())));
    } else if (ehrPatient.getBirthFlag().equals(YES)) {
      p.setMultipleBirth(new BooleanType(true));
    }
    if (ehrPatient.getSex().equals("M")) {
      p.setGender(AdministrativeGender.MALE);
    } else if (ehrPatient.getSex().equals("F")) {
      p.setGender(AdministrativeGender.FEMALE);
    } else {
      p.setGender(AdministrativeGender.OTHER);
    }

    //Race and ethnicity
    Extension raceExtension = p.addExtension();
    raceExtension.setUrl(RACE);
    CodeableConcept race = new CodeableConcept().setText(RACE_SYSTEM);
    raceExtension.setValue(race);
    if (ehrPatient.getRace() != null && !ehrPatient.getRace().isEmpty()) {
      race.addCoding().setCode(ehrPatient.getRace());
    }
    p.addExtension(ETHNICITY_EXTENSION, new Coding().setSystem(ETHNICITY_SYSTEM).setCode(ehrPatient.getEthnicity()));

//    p.addExtension(ETHNICITY_EXTENSION,new CodeType().setSystem(ETHNICITY_SYSTEM).setValue(ehrPatient.getEthnicity()));

    if (ehrPatient.getDeathDate() != null) {
      p.getDeceasedDateTimeType().setValue(ehrPatient.getDeathDate());
    } else if (ehrPatient.getDeathFlag().equals(YES)) {
      p.setDeceased(new BooleanType(true));
    } else if (ehrPatient.getDeathFlag().equals(NO)) {
      p.setDeceased(new BooleanType(false));
    }


    Extension publicity =  p.addExtension();
    publicity.setUrl(PUBLICITY_EXTENSION);
    publicity.setValue(
            new Coding().setSystem(PUBLICITY_SYSTEM)
                    .setCode(ehrPatient.getPublicityIndicator()));
    if (ehrPatient.getPublicityIndicatorDate() != null) {
      publicity.getValueCoding().setVersion(ehrPatient.getPublicityIndicatorDate().toString());
    }
    Extension protection =  p.addExtension();
    protection.setUrl(PROTECTION_EXTENSION);
    protection.setValue(
            new Coding().setSystem(PROTECTION_SYSTEM)
                    .setCode(ehrPatient.getProtectionIndicator()));
    if (ehrPatient.getProtectionIndicatorDate() != null) {
      protection.getValueCoding().setVersion(ehrPatient.getProtectionIndicatorDate().toString());
    }

    Extension registryStatus =  p.addExtension();
    registryStatus.setUrl(REGISTRY_STATUS_EXTENSION);
    registryStatus.setValue(
            new Coding().setSystem(REGISTRY_STATUS_INDICATOR)
                    .setCode(ehrPatient.getRegistryStatusIndicator()));
    if (ehrPatient.getRegistryStatusIndicatorDate() != null) {
      registryStatus.getValueCoding().setVersion(ehrPatient.getRegistryStatusIndicatorDate().toString());
    }
    return p;
  }

  public EhrPatient toEhrPatient(Patient p) {
    EhrPatient ehrPatient = new EhrPatient();
    // Identifiers are dealt with in the providers

    ehrPatient.setUpdatedDate(p.getMeta().getLastUpdated());

    ehrPatient.setBirthDate(p.getBirthDate());
//    patient.setManagingOrganizationId(p.getManagingOrganization().getId());
    // Name
    HumanName name = p.getNameFirstRep();
    ehrPatient.setNameLast(name.getFamily());
    if (name.getGiven().size() > 0) {
      ehrPatient.setNameFirst(name.getGiven().get(0).getValueNotNull());
    }
    if (name.getGiven().size() > 1) {
      ehrPatient.setNameMiddle(name.getGiven().get(1).getValueNotNull());
    }

//		patient.setMotherMaiden(); TODO
    switch (p.getGender()) {
      case MALE:
        ehrPatient.setSex("M");
        break;
      case FEMALE:
        ehrPatient.setSex("F");
        break;
      case OTHER:
      default:
        ehrPatient.setSex("");
        break;
    }
    int raceNumber = 0;
    for (Coding coding: p.getExtensionByUrl(RACE).getValueCodeableConcept().getCoding()) {
      raceNumber++;
      switch (raceNumber) {
        case 1:{
          ehrPatient.setRace(coding.getCode());
        }
//        case 2:{ TODO race list
//          patient.setRace2(coding.getCode());
//        }
//        case 3:{
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
    if (p.getExtensionByUrl(ETHNICITY_EXTENSION) != null) {
      ehrPatient.setEthnicity(p.getExtensionByUrl(ETHNICITY_EXTENSION).getValueCoding().getCode());
    }

    for (ContactPoint telecom : p.getTelecom()) {
      if (null != telecom.getSystem()) {
        if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {
          ehrPatient.setPhone(telecom.getValue());
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

    if (p.getExtensionByUrl(PUBLICITY_EXTENSION) != null) {
      ehrPatient.setPublicityIndicator(p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getCode());
      if (p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getVersion() != null && !p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getVersion().isBlank() ) {
        try {
          ehrPatient.setPublicityIndicatorDate(sdf.parse(p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getVersion()));
        } catch (ParseException e) {}
      }
    }

    if (p.getExtensionByUrl(PROTECTION_EXTENSION) != null) {
      ehrPatient.setProtectionIndicator(p.getExtensionByUrl(PROTECTION_EXTENSION).getValueCoding().getCode());
      if (p.getExtensionByUrl(PROTECTION_EXTENSION).getValueCoding().getVersion() != null) {
        try {
          ehrPatient.setProtectionIndicatorDate(sdf.parse(p.getExtensionByUrl(PROTECTION_EXTENSION).getValueCoding().getVersion()));
        } catch (ParseException e) {}
      }
    }

    if (p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION) != null) {
      ehrPatient.setRegistryStatusIndicator(p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION).getValueCoding().getCode());
      try {
        ehrPatient.setRegistryStatusIndicatorDate(sdf.parse(p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION).getValueCoding().getVersion()));
      } catch (ParseException e) {}
    }
    return ehrPatient;
  }
}
