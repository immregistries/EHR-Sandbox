package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.FhirComponentsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

public abstract class ResourceClient<Identifier> implements IResourceClient<Identifier> {

    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired()
    FhirComponentsService fhirComponentsService;


    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public String read(String resourceType, String resourceId, ImmunizationRegistry ir) {
        return read(resourceType, resourceId, fhirComponentsService.clientFactory().newGenericClient(ir));
    }

    public MethodOutcome create(IBaseResource resource, ImmunizationRegistry ir) {
        return create(resource, fhirComponentsService.clientFactory().newGenericClient(ir));
    }

    public MethodOutcome delete(String resourceType, String resourceId, ImmunizationRegistry ir) {
        return delete(resourceType, resourceId, fhirComponentsService.clientFactory().newGenericClient(ir));
    }

    public MethodOutcome update(IBaseResource resource, String resourceId, ImmunizationRegistry ir) {
        return update(resource, resourceId, fhirComponentsService.clientFactory().newGenericClient(ir));
    }

    public String read(String resourceType, String resourceId, IGenericClient client) {
        IBaseResource resource;
        resource = client.read().resource(resourceType).withId(resourceId).execute();
        return fhirComponentsService.fhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    public MethodOutcome create(IBaseResource resource, IGenericClient client) {
        MethodOutcome outcome;
        outcome = client.create().resource(resource).execute();
        return outcome;
    }

}
