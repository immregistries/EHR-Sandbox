package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.Client.CustomClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ResourceClient {

    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    FhirContext fhirContext;
    @Autowired
    CustomClientBuilder customClientBuilder;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private String add_timestamp(String message){
        LocalDateTime now = LocalDateTime.now();  
        return dtf.format(now) + " " + message;
    }

    public String read(String resourceType, String resourceId, ImmunizationRegistry ir){
        return read(resourceType, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public MethodOutcome create(IBaseResource resource, ImmunizationRegistry ir){
        return create(resource, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public MethodOutcome delete(String resourceType, String resourceId, ImmunizationRegistry ir){
        return delete(resourceType, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public MethodOutcome update(IBaseResource resource, String resourceId, ImmunizationRegistry ir){
        return update(resource, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }
  
  
    public String read(String resourceType, String resourceId, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = customClientBuilder.newGenericClient(iisUrl, tenantId, username, password);
        IBaseResource resource;
        resource = client.read().resource(resourceType).withId(resourceId).execute();
        return fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
    }
  
  
    public MethodOutcome create(IBaseResource resource, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = customClientBuilder.newGenericClient(iisUrl, tenantId, username, password);
        MethodOutcome outcome;
        outcome = client.create().resource(resource).execute();
        return outcome;
    }

  
    public MethodOutcome delete(String resourceType, String iisUrl, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = customClientBuilder.newGenericClient(iisUrl, tenantId, username, password);
        MethodOutcome outcome = client.delete().resourceById(new IdType(resourceType, resourceId)).execute();
        OperationOutcome opeOutcome = (OperationOutcome) outcome.getOperationOutcome();
        return outcome;
    }

    public MethodOutcome update(IBaseResource resource, String resourceId, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = customClientBuilder.newGenericClient(iisUrl, tenantId, username, password);
        MethodOutcome outcome = client.update().resource(resource).execute();
        return outcome;
    }

    public MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, ImmunizationRegistry ir) {
        return  updateOrCreate(resource, type, identifier, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }
    public MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = customClientBuilder.newGenericClient(iisUrl, tenantId, username, password);
        MethodOutcome outcome;
        try {
            if (identifier != null && identifier.getValue() != null && !identifier.getValue().isEmpty()) {
                outcome = client.update().resource(resource).conditionalByUrl(
                        type+"?identifier="+identifier.getSystem()+"|"+identifier.getValue()
                                + "&_tag:not=http://hapifhir.io/fhir/NamingSystem/mdm-record-status|GOLDEN_RECORD"
                ).execute();
            } else {
                outcome = client.create().resource(resource).execute();
            }

        } catch (ResourceNotFoundException | InvalidRequestException e) {
            outcome = client.create().resource(resource).execute();
        }
        return outcome;
    }
    
}
