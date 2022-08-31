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


    HumanName name = fhirPatient.addName();
    name.addGiven(dbPatient.getNameFirst());
    name.addGiven(dbPatient.getNameMiddle());
    name.setFamily(dbPatient.getNameLast());

    Address address = fhirPatient.addAddress();
    address.setCity(dbPatient.getAddressCity());
    address.setCountry(dbPatient.getAddressCountry());
    address.setState(dbPatient.getAddressState());
    address.setPostalCode(dbPatient.getAddressZip());

    ContactPoint cp = fhirPatient.addTelecom();
    cp.setValue(dbPatient.getPhone());
    cp.setSystem(ContactPointSystem.PHONE);
    cp.setValue(dbPatient.getEmail());
    cp.setSystem(ContactPointSystem.EMAIL);

    fhirPatient.setBirthDate(dbPatient.getBirthDate());
    if (dbPatient.getSex().equals("M")) {
      fhirPatient.setGender(AdministrativeGender.MALE);
    } else if (dbPatient.getSex().equals("F")) {
      fhirPatient.setGender(AdministrativeGender.FEMALE);
    }
    return fhirPatient;
  }
}
