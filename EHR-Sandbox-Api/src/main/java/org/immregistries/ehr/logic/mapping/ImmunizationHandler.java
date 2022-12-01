package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.*;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Maps the Database with FHIR for the immunization resources
 */
@Service
public class ImmunizationHandler {
  //        logger.info("Code Maps fetched");
  @Autowired
  CodeMapManager codeMapManager;

  // Constants need to be harmonized with IIS Sandbox
  public static final String CVX = "http://hl7.org/fhir/sid/cvx";
  public static final String MVX = "http://terminology.hl7.org/CodeSystem/MVX";
  public static final String NDC = "NDC";
  public static final String INFORMATION_SOURCE = "informationSource";
  public static final String FUNCTION = "iis-sandbox-function";
  public static final String ORDERING = "ordering";
  public static final String ENTERING = "entering";
  public static final String ADMINISTERING = "administering";
  public static final String REFUSAL_REASON_CODE = "refusalReasonCode";
  public static final String BODY_PART = "bodyPart";
  public static final String BODY_ROUTE = "bodyRoute";
  public static final String FUNDING_SOURCE = "fundingSource";
  public static final String FUNDING_ELIGIBILITY = "fundingEligibility";


  public Immunization dbVaccinationToFhirVaccination(VaccinationEvent dbVaccination, String identifier_system) {
    Immunization i = dbVaccinationToFhirVaccination(dbVaccination);
    Identifier identifier = i.addIdentifier();
    identifier.setValue(""+dbVaccination.getId());
    identifier.setSystem(identifier_system);
    i.setPatient(new Reference("Patient/" + dbVaccination.getPatient().getId())
            .setIdentifier(new Identifier()
                    .setValue("" + dbVaccination.getPatient().getId())
                    .setSystem(identifier_system)));
    return  i;
  }
  
  private Immunization dbVaccinationToFhirVaccination(VaccinationEvent dbVaccination) {
    
    Vaccine vaccine = dbVaccination.getVaccine();
    Facility facility = dbVaccination.getAdministeringFacility();
    Immunization i = new Immunization();

    i.setRecorded(vaccine.getCreatedDate());
    i.setLotNumber(vaccine.getLotNumber());
    i.getOccurrenceDateTimeType().setValue(vaccine.getAdministeredDate());
    i.setDoseQuantity(new Quantity());
    i.getDoseQuantity().setValue(new BigDecimal(vaccine.getAdministeredAmount()));
    i.setExpirationDate(vaccine.getExpirationDate());
    if (vaccine.getActionCode().equals("D")) {
      i.setStatus(Immunization.ImmunizationStatusCodes.ENTEREDINERROR);
    } else {
      switch(vaccine.getCompletionStatus()) {
        case "CP" : {
          i.setStatus(Immunization.ImmunizationStatusCodes.COMPLETED);
          break;
        }
        case "NA" :
        case "PA" :
        case "RE" : {
          i.setStatus(Immunization.ImmunizationStatusCodes.NOTDONE);
          break;
        }
        case "" : {
          i.setStatus(Immunization.ImmunizationStatusCodes.NULL);
          break;
        }
      }
    }
    CodeMap codeMap = codeMapManager.getCodeMap();
    Collection<Code> codeRoute= codeMap.getCodesForTable(CodesetType.BODY_ROUTE);
    for (Code c: codeRoute) {
      if (c.getValue().equals(vaccine.getBodyRoute())) {
        i.setRoute(new CodeableConcept(new Coding(c.getConceptType(), c.getValue(), c.getLabel())));
      }
    }

    i.addReason().setConcept(new CodeableConcept(new Coding(REFUSAL_REASON_CODE,vaccine.getRefusalReasonCode(),vaccine.getRefusalReasonCode())));
    i.getSite().addCoding().setSystem(BODY_PART).setCode(vaccine.getBodySite());
    i.getRoute().addCoding().setSystem(BODY_ROUTE).setCode(vaccine.getBodyRoute());
    i.getFundingSource().addCoding().setSystem(FUNDING_SOURCE).setCode(vaccine.getFundingSource());
    i.addProgramEligibility().addCoding().setSystem(FUNDING_ELIGIBILITY).setCode(vaccine.getFundingEligibility());

    i.getVaccineCode().addCoding()
            .setSystem(CVX)
            .setCode(vaccine.getVaccineCvxCode());
    i.getVaccineCode().addCoding()
            .setSystem(NDC)
            .setCode(vaccine.getVaccineNdcCode());

    i.setManufacturer(new Reference()
            .setType("Organisation")
            .setIdentifier(new Identifier()
                    .setSystem(MVX)
                    .setValue(vaccine.getVaccineMvxCode())));


//    i.getLocation().setReferenceElement();
//    location.setName(facility.getNameDisplay());
    return i;
  }

  public VaccinationEvent fromFhir(Immunization immunization) {
    return new VaccinationEvent();
  }
  
  

}
