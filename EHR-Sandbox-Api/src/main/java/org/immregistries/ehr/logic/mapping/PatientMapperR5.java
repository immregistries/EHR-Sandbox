package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r5.model.Enumerations.AdministrativeGender;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
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

  @Autowired
  CodeMapManager codeMapManager;
  @Autowired
  OrganizationMapperR5 organizationMapperR5;
  private static Logger logger = LoggerFactory.getLogger(PatientMapperR5.class);
  public static final String MRN_SYSTEM = "mrn";
  public static final String MOTHER_MAIDEN_NAME = "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName";
  public static final String REGISTRY_STATUS_EXTENSION = "registryStatus";
  public static final String REGISTRY_STATUS_INDICATOR = "registryStatusIndicator";
  public static final String ETHNICITY_EXTENSION = "ethnicity";
  public static final String ETHNICITY_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-Ethnicity";
  public static final String RACE = "race";
  public static final String RACE_SYSTEM = "https://terminology.hl7.org/2.0.0/CodeSystem-v3-Race.html";
  public static final String PUBLICITY_EXTENSION = "publicity";
  public static final String PUBLICITY_SYSTEM = "publicityIndicator";
  public static final String PROTECTION_EXTENSION = "protection";
  public static final String PROTECTION_SYSTEM = "protectionIndicator";
  public static final String YES = "Y";
  public static final String NO = "N";

  public static final String MALE_SEX = "M";
  public static final String FEMALE_SEX = "F";

  public static final SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
  public Patient toFhirPatient(EhrPatient ehrPatient, Facility facility) {
    Patient p = toFhirPatient(ehrPatient);
    p.setManagingOrganization(new Reference().setIdentifier(organizationMapperR5.toFhirOrganization(facility).getIdentifierFirstRep()));
    return p;
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

    /**
     * Race
     */
    Extension raceExtension = p.addExtension();
    raceExtension.setUrl(RACE);
    CodeableConcept race = new CodeableConcept();
    raceExtension.setValue(race);
    race.addCoding(codingFromCodeset(ehrPatient.getRace(),RACE_SYSTEM,CodesetType.PATIENT_RACE));


    /**
     * Ethnicity
     */
    p.addExtension(ETHNICITY_EXTENSION, codingFromCodeset(ehrPatient.getEthnicity(),ETHNICITY_SYSTEM,CodesetType.PATIENT_ETHNICITY));

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
//    ehrPatient.setManagingOrganizationId(p.getManagingOrganization().getId());
    // Name
    HumanName name = p.getNameFirstRep();
    ehrPatient.setNameLast(name.getFamily());
    if (name.getGiven().size() > 0) {
      ehrPatient.setNameFirst(name.getGiven().get(0).getValueNotNull());
    }
    if (name.getGiven().size() > 1) {
      ehrPatient.setNameMiddle(name.getGiven().get(1).getValueNotNull());
    }

    Identifier chosenIdentifier = p.getIdentifier().stream().filter(identifier -> identifier.getSystem().equals(MRN_SYSTEM)).findFirst()
            .orElse(p.getIdentifierFirstRep());
    ehrPatient.setMrn(chosenIdentifier.getValue());
    ehrPatient.setMrnSystem(chosenIdentifier.getSystem());

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
    if (p.getExtensionByUrl(RACE) != null) {
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
    }

    Extension ethnicity = p.getExtensionByUrl(ETHNICITY_EXTENSION);
    if (ethnicity != null) {
      ehrPatient.setEthnicity(ethnicity.getValueCoding().getCode());
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
    return ehrPatient;
  }

  private Coding codingFromCodeset(String value,String system,CodesetType codesetType) {
    Coding coding = null;
    if (StringUtils.isNotBlank(value)) {
      coding = new Coding().setCode(value).setSystem(system);
      Code code = codeMapManager.getCodeMap().getCodeForCodeset(codesetType,value);
      if (code != null) {
        coding.setDisplay(code.getLabel());
      }
    }
    return coding;
  }
}
