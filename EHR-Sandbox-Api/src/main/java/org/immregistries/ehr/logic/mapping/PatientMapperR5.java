package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r5.model.Enumerations.AdministrativeGender;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Maps the Database with FHIR for patient resources
 */
@Service
public class PatientMapperR5 {
  private  static Logger logger = LoggerFactory.getLogger(PatientMapperR5.class);
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
  public Patient dbPatientToFhirPatient(EhrPatient dbPatient, String identifier_system) {
    Patient fhirPatient = dbPatientToFhirPatient(dbPatient);
    Identifier identifier = fhirPatient.addIdentifier();
    identifier.setValue(""+dbPatient.getId());
    identifier.setSystem(identifier_system);
    return fhirPatient;
  }

  public Patient dbPatientToFhirPatient(EhrPatient dbPatient) {
    Patient fhirPatient = new Patient();
//    fhirPatient.setId("" + dbPatient.getId());
    
//    Identifier identifier = fhirPatient.addIdentifier(); Dealt with in Controller
//    identifier.setValue(""+dbPatient.getId());
//    identifier.setSystem("EHR-Sandbox");


    fhirPatient.addName()
            .addGiven(dbPatient.getNameFirst())
            .addGiven(dbPatient.getNameMiddle())
            .setFamily(dbPatient.getNameLast());

    fhirPatient.addAddress()
            .addLine(dbPatient.getAddressLine1())
            .addLine(dbPatient.getAddressLine2())
            .setCity(dbPatient.getAddressCity())
            .setCountry(dbPatient.getAddressCountry())
            .setState(dbPatient.getAddressState())
            .setPostalCode(dbPatient.getAddressZip());

    fhirPatient.addTelecom()
            .setValue(dbPatient.getPhone())
            .setSystem(ContactPointSystem.PHONE);
    fhirPatient.addTelecom()
            .setValue(dbPatient.getEmail())
            .setSystem(ContactPointSystem.EMAIL);

    fhirPatient.setBirthDate(dbPatient.getBirthDate());
    if (dbPatient.getBirthOrder() != null && !dbPatient.getBirthOrder().equals("")) {
      fhirPatient.setMultipleBirth(new IntegerType().setValue(Integer.parseInt(dbPatient.getBirthOrder())));
    } else if (dbPatient.getBirthFlag().equals(YES)) {
      fhirPatient.setMultipleBirth(new BooleanType(true));
    }
    if (dbPatient.getSex().equals("M")) {
      fhirPatient.setGender(AdministrativeGender.MALE);
    } else if (dbPatient.getSex().equals("F")) {
      fhirPatient.setGender(AdministrativeGender.FEMALE);
    } else {
      fhirPatient.setGender(AdministrativeGender.OTHER);
    }

    //Race and ethnicity
    Extension raceExtension = fhirPatient.addExtension();
    raceExtension.setUrl(RACE);
    CodeableConcept race = new CodeableConcept().setText(RACE_SYSTEM);
    raceExtension.setValue(race);
    if (dbPatient.getRace() != null && !dbPatient.getRace().isEmpty()) {
      race.addCoding().setCode(dbPatient.getRace());
    }
    fhirPatient.addExtension(ETHNICITY_EXTENSION, new Coding().setSystem(ETHNICITY_SYSTEM).setCode(dbPatient.getEthnicity()));

//    fhirPatient.addExtension(ETHNICITY_EXTENSION,new CodeType().setSystem(ETHNICITY_SYSTEM).setValue(dbPatient.getEthnicity()));

    if (dbPatient.getDeathDate() != null) {
      fhirPatient.getDeceasedDateTimeType().setValue(dbPatient.getDeathDate());
    } else if (dbPatient.getDeathFlag().equals(YES)) {
      fhirPatient.setDeceased(new BooleanType(true));
    } else if (dbPatient.getDeathFlag().equals(NO)) {
      fhirPatient.setDeceased(new BooleanType(false));
    }


    Extension publicity =  fhirPatient.addExtension();
    publicity.setUrl(PUBLICITY_EXTENSION);
    publicity.setValue(
            new Coding().setSystem(PUBLICITY_SYSTEM)
                    .setCode(dbPatient.getPublicityIndicator()));
    if (dbPatient.getPublicityIndicatorDate() != null) {
      publicity.getValueCoding().setVersion(dbPatient.getPublicityIndicatorDate().toString());
    }
    Extension protection =  fhirPatient.addExtension();
    protection.setUrl(PROTECTION_EXTENSION);
    protection.setValue(
            new Coding().setSystem(PROTECTION_SYSTEM)
                    .setCode(dbPatient.getProtectionIndicator()));
    if (dbPatient.getProtectionIndicatorDate() != null) {
      protection.getValueCoding().setVersion(dbPatient.getProtectionIndicatorDate().toString());
    }

    Extension registryStatus =  fhirPatient.addExtension();
    registryStatus.setUrl(REGISTRY_STATUS_EXTENSION);
    registryStatus.setValue(
            new Coding().setSystem(REGISTRY_STATUS_INDICATOR)
                    .setCode(dbPatient.getRegistryStatusIndicator()));
    if (dbPatient.getRegistryStatusIndicatorDate() != null) {
      registryStatus.getValueCoding().setVersion(dbPatient.getRegistryStatusIndicatorDate().toString());
    }
    return fhirPatient;
  }

  public EhrPatient fromFhir(Patient p) {
    EhrPatient patient = new EhrPatient();
    // Identifiers are dealt with in the providers

    patient.setUpdatedDate(p.getMeta().getLastUpdated());

    patient.setBirthDate(p.getBirthDate());
//    patient.setManagingOrganizationId(p.getManagingOrganization().getId());
    // Name
    HumanName name = p.getNameFirstRep();
    patient.setNameLast(name.getFamily());
    if (name.getGiven().size() > 0) {
      patient.setNameFirst(name.getGiven().get(0).getValueNotNull());
    }
    if (name.getGiven().size() > 1) {
      patient.setNameMiddle(name.getGiven().get(1).getValueNotNull());
    }

//		patient.setMotherMaiden(); TODO
    switch (p.getGender()) {
      case MALE:
        patient.setSex("M");
        break;
      case FEMALE:
        patient.setSex("F");
        break;
      case OTHER:
      default:
        patient.setSex("");
        break;
    }
    int raceNumber = 0;
    for (Coding coding: p.getExtensionByUrl(RACE).getValueCodeableConcept().getCoding()) {
      raceNumber++;
      switch (raceNumber) {
        case 1:{
          patient.setRace(coding.getCode());
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
      patient.setEthnicity(p.getExtensionByUrl(ETHNICITY_EXTENSION).getValueCoding().getCode());
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

    if (null != p.getMultipleBirth() && !p.getMultipleBirth().isEmpty()) {
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

    if (p.getExtensionByUrl(PUBLICITY_EXTENSION) != null) {
      patient.setPublicityIndicator(p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getCode());
      if (p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getVersion() != null && !p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getVersion().isBlank() ) {
        try {
          patient.setPublicityIndicatorDate(sdf.parse(p.getExtensionByUrl(PUBLICITY_EXTENSION).getValueCoding().getVersion()));
        } catch (ParseException e) {}
      }
    }

    if (p.getExtensionByUrl(PROTECTION_EXTENSION) != null) {
      patient.setProtectionIndicator(p.getExtensionByUrl(PROTECTION_EXTENSION).getValueCoding().getCode());
      if (p.getExtensionByUrl(PROTECTION_EXTENSION).getValueCoding().getVersion() != null) {
        try {
          patient.setProtectionIndicatorDate(sdf.parse(p.getExtensionByUrl(PROTECTION_EXTENSION).getValueCoding().getVersion()));
        } catch (ParseException e) {}
      }
    }

    if (p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION) != null) {
      patient.setRegistryStatusIndicator(p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION).getValueCoding().getCode());
      try {
        patient.setRegistryStatusIndicatorDate(sdf.parse(p.getExtensionByUrl(REGISTRY_STATUS_EXTENSION).getValueCoding().getVersion()));
      } catch (ParseException e) {}
    }
    return patient;
  }
}
