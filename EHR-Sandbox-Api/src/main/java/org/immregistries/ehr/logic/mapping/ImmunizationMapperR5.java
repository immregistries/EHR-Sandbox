package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * Maps the Database with FHIR for the immunization resources
 */
@Service

public class ImmunizationMapperR5 implements IImmunizationMapper<Immunization> {

    @Autowired
    CodeMapManager codeMapManager;
    @Autowired
    ClinicianRepository clinicianRepository;
    @Autowired
    EhrPatientRepository ehrPatientRepository;
    @Autowired
    PractitionerMapperR5 practitionerMapper;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;
    @Autowired
    MappingHelperR5 mappingHelperR5;


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
        i.getRoute().addCoding(toFhirCoding(CodesetType.BODY_ROUTE, vaccine.getBodyRoute()));
        i.addReason().setConcept(new CodeableConcept(toFhirCoding(CodesetType.VACCINATION_REFUSAL, vaccine.getRefusalReasonCode())));
        i.getSite().addCoding(toFhirCoding(CodesetType.BODY_SITE, vaccine.getBodySite()));
        i.getFundingSource().addCoding(toFhirCoding(CodesetType.VACCINATION_FUNDING_SOURCE, vaccine.getFundingSource()));
        i.addProgramEligibility().setProgram(new CodeableConcept(toFhirCoding(CodesetType.FINANCIAL_STATUS_CODE, vaccine.getFinancialStatus())));
        i.getInformationSource().setConcept(new CodeableConcept(toFhirCoding(CodesetType.VACCINATION_INFORMATION_SOURCE, vaccine.getInformationSource())));
        i.getVaccineCode().addCoding(toFhirCoding(CodesetType.VACCINATION_CVX_CODE, CVX, vaccine.getVaccineCvxCode()));
        i.getVaccineCode().addCoding(toFhirCoding(CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE, NDC, vaccine.getVaccineCvxCode()));

        i.setManufacturer(new CodeableReference(new Reference()
                .setType("Organisation")
                .setIdentifier(new Identifier()
                        .setSystem(MVX)
                        .setValue(vaccine.getVaccineMvxCode()))));

        i.addPerformer(fhirPerformer(vaccinationEvent.getEnteringClinician(), ENTERING));
        i.addPerformer(fhirPerformer(vaccinationEvent.getOrderingClinician(), ORDERING));
        i.addPerformer(fhirPerformer(vaccinationEvent.getAdministeringClinician(), ADMINISTERING));
        return i;
    }

    public VaccinationEvent toVaccinationEvent(Facility facility, Immunization i) {
        VaccinationEvent ve = toVaccinationEvent(i);
        if (i.getPatient() != null && i.getPatient().getReference() != null) {
            ve.setPatient(resourceIdentificationService.getLocalPatient(i.getPatient(), null, facility));
        }
        for (Immunization.ImmunizationPerformerComponent performer : i.getPerformer()) {
            if (performer.getActor() != null) {
                Clinician clinician = null;
                if (StringUtils.isNotBlank(performer.getActor().getReference())) {
                    String performerId = performer.getActor().getReference().split("Clinician/")[1]; // TODO
                    clinician = clinicianRepository.findByTenantIdAndId(facility.getTenant().getId(), performerId).orElse(null);
                }
                if (clinician != null) {
                    if (performer.getActor().hasIdentifier()) {
                        Identifier identifier = performer.getActor().getIdentifier();
                        clinician = clinicianRepository.findByTenantIdAndIdentifier(facility.getTenant().getId(), identifier.getSystem(), identifier.getValue()).orElse(null);
                    }
                }
                if (clinician != null) {
                    switch (performer.getFunction().getCode(FUNCTION)) {
                        case ADMINISTERING: {
                            ve.setAdministeringClinician(clinician);
                            break;
                        }
                        case ORDERING: {
                            ve.setOrderingClinician(clinician);
                            break;
                        }
                        case ENTERING: {
                            ve.setEnteringClinician(clinician);
                            break;
                        }
                    }
                }
            }
        }
        return ve;
    }

    public VaccinationEvent toVaccinationEvent(Immunization i) {
        VaccinationEvent ve = new VaccinationEvent();
        ve.setVaccine(toVaccine(i));
        ve.setPrimarySource(i.getPrimarySource());
        if (i.getLocation() != null && i.getLocation().getReference() != null && !i.getLocation().getReference().isBlank()) {
//      ve.setOrgLocation(fhirRequests.readOrgLocation(i.getLocation().getReference()));
        }
//    if (i.hasInformationSourceReference() && i.getInformationSourceReference().getReference() != null && !i.getInformationSourceReference().getReference().isBlank()) {
//      Integer informationSourceId = Integer.parseInt(i.getInformationSourceReference().getReference().split("Clinician/")[1]); // TODO
//      ve.setEnteringClinician(clinicianRepository.findById(informationSourceId).get());
//    }

        return ve;
    }

    public Vaccine toVaccine(Immunization i) {
        Vaccine v = new Vaccine();
        v.setUpdatedDate(i.getMeta().getLastUpdated());

//    v.setCreatedDate(i.getRecorded());
        v.setAdministeredDate(i.getOccurrenceDateTimeType().getValue());

        v.setVaccineCvxCode(i.getVaccineCode().getCode(CVX));
        v.setVaccineNdcCode(i.getVaccineCode().getCode(NDC));
        if (i.getManufacturer().getReference().hasIdentifier() && MVX.equals(i.getManufacturer().getReference().getIdentifier().getSystem())) {
            v.setVaccineMvxCode(i.getManufacturer().getReference().getIdentifier().getValue());
        }

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


    private Coding toFhirCoding(CodesetType codesetType, String system, String value) {
        return mappingHelperR5.codingFromCodeset(value, system, codesetType);

    }

    private Coding toFhirCoding(CodesetType codesetType, String value) {
        return mappingHelperR5.codingFromCodeset(value, "", codesetType);
    }

    public Immunization.ImmunizationPerformerComponent fhirPerformer(Clinician clinician, String function) {
        if (clinician != null) {
            return new Immunization.ImmunizationPerformerComponent().setActor(new Reference()
                            .setIdentifier(practitionerMapper.toFhir(clinician).getIdentifierFirstRep()))
                    .setFunction(new CodeableConcept(new Coding().setSystem(FUNCTION).setCode(function)));
        } else {
            return null;
        }
    }


}
