package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;

public interface IGroupMapper<Group extends IBaseResource> extends IEhrEntityFhirMapper<EhrGroup> {

    Group toFhir(EhrGroup ehrGroup);

    EhrGroup toEhrGroup(Group group);

    EhrGroup toEhrGroup(Group group, Facility facility, ImmunizationRegistry immunizationRegistry);

    /**
     * Used to extract identifier for Request Parameter when resource comes from parsing another request
     *
     * @param group resource
     * @return EhrIdentifier extracted form resource(usually the first)
     */
    EhrIdentifier extractGroupIdentifier(IBaseResource group);
}
