package org.immregistries.ehr.logic;

import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.entities.Facility;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.entities.Vaccine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ImmunizationHandler {
  
  public static Immunization dbVaccinationToFhirVaccination(VaccinationEvent dbVaccination) {
    
    Vaccine vaccine = dbVaccination.getVaccine();
    Facility facility = dbVaccination.getAdministeringFacility();
    Immunization i = new Immunization();

    i.setId(""+vaccine.getId());
    Identifier identifier = new Identifier();
    identifier.setValue(""+dbVaccination.getId());
    List<Identifier> li = new ArrayList<>();
    li.add(identifier);
    i.setIdentifier(li);

    i.setRecorded(vaccine.getCreatedDate());
    i.setLotNumber(vaccine.getLotNumber());
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
    i.setPatient(new Reference("Patient/" + dbVaccination.getPatient().getId()));
    Location location = i.getLocationTarget();
    location.setName(facility.getNameDisplay());

    return i;
    
  }
  
  

}
