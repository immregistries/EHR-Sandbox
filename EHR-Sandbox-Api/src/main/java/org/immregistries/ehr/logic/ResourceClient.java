package org.immregistries.ehr.logic;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ResourceClient {

    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final FhirContext CTX = CustomClientBuilder.getCTX();

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static String add_timestamp(String message){
        LocalDateTime now = LocalDateTime.now();  
        return dtf.format(now) + " " + message;
    }

    public static String read(String resourceType, String resourceId, ImmunizationRegistry ir){
        return read(resourceType, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static MethodOutcome write(IBaseResource resource, ImmunizationRegistry ir){
        return write(resource, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static MethodOutcome delete(String resourceType, String resourceId, ImmunizationRegistry ir){
        return delete(resourceType, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static MethodOutcome update(IBaseResource resource, String resourceId, ImmunizationRegistry ir){
        return update(resource, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }
  
  
    public static String read(String resourceType, String resourceId, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        IBaseResource resource;
        resource = client.read().resource(resourceType).withId(resourceId).execute();
        return CTX.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
    }
  
  
    public static MethodOutcome write(IBaseResource resource, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        MethodOutcome outcome;
        outcome = client.create().resource(resource).execute();
        return outcome;
    }

  
    public static MethodOutcome delete(String resourceType, String iisUrl, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        MethodOutcome outcome = client.delete().resourceById(new IdType(resourceType, resourceId)).execute();
        OperationOutcome opeOutcome = (OperationOutcome) outcome.getOperationOutcome();
        return outcome;
    }

    public static MethodOutcome update(IBaseResource resource, String resourceId, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        MethodOutcome outcome = client.update().resource(resource).execute();
        return outcome;
    }

    public static MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, ImmunizationRegistry ir) {
        return  updateOrCreate(resource, type, identifier, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }
    public static MethodOutcome updateOrCreate(IBaseResource resource, String type, Identifier identifier, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        MethodOutcome outcome;
        try {
            outcome = client.update().resource(resource).conditionalByUrl(
                    type+"?identifier="+identifier.getSystem()+"|"+identifier.getValue()
                            + "&_tag:not=https://hapifhir.io/fhir/NamingSystem/mdm-record-status|GOLDEN_RECORD"
            ).execute();
        } catch (ResourceNotFoundException | InvalidRequestException e) {
            outcome = client.create().resource(resource).execute();
        }
        return outcome;
    }
    
}
