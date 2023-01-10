package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Maps the Database with FHIR for patient resources
 */
@Service
public class PatientMapperR4 {
  private  static Logger logger = LoggerFactory.getLogger(PatientMapperR4.class);
  private static final String MOTHER_MAIDEN_NAME = "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName";
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
  private static final String MALE_SEX = "M";
  private static final String FEMALE_SEX = "F";
  
  


  public static final SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
  public Patient dbPatientToFhirPatient(org.immregistries.ehr.api.entities.Patient dbPatient, String identifier_system) {
    Patient fhirPatient = dbPatientToFhirPatient(dbPatient);
    Identifier identifier = fhirPatient.addIdentifier();
    identifier.setValue(""+dbPatient.getId());
    identifier.setSystem(identifier_system);
    return fhirPatient;
  }

  public Patient dbPatientToFhirPatient(org.immregistries.ehr.api.entities.Patient dbPatient) {
    Patient p = new Patient();
    
    p.setBirthDate(dbPatient.getBirthDate());
    if (p.getNameFirstRep() != null) {
      HumanName name = p.addName()
              .setFamily(dbPatient.getNameLast())
              .addGiven(dbPatient.getNameFirst())
              .addGiven(dbPatient.getNameMiddle());
//			   .setUse(HumanName.NameUse.USUAL);
    }

    Extension motherMaidenName = p.addExtension()
            .setUrl(MOTHER_MAIDEN_NAME)
            .setValue(new StringType(dbPatient.getMotherMaiden()));

    switch (dbPatient.getSex()) {
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
    Extension raceExtension =  p.addExtension();
    raceExtension.setUrl(RACE);
    CodeableConcept race = new CodeableConcept().setText(RACE_SYSTEM);
    raceExtension.setValue(race);
    if (dbPatient.getRace() != null && !dbPatient.getRace().isBlank()) {
      race.addCoding().setCode(dbPatient.getRace());
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
    p.addExtension(ETHNICITY_EXTENSION, new Coding().setSystem(ETHNICITY_SYSTEM).setCode(dbPatient.getEthnicity()));
    // telecom
    if (null != dbPatient.getPhone()) {
      p.addTelecom().setSystem(ContactPoint.ContactPointSystem.PHONE)
              .setValue(dbPatient.getPhone());
    }
    if (null != dbPatient.getEmail()) {
      p.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL)
              .setValue(dbPatient.getEmail());
    }


    if (dbPatient.getDeathDate() != null) {
      p.setDeceased(new DateType(dbPatient.getDeathDate()));
    } else if (dbPatient.getDeathFlag().equals(YES)) {
      p.setDeceased(new BooleanType(true));
    } else if (dbPatient.getDeathFlag().equals(NO)) {
      p.setDeceased(new BooleanType(false));
    }

    p.addAddress().addLine(dbPatient.getAddressLine1())
            .addLine(dbPatient.getAddressLine2())
            .setCity(dbPatient.getAddressCity())
            .setCountry(dbPatient.getAddressCountry())
            .setState(dbPatient.getAddressState())
            .setDistrict(dbPatient.getAddressCountyParish())
            .setPostalCode(dbPatient.getAddressZip());

    if (dbPatient.getBirthOrder() != null && !dbPatient.getBirthOrder().isBlank()) {
      p.setMultipleBirth(new IntegerType().setValue(Integer.parseInt(dbPatient.getBirthOrder())));
    } else if (dbPatient.getBirthFlag().equals(YES)) {
      p.setMultipleBirth(new BooleanType(true));
    }

    Extension publicity = p.addExtension();
    publicity.setUrl(PUBLICITY_EXTENSION);
    Coding publicityValue = new Coding()
            .setSystem(PUBLICITY_SYSTEM)
            .setCode(dbPatient.getPublicityIndicator());
    publicity.setValue(publicityValue);
    if (dbPatient.getPublicityIndicatorDate() != null) {
      publicityValue.setVersion(dbPatient.getPublicityIndicatorDate().toString());
    }

    Extension protection = p.addExtension();
    protection.setUrl(PROTECTION_EXTENSION);
    Coding protectionValue = new Coding()
            .setSystem(PROTECTION_SYSTEM)
            .setCode(dbPatient.getProtectionIndicator());
    protection.setValue(protectionValue);
    if (dbPatient.getProtectionIndicatorDate() != null) {
      protectionValue.setVersion(dbPatient.getProtectionIndicatorDate().toString());
    }

    Extension registryStatus = p.addExtension();
    registryStatus.setUrl(REGISTRY_STATUS_EXTENSION);
    Coding registryValue = new Coding()
            .setSystem(REGISTRY_STATUS_INDICATOR)
            .setCode(dbPatient.getRegistryStatusIndicator());
    registryStatus.setValue(registryValue);
    if (dbPatient.getRegistryStatusIndicatorDate() != null) {
      registryValue.setVersion(dbPatient.getRegistryStatusIndicatorDate().toString());
    }

    Patient.ContactComponent contact = p.addContact();
    HumanName contactName = new HumanName();
    contact.setName(contactName);
    contact.addRelationship().setText(dbPatient.getGuardianRelationship());
    contactName.setFamily(dbPatient.getGuardianLast());
    contactName.addGivenElement().setValue(dbPatient.getGuardianFirst());
    contactName.addGivenElement().setValue(dbPatient.getGuardianMiddle());
    return p;
  }

  public org.immregistries.ehr.api.entities.Patient fromFhir(Patient p) {
    org.immregistries.ehr.api.entities.Patient patient = new org.immregistries.ehr.api.entities.Patient();
//    patient.setId(Integer.valueOf(new IdType(p.getId()).getIdPart()));
    patient.setUpdatedDate(p.getMeta().getLastUpdated());

    patient.setBirthDate(p.getBirthDate());
    // Name
    HumanName name = p.getNameFirstRep();
    patient.setNameLast(name.getFamily());
    if (name.getGiven().size() > 0) {
      patient.setNameFirst(name.getGiven().get(0).getValueNotNull());
    }
    if (name.getGiven().size() > 1) {
      patient.setNameMiddle(name.getGiven().get(1).getValueNotNull());
    }

    Extension motherMaiden = p.getExtensionByUrl(MOTHER_MAIDEN_NAME);
    if (motherMaiden != null) {
      patient.setMotherMaiden(motherMaiden.getValue().toString());
    }
    switch (p.getGender()) {
      case MALE:
        patient.setSex(MALE_SEX);
        break;
      case FEMALE:
        patient.setSex(FEMALE_SEX);
        break;
      case OTHER:
      default:
        patient.setSex("");
        break;
    }
    int raceNumber = 0;
    CodeableConcept races = MappingHelper.extensionGetCodeableConcept(p.getExtensionByUrl(RACE));
    for (Coding coding : races.getCoding()) {
      raceNumber++;
      switch (raceNumber) {
        case 1: {
          patient.setRace(coding.getCode());
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
    if (p.getExtensionByUrl(ETHNICITY_EXTENSION) != null) {
      Coding ethnicity = MappingHelper.extensionGetCoding(p.getExtensionByUrl(ETHNICITY_EXTENSION));
      patient.setEthnicity(ethnicity.getCode());
    }

    for (ContactPoint telecom : p.getTelecom()) {
      if (null != telecom.getSystem()) {
        if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {
          patient.setPhone(telecom.getValue());
        } else if (telecom.getSystem().equals(ContactPointSystem.EMAIL)) {
          patient.setEmail(telecom.getValue());
        }
      }
    }

    if (null != p.getDeceased()) {
      if (p.getDeceased().isBooleanPrimitive()) {
        if (p.getDeceasedBooleanType().booleanValue()) {
          patient.setDeathFlag(YES);
        } else {
          patient.setDeathFlag(NO);
        }
      }
      if (p.getDeceased().isDateTime()) {
        patient.setDeathDate(p.getDeceasedDateTimeType().getValue());
      }
    }
    // Address
    Address address = p.getAddressFirstRep();
    if (address.getLine().size() > 0) {
      patient.setAddressLine1(address.getLine().get(0).getValueNotNull());
    }
    if (address.getLine().size() > 1) {
      patient.setAddressLine2(address.getLine().get(1).getValueNotNull());
    }
    patient.setAddressCity(address.getCity());
    patient.setAddressState(address.getState());
    patient.setAddressZip(address.getPostalCode());
    patient.setAddressCountry(address.getCountry());
    patient.setAddressCountyParish(address.getDistrict());

    if (null != p.getMultipleBirth()) {
      if (p.getMultipleBirth().isBooleanPrimitive()) {
        if (p.getMultipleBirthBooleanType().booleanValue()) {
          patient.setBirthFlag(YES);
        } else {
          patient.setBirthFlag(NO);
        }
      } else {
        patient.setBirthOrder(String.valueOf(p.getMultipleBirthIntegerType()));
      }
    }

    Extension publicity = p.getExtensionByUrl(PUBLICITY_EXTENSION);
    if (publicity != null) {
      Coding value = MappingHelper.extensionGetCoding(publicity);
      patient.setPublicityIndicator(value.getCode());
      if (value.getVersion() != null && !value.getVersion().isBlank()) {
        try {
          patient.setPublicityIndicatorDate(MappingHelper.sdf.parse(value.getVersion()));
        } catch (ParseException e) {
//					throw new RuntimeException(e);
        }
      }
    }
    Extension protection = p.getExtensionByUrl(PROTECTION_EXTENSION);
    if (protection != null) {
      Coding value = MappingHelper.extensionGetCoding(protection);
      patient.setProtectionIndicator(value.getCode());
      if (value.getVersion() != null && !value.getVersion().isBlank()) {
        try {
          patient.setProtectionIndicatorDate(MappingHelper.sdf.parse(value.getVersion()));
        } catch (ParseException e) {
//					throw new RuntimeException(e);
        }
      }
    }
    Extension registry = p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION);
    if (registry != null) {
      Coding value = MappingHelper.extensionGetCoding(registry);
      patient.setRegistryStatusIndicator(value.getCode());
      if (value.getVersion() != null && !value.getVersion().isBlank()) {
        try {
          patient.setRegistryStatusIndicatorDate(MappingHelper.sdf.parse(value.getVersion()));
        } catch (ParseException e) {
//				throw new RuntimeException(e);
        }
      }
    }
    return patient;
  }
}