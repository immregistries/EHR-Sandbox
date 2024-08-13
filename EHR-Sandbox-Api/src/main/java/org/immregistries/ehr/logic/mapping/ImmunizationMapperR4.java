package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.fhir.annotations.OnR4Condition;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Maps the Database with FHIR for the immunization resources
 */
@Service
@Conditional(OnR4Condition.class)
public class ImmunizationMapperR4 implements IImmunizationMapper<Immunization> {

    @Autowired
    CodeMapManager codeMapManager;
    @Autowired
    ClinicianRepository clinicianRepository;
    @Autowired
    PractitionerMapperR4 practitionerMapper;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;

    public Immunization toFhir(VaccinationEvent dbVaccination, String identifier_system) {
        Immunization i = toFhir(dbVaccination);
        Identifier identifier = i.addIdentifier();
        identifier.setValue("" + dbVaccination.getId());
        identifier.setSystem(identifier_system);
//        i.setPatient(new Reference("Patient/" + dbVaccination.getPatient().getId())
//                .setIdentifier(new Identifier()
//                        .setValue("" + dbVaccination.getPatient().getId())
//                        .setSystem(identifier_system)));
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
                    .setIdentifier(mrnIdentifier.toR4()));
        }
        i.setRecorded(vaccine.getCreatedDate());
        i.setLotNumber(vaccine.getLotNumber());
        i.getOccurrenceDateTimeType().setValue(vaccine.getAdministeredDate());
        i.setDoseQuantity(new Quantity());
        if (StringUtils.isNotBlank(vaccine.getAdministeredAmount())) {
            i.getDoseQuantity().setValue(new BigDecimal(vaccine.getAdministeredAmount()));
        }
        i.setExpirationDate(vaccine.getExpirationDate());

        i.setManufacturer(new Reference()
                .setType("Organisation")
                .setIdentifier(new Identifier()
                        .setSystem(MVX)
                        .setValue(vaccine.getVaccineMvxCode())));


        i.setExpirationDate(vaccine.getExpirationDate());

        if (vaccine.getActionCode().equals("D")) {
            i.setStatus(Immunization.ImmunizationStatus.ENTEREDINERROR);
        } else {
            switch (vaccine.getCompletionStatus()) {
                case "CP": {
                    i.setStatus(Immunization.ImmunizationStatus.COMPLETED);
                    break;
                }
                case "NA":
                case "PA":
                case "RE": {
                    i.setStatus(Immunization.ImmunizationStatus.NOTDONE);
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
        i.setStatusReason(new CodeableConcept(toFhirCoding(CodesetType.VACCINATION_REFUSAL, vaccine.getRefusalReasonCode())));
        i.getSite().addCoding(toFhirCoding(CodesetType.BODY_SITE, vaccine.getBodySite()));
        i.getFundingSource().addCoding(toFhirCoding(CodesetType.VACCINATION_FUNDING_SOURCE, vaccine.getFundingSource()));
        i.addProgramEligibility().addCoding(toFhirCoding(CodesetType.FINANCIAL_STATUS_CODE, vaccine.getFinancialStatus()));
//        i.getInformationSource().setConcept(new CodeableConcept(toFhirCoding(CodesetType.VACCINATION_INFORMATION_SOURCE, vaccine.getInformationSource())));

        i.getVaccineCode().addCoding(toFhirCoding(CodesetType.VACCINATION_CVX_CODE, CVX, vaccine.getVaccineCvxCode()));
        i.getVaccineCode().addCoding(toFhirCoding(CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE, NDC, vaccine.getVaccineCvxCode()));

        i.addPerformer(fhirPerformer(vaccinationEvent.getEnteringClinician(), ENTERING));
        i.addPerformer(fhirPerformer(vaccinationEvent.getOrderingClinician(), ORDERING));
        i.addPerformer(fhirPerformer(vaccinationEvent.getAdministeringClinician(), ADMINISTERING));
        return i;
    }

    public VaccinationEvent toVaccinationEvent(Immunization i) {
        VaccinationEvent ve = new VaccinationEvent();
        ve.setVaccine(toVaccine(i));
//    ve.setExternalLink(i.getIdentifierFirstRep().getValue());
        if (i.getPatient() != null && i.getPatient().getReference() != null && !i.getPatient().getReference().isBlank()) {
        }
        if (i.getLocation() != null && i.getLocation().getReference() != null && !i.getLocation().getReference().isBlank()) {
//      ve.setOrgLocation(fhirRequests.readOrgLocation(i.getLocation().getReference()));
        }
//    if (i.hasInformationSourceReference() && i.getInformationSourceReference().getReference() != null && !i.getInformationSourceReference().getReference().isBlank()) {
//      Integer informationSourceId = Integer.parseInt(i.getInformationSourceReference().getReference().split("Clinician/")[1]);
//      ve.setEnteringClinician(clinicianRepository.findById(informationSourceId).get());
//    }

        return ve;
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
                    switch (performer.getFunction().getCodingFirstRep().getCode()) {
                        case ADMINISTERING: {
                            ve.setAdministeringClinician(clinician);
                            break;
                        }
                        case ORDERING: {
                            ve.setOrderingClinician(clinician);
                            break;
                        }
                    }
                }
            }
        }
        return ve;
    }


    public Vaccine toVaccine(Immunization i) {
        Vaccine v = new Vaccine();
        v.setUpdatedDate(i.getMeta().getLastUpdated());
        v.setUpdatedDate(i.getMeta().getLastUpdated());
//		v.setVaccinationReportedExternalLink(MappingHelper.filterIdentifier(i.getIdentifier(),MappingHelper.VACCINATION_REPORTED).getValue());

        v.setCreatedDate(i.getRecorded());
        if (i.hasOccurrenceDateTimeType()) {
            v.setAdministeredDate(i.getOccurrenceDateTimeType().getValue());
        } else if (i.hasOccurrenceStringType()) {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-mm-dd");
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

        if (i.getDoseQuantity().getValue() != null) {
            v.setAdministeredAmount(i.getDoseQuantity().getValue().toString());
        }

        v.setInformationSource(i.getReportOrigin().getCodingFirstRep().getCode());
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
        v.setRefusalReasonCode(i.getStatusReason().getCodingFirstRep().getCode());
        v.setBodySite(i.getSite().getCodingFirstRep().getCode());
        v.setBodyRoute(i.getRoute().getCodingFirstRep().getCode());
        v.setFundingSource(i.getFundingSource().getCodingFirstRep().getCode());
        v.setFinancialStatus(i.getProgramEligibilityFirstRep().getCodingFirstRep().getCode());

        return v;
    }

    private Coding toFhirCoding(Code code) {
        Coding coding = new Coding(code.getConceptType(), code.getValue(), code.getLabel());
        return coding;
    }

    private Coding toFhirCoding(CodesetType codesetType, String system, String value) {
        return toFhirCoding(codesetType, value).setSystem(system);
    }

    private Coding toFhirCoding(CodesetType codesetType, String value) {
        Code code = codeMapManager.getCodeMap().getCodeForCodeset(codesetType, value);
        if (code != null) {
            return toFhirCoding(code);
        } else {
            return new Coding().setCode(value);
        }
    }

    private Immunization.ImmunizationPerformerComponent fhirPerformer(Clinician clinician, String function) {
        if (clinician != null) {
            return new Immunization.ImmunizationPerformerComponent().setActor(new Reference()
                            .setIdentifier(practitionerMapper.toFhir(clinician).getIdentifierFirstRep()))
                    .setFunction(new CodeableConcept(new Coding(FUNCTION, function, function)));
        } else {
            return null;
        }
    }


}
