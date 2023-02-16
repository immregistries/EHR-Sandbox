package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.*;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * Maps the Database with FHIR for the immunization resources
 */
@Service
public class ImmunizationMapperR5 {

  @Autowired
  CodeMapManager codeMapManager;
  @Autowired
  ClinicianRepository clinicianRepository;

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


  public Immunization toFhirImmunization(VaccinationEvent vaccinationEvent, String identifier_system, String patient_identifier_system) {
    Immunization i = toFhirImmunization(vaccinationEvent);
    Identifier identifier = i.addIdentifier();
    identifier.setValue(""+vaccinationEvent.getId());
    identifier.setSystem(identifier_system);
//    i.setPatient(new Reference()
////            .setReference("Patient/" + vaccinationEvent.getPatient().getId())
//            .setIdentifier(new Identifier()
//                    .setValue(vaccinationEvent.getPatient().getId())
//                    .setSystem(patient_identifier_system)));
    return  i;
  }
  
  private Immunization toFhirImmunization(VaccinationEvent vaccinationEvent) {

    Vaccine vaccine = vaccinationEvent.getVaccine();
    Facility facility = vaccinationEvent.getAdministeringFacility();
    Immunization i = new Immunization();
    i.setPrimarySource(vaccinationEvent.isPrimarySource());

    if (!vaccinationEvent.getPatient().getMrn().isBlank()) {
      i.setPatient(new Reference()
//              .setReference("Patient/" + dbVaccination.getPatient().getId())
              .setIdentifier(new Identifier()
                      .setValue(vaccinationEvent.getPatient().getMrn())
                      .setSystem(PatientMapperR5.MRN_SYSTEM)));
    }

//    i.setRecorded(vaccine.getCreatedDate());
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
    i.addProgramEligibility().setProgram(new CodeableConcept(new Coding().setSystem(FUNDING_ELIGIBILITY).setCode(vaccine.getFundingEligibility())));

    i.getVaccineCode().addCoding()
            .setSystem(CVX)
            .setCode(vaccine.getVaccineCvxCode());
    i.getVaccineCode().addCoding()
            .setSystem(NDC)
            .setCode(vaccine.getVaccineNdcCode());

    i.setManufacturer(new CodeableReference(new Reference()
            .setType("Organisation")
            .setIdentifier(new Identifier()
                    .setSystem(MVX)
                    .setValue(vaccine.getVaccineMvxCode()))));
//    i.getLocation().setReferenceElement();
//    location.setName(facility.getNameDisplay());
    return i;
  }

  public VaccinationEvent toVaccinationEvent(Immunization i) {
    VaccinationEvent ve = new VaccinationEvent();
    ve.setVaccine(vaccineFromFhir(i));
    ve.setPrimarySource(i.getPrimarySource());
//    ve.setExternalLink(i.getIdentifierFirstRep().getValue());
    if (i.getPatient() != null && i.getPatient().getReference() != null && !i.getPatient().getReference().isBlank()) {
    }
    if (i.getLocation() != null && i.getLocation().getReference() != null && !i.getLocation().getReference().isBlank()){
//      ve.setOrgLocation(fhirRequests.readOrgLocation(i.getLocation().getReference()));
    }
//    if (i.hasInformationSourceReference() && i.getInformationSourceReference().getReference() != null && !i.getInformationSourceReference().getReference().isBlank()) {
//      Integer informationSourceId = Integer.parseInt(i.getInformationSourceReference().getReference().split("Clinician/")[1]); // TODO
//      ve.setEnteringClinician(clinicianRepository.findById(informationSourceId).get());
//    }
    for (Immunization.ImmunizationPerformerComponent performer: i.getPerformer()) {
      if (performer.getActor() != null && performer.getActor().getReference() != null && !performer.getActor().getReference().isBlank()){
        Integer performerId = Integer.parseInt(performer.getActor().getReference().split("Clinician/")[1]); // TODO
        switch (performer.getFunction().getCode(FUNCTION)){
          case ADMINISTERING: {
            ve.setAdministeringClinician(clinicianRepository.findById(performerId).get());//TODO
            break;
          }
          case ORDERING: {
            ve.setOrderingClinician(clinicianRepository.findById(performerId).get());//TODO
            break;
          }
        }
      }
    }
    return  ve;
  }

  public Vaccine vaccineFromFhir(Immunization i) {
    Vaccine v = new Vaccine();
    v.setUpdatedDate(i.getMeta().getLastUpdated());

//    v.setCreatedDate(i.getRecorded());
    v.setAdministeredDate(i.getOccurrenceDateTimeType().getValue());

    v.setVaccineCvxCode(i.getVaccineCode().getCode(CVX));
    v.setVaccineNdcCode(i.getVaccineCode().getCode(NDC));
    v.setVaccineMvxCode(i.getVaccineCode().getCode(MVX));

    v.setVaccineMvxCode(i.getManufacturer().getReference().getIdentifier().getValue());

    v.setAdministeredAmount(i.getDoseQuantity().getValue().toString());

//    v.setInformationSource(i.getInformationSourceCodeableConcept().getCode(INFORMATION_SOURCE));
    v.setUpdatedDate(new Date());

    v.setLotNumber(i.getLotNumber());
    v.setExpirationDate(i.getExpirationDate());
    if (i.getStatus() != null) {
      switch(i.getStatus()){
        case COMPLETED: {
          v.setCompletionStatus("CP");
          break;
        }
        case ENTEREDINERROR: {
          v.setActionCode("D");
          break;
        }
        case NOTDONE: {
          v.setCompletionStatus("RE");
          break;
        } //Could also be NA or PA
        case NULL:
        default:
          v.setCompletionStatus("");
          break;
      }
    }
    v.setRefusalReasonCode(i.getReasonFirstRep().getConcept().getCodingFirstRep().getCode());
    v.setBodySite(i.getSite().getCodingFirstRep().getCode());
    v.setBodyRoute(i.getRoute().getCodingFirstRep().getCode());
    v.setFundingSource(i.getFundingSource().getCodingFirstRep().getCode());
//    v.setFundingEligibility(i.getProgramEligibilityFirstRep().getCodingFirstRep().getCode());

    return v;
  }
  
  

}
