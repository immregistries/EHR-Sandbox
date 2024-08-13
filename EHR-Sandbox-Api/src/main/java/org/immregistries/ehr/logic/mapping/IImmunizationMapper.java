package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;

public interface IImmunizationMapper<Immunization extends IBaseResource> extends IEhrEntityFhirMapper<VaccinationEvent> {
    // TODO Constants need to be harmonized with IIS Sandbox
    String CVX = "http://hl7.org/fhir/sid/cvx";
    String MVX = "http://terminology.hl7.org/CodeSystem/MVX";
    String NDC = "NDC";
    //    String INFORMATION_SOURCE = "informationSource";
    String FUNCTION = "iis-sandbox-function";
    String ORDERING = "ordering";
    String ENTERING = "entering";
    String ADMINISTERING = "administering";
//    String REFUSAL_REASON_CODE = "refusalReasonCode";
//    String BODY_PART = "bodyPart";
//    String BODY_ROUTE = "bodyRoute";
//    String FUNDING_SOURCE = "fundingSource";
//    String FUNDING_ELIGIBILITY = "fundingEligibility";

    Immunization toFhir(VaccinationEvent vaccination, String identifier_system);

    VaccinationEvent toVaccinationEvent(Immunization i);

    VaccinationEvent toVaccinationEvent(Facility facility, Immunization i);

    Vaccine toVaccine(Immunization i);
}
