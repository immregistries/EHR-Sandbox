package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

import static org.immregistries.ehr.logic.ResourceIdentificationService.FACILITY_SYSTEM;

public interface IOrganizationMapper<Organization extends IBaseResource> extends IEhrEntityFhirMapper<Facility> {
    Organization toFhir(Facility facility);

    Facility facilityFromFhir(Organization organization);

    static EhrIdentifier facilityEhrIdentifier(Facility facility) {
        EhrIdentifier ehrIdentifier = new EhrIdentifier();
        ehrIdentifier.setSystem(FACILITY_SYSTEM);
        ehrIdentifier.setValue(facility.getId());
        return ehrIdentifier;
    }


}
