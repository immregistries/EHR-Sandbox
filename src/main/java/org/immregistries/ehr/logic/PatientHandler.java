package org.immregistries.ehr.logic;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;

import java.util.ArrayList;
import java.util.List;

public class PatientHandler {
  
  
  public static Patient dbPatientToFhirPatient(org.immregistries.ehr.entities.Patient dbPatient) {
    Patient fhirPatient = new Patient();
    fhirPatient.setId("" + dbPatient.getId());
    
    Identifier identifier = new Identifier();
    identifier.setValue(""+dbPatient.getId());
    List<Identifier> li = new ArrayList<>();
    li.add(identifier);
    fhirPatient.setIdentifier(li);
    HumanName name = new HumanName();
    name.addGiven(dbPatient.getNameFirst());
    name.addGiven(dbPatient.getNameMiddle());
    name.setFamily(dbPatient.getNameLast());
    fhirPatient.addName(name);

    Address address = new Address();
    address.setCity(dbPatient.getAddressCity());
    address.setCountry(dbPatient.getAddressCountry());
    address.setState(dbPatient.getAddressState());
    address.setPostalCode(dbPatient.getAddressZip());
    fhirPatient.addAddress(address);


      ContactPoint cp = new ContactPoint();
      cp.setValue(dbPatient.getPhone());
      cp.setSystem(ContactPointSystem.PHONE);
      cp.setValue(dbPatient.getEmail());
      cp.setSystem(ContactPointSystem.EMAIL);
      fhirPatient.addTelecom(cp);

    fhirPatient.setBirthDate(dbPatient.getBirthDate());

      if (dbPatient.getSex().equals("M")) {
        fhirPatient.setGender(AdministrativeGender.MALE);
      } else if (dbPatient.getSex().equals("F")) {
        fhirPatient.setGender(AdministrativeGender.FEMALE);
      }
      
    // String username = "default";
    // String password = "default"; 
    // String response;

    // response = ResourceClient.write(fhirPatient, tenantId, username, password);
        
    return fhirPatient;
  }
}
