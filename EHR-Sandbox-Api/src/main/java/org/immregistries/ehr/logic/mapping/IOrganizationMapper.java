package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.Facility;

public interface IOrganizationMapper<Organization extends IBaseResource> extends IEhrEntityFhirMapper<Facility> {
    Organization toFhir(Facility facility);
    Facility facilityFromFhir(Organization organization);

}
