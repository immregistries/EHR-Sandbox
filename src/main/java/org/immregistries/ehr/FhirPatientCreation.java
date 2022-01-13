package org.immregistries.ehr;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;

public class FhirPatientCreation {
  
  
  public static Patient dbPatientToFhirPatient(org.immregistries.ehr.model.Patient dbPatient,String tenantId) {
    Patient fhirPatient = new Patient();

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
    FhirContext ctx = FhirContext.forR4();
    String serverbase = "https://florence.immregistries.org/iis-sandbox/fhir/" + tenantId;
    IGenericClient client = ctx.newRestfulGenericClient(serverbase);
    MethodOutcome outcome = client.create()
        .resource(fhirPatient)
        .prettyPrint()
        .encodedXml()
        .execute();
    
    IIdType id = outcome.getId();
    System.out.println(id);
        
    return fhirPatient;
  }
}
