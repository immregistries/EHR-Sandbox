package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Maps the Database with FHIR for the immunization resources
 */
@Service
public class ImmunizationMapperR4 {

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
//	  i.addIdentifier(MappingHelper.getFhirIdentifier(MappingHelper.VACCINATION_REPORTED, vaccine.getVaccinationReportedExternalLink()));
    i.setPatient(new Reference().setReference("Patient/"+ dbVaccination.getPatient().getId()));
    i.setRecorded(vaccine.getCreatedDate());
    i.getOccurrenceDateTimeType().setValue(vaccine.getAdministeredDate());

    if(!vaccine.getVaccineCvxCode().isBlank()){
      i.getVaccineCode().addCoding().setCode(vaccine.getVaccineCvxCode()).setSystem(CVX);
    }
    if(!vaccine.getVaccineNdcCode().isBlank()){
      i.getVaccineCode().addCoding().setCode(vaccine.getVaccineNdcCode()).setSystem(NDC);
    }
//	  i.setManufacturer(MappingHelper.getFhirReference(MappingHelper.ORGANISATION,MVX,vaccine.getVaccineMvxCode()));

    i.setDoseQuantity(new Quantity().setValue(new BigDecimal(vaccine.getAdministeredAmount())));

//	  i.setInformationSource(new CodeableConcept(new Coding().setSystem(INFORMATION_SOURCE).setCode(vaccine.getInformationSource()))); // TODO change system name
    i.setLotNumber(vaccine.getLotNumber());
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
        case "" :
        default: {
//					 i.setStatus(Immunization.ImmunizationStatus.NULL);
          break;
        }
      }
    }
    i.setStatusReason(new CodeableConcept(new Coding(REFUSAL_REASON_CODE,vaccine.getRefusalReasonCode(),vaccine.getRefusalReasonCode())));
    i.getSite().addCoding().setSystem(BODY_PART).setCode(vaccine.getBodySite());
    i.getRoute().addCoding().setSystem(BODY_ROUTE).setCode(vaccine.getBodyRoute());
    i.getFundingSource().addCoding().setSystem(FUNDING_SOURCE).setCode(vaccine.getFundingSource());
    i.addProgramEligibility().addCoding().setSystem(FUNDING_ELIGIBILITY).setCode(vaccine.getFundingEligibility());


//    if (dbVaccination.getAdministeringFacility() != null) {
//      i.setLocation(new Reference(MappingHelper.LOCATION + "/" + vaccine.getOrgLocationId()));
//    }

//    if (vaccine.getEnteredBy() != null) {
////		  i.setInformationSource(new Reference(MappingHelper.PRACTITIONER+"/" + vaccine.getEnteredBy().getPersonId()));
//    }
//    if (vaccine.getOrderingProvider() != null) {
//      i.addPerformer(performer(vaccine.getOrderingProvider(),ORDERING, ORDERING_DISPLAY));
//    }
//    if (vaccine.getAdministeringProvider() != null) {
//      i.addPerformer(performer(vaccine.getAdministeringProvider(),ADMINISTERING, ADMINISTERING_DISPLAY));
//    }
    return i;
  }

  public VaccinationEvent fromFhir(Immunization i) {
    VaccinationEvent ve = new VaccinationEvent();
    ve.setVaccine(vaccineFromFhir(i));
//    ve.setExternalLink(i.getIdentifierFirstRep().getValue());
    if (i.getPatient() != null && i.getPatient().getReference() != null && !i.getPatient().getReference().isBlank()) {
    }
    if (i.getLocation() != null && i.getLocation().getReference() != null && !i.getLocation().getReference().isBlank()){
//      ve.setOrgLocation(fhirRequests.readOrgLocation(i.getLocation().getReference()));
    }
//    if (i.hasInformationSourceReference() && i.getInformationSourceReference().getReference() != null && !i.getInformationSourceReference().getReference().isBlank()) {
//      Integer informationSourceId = Integer.parseInt(i.getInformationSourceReference().getReference().split("Clinician/")[1]);
//      ve.setEnteringClinician(clinicianRepository.findById(informationSourceId).get());
//    }
    for (Immunization.ImmunizationPerformerComponent performer: i.getPerformer()) {
      if (performer.getActor() != null && performer.getActor().getReference() != null && !performer.getActor().getReference().isBlank()
       && performer.getActor().getType().equals("Clinician") //TODO support more actor types
        ){
        String performerRef = performer.getActor().getReference();
        Integer performerId;
        if ( performerRef.split("Clinician/").length > 1) {
          performerId = Integer.parseInt(performer.getActor().getReference().split("Clinician/")[1]);
        } else {
          performerId = Integer.parseInt(performer.getActor().getReference().split("Clinician/")[0]);
        }
        switch (performer.getFunction().getCodingFirstRep().getCode()){ // TODO make sure system is FUNCTION
          case ADMINISTERING: {
            ve.setAdministeringClinician(clinicianRepository.findById(performerId).get());
            break;
          }
          case ORDERING: {
            ve.setOrderingClinician(clinicianRepository.findById(performerId).get());
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
    v.setUpdatedDate(i.getMeta().getLastUpdated());
//		v.setVaccinationReportedExternalLink(MappingHelper.filterIdentifier(i.getIdentifier(),MappingHelper.VACCINATION_REPORTED).getValue());

    v.setCreatedDate(i.getRecorded());
    if (i.hasOccurrenceDateTimeType()) {
      v.setAdministeredDate(i.getOccurrenceDateTimeType().getValue());
    } else if (i.hasOccurrenceStringType()){
      SimpleDateFormat parser=new SimpleDateFormat("yyyy-mm-dd");
      try {
        v.setAdministeredDate(parser.parse(i.getOccurrenceStringType().getValueNotNull()));
      } catch (ParseException e) {
        e.printStackTrace();
//        throw new RuntimeException(e);
      }
    }

    i.getVaccineCode().getCoding().forEach(coding -> {
      switch (coding.getSystem()) {
        case CVX: {
          v.setVaccineCvxCode(coding.getCode());
          break;
        }
        case NDC: {
          v.setVaccineNdcCode(coding.getCode());
          break;
        }
        case MVX: {
          v.setVaccineMvxCode(coding.getCode());
          break;
        }
      }
    });

    v.setVaccineMvxCode(i.getManufacturer().getIdentifier().getValue());

    if (i.getDoseQuantity().getValue() != null){
      v.setAdministeredAmount(i.getDoseQuantity().getValue().toString());
    }

    v.setInformationSource(i.getReportOrigin().getCodingFirstRep().getCode());
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
    v.setRefusalReasonCode(i.getStatusReason().getCodingFirstRep().getCode());
    v.setBodySite(i.getSite().getCodingFirstRep().getCode());
    v.setBodyRoute(i.getRoute().getCodingFirstRep().getCode());
    v.setFundingSource(i.getFundingSource().getCodingFirstRep().getCode());
    v.setFundingEligibility(i.getProgramEligibilityFirstRep().getCodingFirstRep().getCode());

    return v;
  }
  
  

}
