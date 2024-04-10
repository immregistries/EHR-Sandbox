package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.Clinician;

public interface IPractitionerMapper<Practitioner extends IBaseResource> extends IEhrEntityFhirMapper<Clinician> {
    Practitioner toFhir(Clinician clinician);
    Clinician toClinician(Practitioner practitioner);
}
