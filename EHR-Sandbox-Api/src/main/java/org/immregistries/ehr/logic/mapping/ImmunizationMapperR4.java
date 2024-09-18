package org.immregistries.ehr.logic.mapping;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ImmunizationMapperR4 implements IImmunizationMapper<Immunization> {

    @Autowired
    ClinicianRepository clinicianRepository;
    @Autowired
    ResourceIdentificationService resourceIdentificationService;
    @Autowired
    MappingHelperR4 mappingHelperR4;

    public Immunization toFhir(VaccinationEvent vaccinationEvent, String identifier_system) {
        Immunization i = toFhir(vaccinationEvent);
        Identifier identifier = i.addIdentifier();
        identifier.setValue(vaccinationEvent.getId());
        identifier.setSystem(identifier_system);
//        i.setPatient(new Reference("Patient/" + vaccinationEvent.getPatient().getId())
//                .setIdentifier(new Identifier()
//                        .setValue("" + vaccinationEvent.getPatient().getId())
//                        .setSystem(identifier_system)));
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
                        .setSystem(MVX_SYSTEM)
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
        i.getRoute().addCoding(mappingHelperR4.codingFromCodeset(vaccine.getBodyRoute(), BODY_ROUTE_SYSTEM, CodesetType.BODY_ROUTE));
        i.setStatusReason(new CodeableConcept(mappingHelperR4.codingFromCodeset(vaccine.getRefusalReasonCode(), REFUSAL_REASON_CODE_SYSTEM, CodesetType.VACCINATION_REFUSAL)));
        i.getSite().addCoding(mappingHelperR4.codingFromCodeset(vaccine.getBodySite(), BODY_SITE_SYSTEM, CodesetType.BODY_SITE));
        i.getFundingSource().addCoding(mappingHelperR4.codingFromCodeset(vaccine.getFundingSource(), FUNDING_SOURCE_SYSTEM, CodesetType.VACCINATION_FUNDING_SOURCE));
        i.addProgramEligibility().addCoding(mappingHelperR4.codingFromCodeset(vaccine.getFinancialStatus(), FUNDING_ELIGIBILITY_SYSTEM, CodesetType.FINANCIAL_STATUS_CODE));
//        i.getInformationSource().setConcept(new CodeableConcept(toFhirCoding(CodesetType.VACCINATION_INFORMATION_SOURCE, vaccine.getInformationSource())));

        i.setReportOrigin(new CodeableConcept(mappingHelperR4.codingFromCodeset(vaccine.getInformationSource(), INFORMATION_SOURCE_SYSTEM, CodesetType.VACCINATION_INFORMATION_SOURCE)));

        i.getVaccineCode().addCoding(mappingHelperR4.codingFromCodeset(vaccine.getVaccineCvxCode(), CVX_SYSTEM, CodesetType.VACCINATION_CVX_CODE));

        i.getVaccineCode().addCoding(mappingHelperR4.codingFromCodeset(vaccine.getVaccineNdcCode(), NDC_SYSTEM, CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE));

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
                    switch (MappingHelperR4.codeFromSystemOrDefault(performer.getFunction(), FUNCTION_SYSTEM)) {
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
            try {
                v.setAdministeredDate(simpleDateFormat.parse(i.getOccurrenceStringType().getValueNotNull()));
            } catch (ParseException e) {
                e.printStackTrace();
//        throw new RuntimeException(e);
            }
        }

        i.getVaccineCode().getCoding().forEach(coding -> {
            switch (coding.getSystem()) {
                case CVX_SYSTEM: {
                    v.setVaccineCvxCode(coding.getCode());
                    break;
                }
                case NDC_SYSTEM: {
                    v.setVaccineNdcCode(coding.getCode());
                    break;
                }
                case MVX_SYSTEM: {
                    v.setVaccineMvxCode(coding.getCode());
                    break;
                }
            }
        });

        if (i.getManufacturer().hasIdentifier() && MVX_SYSTEM.equals(i.getManufacturer().getIdentifier().getSystem())) {
            v.setVaccineMvxCode(i.getManufacturer().getIdentifier().getValue());
        }

        if (i.getDoseQuantity().getValue() != null) {
            v.setAdministeredAmount(i.getDoseQuantity().getValue().toString());
        }

        v.setInformationSource(MappingHelperR4.codeFromSystemOrDefault(i.getReportOrigin(), INFORMATION_SOURCE_SYSTEM));
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
        v.setRefusalReasonCode(MappingHelperR4.codeFromSystemOrDefault(i.getStatusReason(), REFUSAL_REASON_CODE_SYSTEM));
        v.setBodySite(MappingHelperR4.codeFromSystemOrDefault(i.getSite(), BODY_SITE_SYSTEM));
        v.setBodyRoute(MappingHelperR4.codeFromSystemOrDefault(i.getRoute(), BODY_ROUTE_SYSTEM));
        v.setFundingSource(MappingHelperR4.codeFromSystemOrDefault(i.getFundingSource(), FUNDING_SOURCE_SYSTEM));
        v.setFinancialStatus(MappingHelperR4.codeFromSystemOrDefault(i.getProgramEligibilityFirstRep(), FUNDING_ELIGIBILITY_SYSTEM));

        return v;
    }

    public Immunization.ImmunizationPerformerComponent fhirPerformer(Clinician clinician, String function) {
        if (clinician != null) {
            return new Immunization.ImmunizationPerformerComponent().setActor(new Reference()
                            .setIdentifier(IPractitionerMapper.clinicianEhrIdentifier(clinician).toR4()))
                    .setFunction(new CodeableConcept(new Coding().setSystem(FUNCTION_SYSTEM).setCode(function)));
        } else {
            return null;
        }
    }


}
