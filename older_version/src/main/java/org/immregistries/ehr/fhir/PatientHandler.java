package org.immregistries.ehr.fhir;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Patient;

import org.hl7.fhir.r5.model.Enumerations.AdministrativeGender;

public class PatientHandler {
  
  
  public static Patient dbPatientToFhirPatient(org.immregistries.ehr.model.Patient dbPatient) {
    Patient fhirPatient = new Patient();
    fhirPatient.setId("" + dbPatient.getPatientId());
    
    Identifier identifier = new Identifier();
    identifier.setValue(""+dbPatient.getPatientId());
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
