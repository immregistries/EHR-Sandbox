package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
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
    ClinicianRepository clinicianRepository;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;
    @Autowired
    MappingHelperR5 mappingHelperR5;


    public Immunization toFhir(VaccinationEvent vaccinationEvent, String identifier_system) {
        Immunization i = toFhir(vaccinationEvent);
        Identifier identifier = i.addIdentifier();
        identifier.setValue(String.valueOf(vaccinationEvent.getId()));
        identifier.setSystem(identifier_system);
//    i.setPatient(new Reference()
////            .setReference("Patient/" + vaccinationEvent.getPatient().getId())
//            .setIdentifier(new Identifier()
//                    .setValue(vaccinationEvent.getPatient().getId())
//                    .setSystem(patient_identifier_system)));
        return i;
    }

    public EhrIdentifier extractImmunizationIdentifier(IBaseResource iBaseResource) {
        Immunization immunization = (Immunization) iBaseResource;
        return new EhrIdentifier(immunization.getIdentifierFirstRep());
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
        i.getRoute().addCoding(mappingHelperR5.codingFromCodeset(vaccine.getBodyRoute(), BODY_ROUTE_SYSTEM, CodesetType.BODY_ROUTE));
        i.setStatusReason(new CodeableConcept(mappingHelperR5.codingFromCodeset(vaccine.getRefusalReasonCode(), REFUSAL_REASON_CODE_SYSTEM, CodesetType.VACCINATION_REFUSAL)));
        i.getSite().addCoding(mappingHelperR5.codingFromCodeset(vaccine.getBodySite(), BODY_SITE_SYSTEM, CodesetType.BODY_SITE));
        i.getFundingSource().addCoding(mappingHelperR5.codingFromCodeset(vaccine.getFundingSource(), FUNDING_SOURCE_SYSTEM, CodesetType.VACCINATION_FUNDING_SOURCE));
        i.addProgramEligibility().setProgramStatus(new CodeableConcept(mappingHelperR5.codingFromCodeset(vaccine.getFinancialStatus(), FUNDING_ELIGIBILITY_SYSTEM, CodesetType.FINANCIAL_STATUS_CODE)));
        i.getInformationSource().setConcept(new CodeableConcept(mappingHelperR5.codingFromCodeset(vaccine.getInformationSource(), INFORMATION_SOURCE_SYSTEM, CodesetType.VACCINATION_INFORMATION_SOURCE)));

        i.getVaccineCode().addCoding(mappingHelperR5.codingFromCodeset(vaccine.getVaccineCvxCode(), CVX_SYSTEM, CodesetType.VACCINATION_CVX_CODE));

        i.getVaccineCode().addCoding(
                mappingHelperR5.codingFromCodeset(
                        vaccine.getVaccineNdcCode(), NDC_SYSTEM, CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE));

        i.setManufacturer(new CodeableReference(new Reference()
                .setType("Organisation")
                .setIdentifier(new Identifier()
                        .setSystem(MVX_SYSTEM)
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
                    try {
                        Integer performerId = EhrUtils.convert(performer.getActor().getReference().split("Clinician/")[1]); // TODO
                        clinician = clinicianRepository.findByTenantIdAndId(facility.getTenant().getId(), performerId).orElse(null);
                    } catch (NumberFormatException numberFormatException) {
                    }
                }
                if (clinician != null) {
                    if (performer.getActor().hasIdentifier()) {
                        Identifier identifier = performer.getActor().getIdentifier();
                        clinician = clinicianRepository.findByTenantIdAndIdentifier(facility.getTenant().getId(), identifier.getSystem(), identifier.getValue()).orElse(null);
                    }
                }
                if (clinician != null) {
                    switch (MappingHelperR5.codeFromSystemOrDefault(performer.getFunction(), FUNCTION_SYSTEM)) {
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

        v.setVaccineCvxCode(i.getVaccineCode().getCode(CVX_SYSTEM));
        v.setVaccineNdcCode(i.getVaccineCode().getCode(NDC_SYSTEM));
        if (i.getManufacturer().getReference().hasIdentifier() && MVX_SYSTEM.equals(i.getManufacturer().getReference().getIdentifier().getSystem())) {
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
        v.setRefusalReasonCode(MappingHelperR5.codeFromSystemOrDefault(i.getReasonFirstRep().getConcept(), REFUSAL_REASON_CODE_SYSTEM));
        v.setBodySite(MappingHelperR5.codeFromSystemOrDefault(i.getSite(), BODY_SITE_SYSTEM));
        v.setBodyRoute(MappingHelperR5.codeFromSystemOrDefault(i.getRoute(), BODY_ROUTE_SYSTEM));

        v.setFundingSource(MappingHelperR5.codeFromSystemOrDefault(i.getFundingSource(), FUNDING_SOURCE_SYSTEM));
        if (i.hasProgramEligibility()) {
            v.setFinancialStatus(MappingHelperR5.codeFromSystemOrDefault(i.getProgramEligibilityFirstRep().getProgramStatus(), FUNDING_ELIGIBILITY_SYSTEM));
        }
        if (i.hasInformationSource() && i.getInformationSource().getConcept() != null) {
            v.setInformationSource(MappingHelperR5.codeFromSystemOrDefault(i.getInformationSource().getConcept(), INFORMATION_SOURCE_SYSTEM));
        }

        return v;
    }


    public Immunization.ImmunizationPerformerComponent fhirPerformer(Clinician clinician, String function) {
        if (clinician != null) {
            return new Immunization.ImmunizationPerformerComponent().setActor(new Reference()
                            .setIdentifier(IPractitionerMapper.clinicianEhrIdentifier(clinician).toR5()))
                    .setFunction(new CodeableConcept(new Coding().setSystem(FUNCTION_SYSTEM).setCode(function)));
        } else {
            return null;
        }
    }


}
