package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

/**
 * Maps the Database with FHIR for the immunization resources
 */
@Service
@Conditional(OnR5Condition.class)
public class ImmunizationMapperR5 implements IImmunizationMapper<Immunization> {

    @Autowired
    CodeMapManager codeMapManager;
    @Autowired
    ClinicianRepository clinicianRepository;


    public Immunization toFhir(VaccinationEvent vaccinationEvent, String identifier_system) {
        Immunization i = toFhir(vaccinationEvent);
        Identifier identifier = i.addIdentifier();
        identifier.setValue("" + vaccinationEvent.getId());
        identifier.setSystem(identifier_system);
//    i.setPatient(new Reference()
////            .setReference("Patient/" + vaccinationEvent.getPatient().getId())
//            .setIdentifier(new Identifier()
//                    .setValue(vaccinationEvent.getPatient().getId())
//                    .setSystem(patient_identifier_system)));
        return i;
    }

    private Immunization toFhir(VaccinationEvent vaccinationEvent) {

        Vaccine vaccine = vaccinationEvent.getVaccine();
        Facility facility = vaccinationEvent.getAdministeringFacility();
        Immunization i = new Immunization();
        if (Objects.nonNull(vaccinationEvent.getPrimarySource())) {
            i.setPrimarySource(vaccinationEvent.getPrimarySource());
        }

        EhrIdentifier mrnIdentifier = vaccinationEvent.getPatient().getMrnEhrIdentifier();
        if (mrnIdentifier != null) {
            i.setPatient(new Reference()
                    .setIdentifier(mrnIdentifier.toR5()));
        }
        i.setLotNumber(vaccine.getLotNumber());
        i.getOccurrenceDateTimeType().setValue(vaccine.getAdministeredDate());
        i.setDoseQuantity(new Quantity());
        if (StringUtils.isNotBlank(vaccine.getAdministeredAmount())) {
            i.getDoseQuantity().setValue(new BigDecimal(vaccine.getAdministeredAmount()));
        }
        i.setExpirationDate(vaccine.getExpirationDate());
        if (vaccine.getActionCode().equals("D")) {
            i.setStatus(Immunization.ImmunizationStatusCodes.ENTEREDINERROR);
        } else {
            switch (vaccine.getCompletionStatus()) {
                case "CP": {
                    i.setStatus(Immunization.ImmunizationStatusCodes.COMPLETED);
                    break;
                }
                case "NA":
                case "PA":
                case "RE": {
                    i.setStatus(Immunization.ImmunizationStatusCodes.NOTDONE);
                    break;
                }
                case "":
                default: {
                    i.setStatus(null);
                    break;
                }
            }
        }
        CodeMap codeMap = codeMapManager.getCodeMap();
        Collection<Code> codeRoute = codeMap.getCodesForTable(CodesetType.BODY_ROUTE);
        for (Code c : codeRoute) {
            if (c.getValue().equals(vaccine.getBodyRoute())) {
                i.setRoute(new CodeableConcept(new Coding(c.getConceptType(), c.getValue(), c.getLabel())));
            }
        }

        i.addReason().setConcept(new CodeableConcept(new Coding(REFUSAL_REASON_CODE, vaccine.getRefusalReasonCode(), vaccine.getRefusalReasonCode())));
        i.getSite().addCoding().setSystem(BODY_PART).setCode(vaccine.getBodySite());
        i.getRoute().addCoding().setSystem(BODY_ROUTE).setCode(vaccine.getBodyRoute());
        i.getFundingSource().addCoding().setSystem(FUNDING_SOURCE).setCode(vaccine.getFundingSource());
        i.addProgramEligibility().setProgram(new CodeableConcept(new Coding().setSystem(FUNDING_ELIGIBILITY).setCode(vaccine.getFinancialStatus())));
        i.getInformationSource().setConcept(new CodeableConcept(new Coding().setSystem(INFORMATION_SOURCE).setCode(vaccine.getInformationSource())));
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
        ve.setVaccine(toVaccine(i));
        ve.setPrimarySource(i.getPrimarySource());
//    ve.setExternalLink(i.getIdentifierFirstRep().getValue());
        if (i.getPatient() != null && i.getPatient().getReference() != null && !i.getPatient().getReference().isBlank()) {
        }
        if (i.getLocation() != null && i.getLocation().getReference() != null && !i.getLocation().getReference().isBlank()) {
//      ve.setOrgLocation(fhirRequests.readOrgLocation(i.getLocation().getReference()));
        }
//    if (i.hasInformationSourceReference() && i.getInformationSourceReference().getReference() != null && !i.getInformationSourceReference().getReference().isBlank()) {
//      Integer informationSourceId = Integer.parseInt(i.getInformationSourceReference().getReference().split("Clinician/")[1]); // TODO
//      ve.setEnteringClinician(clinicianRepository.findById(informationSourceId).get());
//    }
        for (Immunization.ImmunizationPerformerComponent performer : i.getPerformer()) {
            if (performer.getActor() != null && performer.getActor().getReference() != null && !performer.getActor().getReference().isBlank()) {
                Integer performerId = Integer.parseInt(performer.getActor().getReference().split("Clinician/")[1]); // TODO
                switch (performer.getFunction().getCode(FUNCTION)) {
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
        return ve;
    }

    public Vaccine toVaccine(Immunization i) {
        Vaccine v = new Vaccine();
        v.setUpdatedDate(i.getMeta().getLastUpdated());

//    v.setCreatedDate(i.getRecorded());
        v.setAdministeredDate(i.getOccurrenceDateTimeType().getValue());

        v.setVaccineCvxCode(i.getVaccineCode().getCode(CVX));
        v.setVaccineNdcCode(i.getVaccineCode().getCode(NDC));
        v.setVaccineMvxCode(i.getVaccineCode().getCode(MVX));

        v.setVaccineMvxCode(i.getManufacturer().getReference().getIdentifier().getValue());

        v.setAdministeredAmount(i.getDoseQuantity().getValue().toString());

        v.setUpdatedDate(new Date());

        v.setLotNumber(i.getLotNumber());
        v.setExpirationDate(i.getExpirationDate());
        if (i.getStatus() != null) {
            switch (i.getStatus()) {
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
        if (i.hasProgramEligibility()) {
            v.setFinancialStatus(i.getProgramEligibilityFirstRep().getProgram().getCodingFirstRep().getCode());
        }
        if (i.hasInformationSource() && i.getInformationSource().getConcept() != null) {
            v.setInformationSource(i.getInformationSource().getConcept().getCodingFirstRep().getCode());
        }

        return v;
    }


}
