package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Organization;
import org.hl7.fhir.r5.model.Reference;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import static org.immregistries.ehr.logic.ResourceIdentificationService.FACILITY_SYSTEM;

@Service
@Conditional(OnR5Condition.class)
public class OrganizationMapperR5 implements IOrganizationMapper<Organization> {

    public Organization toFhir(Facility facility) {
        Organization organization = new Organization();
        organization.addIdentifier().setSystem(FACILITY_SYSTEM).setValue(String.valueOf(facility.getId()));
        organization.setName(facility.getNameDisplay());
        if (facility.getParentFacility() != null) {
            organization.setPartOf(new Reference().setIdentifier(facilityIdentifier(facility.getParentFacility())));
        }
        return organization;
    }

    public Facility facilityFromFhir(Organization organization) {
        Facility facility =new Facility();
        facility.setNameDisplay(organization.getName());
//        if (organization.hasPartOf()) {
//            facility.setParentFacility();
//        }
        return facility;
    }

    public Tenant tenantFromFhir(Organization organization) {
        Tenant tenant = new Tenant();
        tenant.setNameDisplay(organization.getName());
        return tenant;
    }

    public Reference facilityReference(Facility facility) {
        return new Reference().setType("Organization").setIdentifier( new Identifier().setSystem(FACILITY_SYSTEM).setValue(facility.getId()));
    }

    public Identifier facilityIdentifier(Facility facility) {
        return new Identifier().setSystem(FACILITY_SYSTEM).setValue(facility.getId());
    }
}
