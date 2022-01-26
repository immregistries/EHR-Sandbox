package org.immregistries.ehr;

import java.math.BigDecimal;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;

public class FhirImmunizationCreation {
  
  public static Immunization dbVaccinationToFhirVaccination(org.immregistries.ehr.model.VaccinationEvent dbVaccination) {
    
    Vaccine vaccine = dbVaccination.getVaccine();
    Facility facility = dbVaccination.getAdministeringFacility();
    Immunization i = new Immunization();
    i.setId(""+vaccine.getVaccineId());
    i.setRecorded(vaccine.getCreatedDate());
    i.setLotNumber(vaccine.getLotnumber());
    i.getOccurrenceDateTimeType().setValue(vaccine.getAdministeredDate());
    i.setDoseQuantity(new Quantity());
    i.getDoseQuantity().setValue(new BigDecimal(vaccine.getAdministeredAmount()));
    i.setExpirationDate(vaccine.getExpirationDate());
    if (vaccine.getActionCode() == "D") {
      i.setStatus(Immunization.ImmunizationStatus.ENTEREDINERROR);
    } else {
      switch(vaccine.getCompletionStatus()) {
        case "CP" : {
          i.setStatus(Immunization.ImmunizationStatus.COMPLETED);
          break;
        }
        case "NA" : {
          i.setStatus(Immunization.ImmunizationStatus.NOTDONE);
          break;
        }
        case "PA" : {
          i.setStatus(Immunization.ImmunizationStatus.NOTDONE);
          break;
        }
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
