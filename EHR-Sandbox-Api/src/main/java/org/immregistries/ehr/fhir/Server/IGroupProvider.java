package org.immregistries.ehr.fhir.Server;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;

public interface IGroupProvider<Group extends IBaseResource> extends IResourceProvider {

    MethodOutcome update(Group group, ServletRequestDetails requestDetails);

    MethodOutcome update(Group group, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);

    MethodOutcome update(Group group, Facility facility, ImmunizationRegistry immunizationRegistry);

    MethodOutcome create(Group group, ServletRequestDetails requestDetails);

    MethodOutcome create(Group group, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);

    MethodOutcome create(Group group, Facility facility, ImmunizationRegistry immunizationRegistry);

    //May not belong here TODO
    Group getRemoteGroup(IGenericClient client, EhrGroup ehrGroup);
}
