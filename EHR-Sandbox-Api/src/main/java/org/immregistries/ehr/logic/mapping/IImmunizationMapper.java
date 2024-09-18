package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.entities.Vaccine;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

public interface IImmunizationMapper<Immunization extends IBaseResource> extends IEhrEntityFhirMapper<VaccinationEvent> {
    // TODO Constants need to be harmonized with IIS Sandbox
    String CVX_SYSTEM = "http://hl7.org/fhir/sid/cvx";
    String MVX_SYSTEM = "http://hl7.org/fhir/sid/mvx";
    String NDC_SYSTEM = "http://hl7.org/fhir/sid/ndc";
    String FUNCTION_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-function";
    String ORDERING = "OP";
    String ENTERING = "entering";
    String ADMINISTERING = "AP";

    String INFORMATION_SOURCE_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-origin";
    String REFUSAL_REASON_CODE_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-reason";
    String BODY_SITE_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-site";
    String BODY_ROUTE_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-route";
    String FUNDING_SOURCE_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-funding-source";
    String FUNDING_ELIGIBILITY_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-program-eligibility";
//    String FUNDING_PROGRAMM_SYSTEM = "http://hl7.org/fhir/ValueSet/immunization-vaccine-funding-program";

    Immunization toFhir(VaccinationEvent vaccination, String identifier_system);

    /**
     * Used to extract identifier for Request Parameter when resource comes from parsing another request
     *
     * @param immunization
     * @return
     */
    EhrIdentifier extractImmunizationIdentifier(IBaseResource immunization);

    VaccinationEvent toVaccinationEvent(Immunization i);

    VaccinationEvent toVaccinationEvent(Facility facility, Immunization i);

    Vaccine toVaccine(Immunization i);
}
