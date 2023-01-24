package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;

public interface IResourceClient<Identifier> {
    String read(String resourceType, String resourceId, ImmunizationRegistry ir);
    String read(String resourceType, String resourceId, String iisUrl, String tenantId, String username, String password);
    MethodOutcome create(IBaseResource resource, ImmunizationRegistry ir);
    MethodOutcome create(IBaseResource resource, String iisUrl, String tenantId, String username, String password);
    MethodOutcome update(IBaseResource resource, String resourceId, ImmunizationRegistry ir);

    MethodOutcome delete(String resourceType, String resourceId, ImmunizationRegistry ir);


    MethodOutcome delete(String resourceType, String iisUrl, String resourceId, String tenantId, String username, String password);
    MethodOutcome update(IBaseResource resource, String resourceId, String iisUrl, String tenantId, String username, String password);
    MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, ImmunizationRegistry ir);
    public MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, String iisUrl, String tenantId, String username, String password);



    }
