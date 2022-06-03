package org.immregistries.ehr.fhir;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Immunization;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.Location;
import org.hl7.fhir.r5.model.Reference;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;

public class ImmunizationHandler {
  
  public static Immunization dbVaccinationToFhirVaccination(VaccinationEvent dbVaccination) {
    
    Vaccine vaccine = dbVaccination.getVaccine();
    Facility facility = dbVaccination.getAdministeringFacility();
    Immunization i = new Immunization();

    i.setId(""+vaccine.getVaccineId());
    Identifier identifier = new Identifier();
    identifier.setValue(""+dbVaccination.getVaccinationEventId());
    List<Identifier> li = new ArrayList<>();
    li.add(identifier);
    i.setIdentifier(li);

    i.setRecorded(vaccine.getCreatedDate());
    i.setLotNumber(vaccine.getLotnumber());
    i.getOccurrenceDateTimeType().setValue(vaccine.getAdministeredDate());
    i.setDoseQuantity(new Quantity());
    i.getDoseQuantity().setValue(new BigDecimal(vaccine.getAdministeredAmount()));
    i.setExpirationDate(vaccine.getExpirationDate());
    if (vaccine.getActionCode().equals("D")) {
      i.setStatus(Immunization.ImmunizationStatus.ENTEREDINERROR);
    } else {
      switch(vaccine.getCompletionStatus()) {
        case "CP" : {
          i.setStatus(Immunization.ImmunizationStatus.COMPLETED);
          break;
        }
        case "NA" :
        case "PA" :
        case "RE" : {
          i.setStatus(Immunization.ImmunizationStatus.NOTDONE);
          break;
        }
        case "" : {
          i.setStatus(Immunization.ImmunizationStatus.NULL);
          break;
        }
      }
    }
    i.addReasonCode().addCoding().setCode(vaccine.getRefusalReasonCode());
    i.getVaccineCode().addCoding().setCode(vaccine.getVaccineCvxCode());
    i.setPatient(new Reference("Patient/" + dbVaccination.getPatient().getPatientId()));
    Location location = i.getLocationTarget();
    location.setName(facility.getNameDisplay());

    return i;
    
  }
  
  

}
