package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.Organization;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Tenant;
import org.springframework.stereotype.Service;

@Service
public class OrganizationMapperR5 {

    public Organization toFhirOrganization(Facility facility) {
        Organization organization = new Organization();
        organization.addIdentifier().setSystem("ehr-sandbox/facilities").setValue(String.valueOf(facility.getId()));
        organization.setName(facility.getNameDisplay());
        if (facility.getParentFacility() != null) {
//            organization.se
        }
        return organization;
    }

    public Facility facilityFromFhir(Organization organization) {
        Facility facility =new Facility();
        facility.setNameDisplay(organization.getName());
        return facility;
    }

    public Tenant tenantFromFhir(Organization organization) {
        Tenant tenant = new Tenant();
        tenant.setNameDisplay(organization.getName());
        return tenant;
    }
}
