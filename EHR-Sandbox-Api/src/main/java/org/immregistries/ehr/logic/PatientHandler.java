package org.immregistries.ehr.logic;

import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r5.model.Enumerations.AdministrativeGender;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps the Database with FHIR for patient resources
 */
public class PatientHandler {
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

  public static Patient dbPatientToFhirPatient(org.immregistries.ehr.entities.Patient dbPatient, String identifier_system) {
    Patient fhirPatient = dbPatientToFhirPatient(dbPatient);
    Identifier identifier = fhirPatient.addIdentifier();
    identifier.setValue(""+dbPatient.getId());
    identifier.setSystem(identifier_system);
    return fhirPatient;
  }
  private static Patient dbPatientToFhirPatient(org.immregistries.ehr.entities.Patient dbPatient) {
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
    fhirPatient.addExtension(ETHNICITY_EXTENSION,new CodeType().setSystem(ETHNICITY_SYSTEM).setValue(dbPatient.getEthnicity()));

    if (dbPatient.getDeathDate() != null) {
      fhirPatient.setDeceased(new DateType(dbPatient.getDeathDate()));
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
}
