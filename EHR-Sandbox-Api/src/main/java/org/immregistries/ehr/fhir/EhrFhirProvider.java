package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.IdType;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;

public interface EhrFhirProvider<ResourceType extends IBaseResource> {
    Class<ResourceType> getResourceType();
    MethodOutcome update(ResourceType resourceType, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);
//    MethodOutcome delete(IdType theId, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);
    MethodOutcome deleteConditional(IdType theId, String theConditionalUrl, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry);


}
