package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;

public interface IGroupMapper<Group extends IBaseResource> extends IEhrEntityFhirMapper<EhrGroup> {

    Group toFhir(EhrGroup ehrGroup);
    EhrGroup toEhrGroup(Group group);
    EhrGroup toEhrGroup(Group group, Facility facility, ImmunizationRegistry immunizationRegistry);
}
