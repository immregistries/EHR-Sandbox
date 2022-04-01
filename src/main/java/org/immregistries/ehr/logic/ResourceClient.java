package org.immregistries.ehr.logic;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
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

    public static String write(IBaseResource resource, ImmunizationRegistry ir){
        return write(resource, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static String delete(String resourceType, String resourceId, ImmunizationRegistry ir){
        return delete(resourceType, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }

    public static String update(IBaseResource resource, String resourceId, ImmunizationRegistry ir){
        return update(resource, resourceId, ir.getIisFhirUrl(), ir.getIisFacilityId(), ir.getIisUsername(), ir.getIisPassword());
    }
  
  
    public static String read(String resourceType, String resourceId, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        IBaseResource resource;
        String response;
        try {
            resource = client.read().resource(resourceType).withId(resourceId).execute();
            // resource = client.read().resource(Patient.class).withId(resourceId).execute();
            response = CTX.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
          } catch (ResourceNotFoundException e) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
            LocalDateTime now = LocalDateTime.now();  
            response = dtf.format(now) + "Resource not found";
            e.printStackTrace();
          }
        return response;
    }
  
  
    public static String write(IBaseResource resource, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        MethodOutcome outcome;
        String response;
        try {
           // Create the resource on the server
           outcome = client.create().resource(resource).execute();
           // Log the ID that the server assigned
           IIdType id = outcome.getId();
           if (id != null){  
                response = "Created resource, got ID: " + id.getIdPart();
           }else {
               response = "Response includes no id";
           }
        } catch (DataFormatException e) {
           response = "ERROR Writing FHIR Resource";
           e.printStackTrace();
        }
        return add_timestamp(response);
    }

  
    public static String delete(String resourceType, String iisUrl, String resourceId, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        String response;
        MethodOutcome outcome = client.delete().resourceById(new IdType(resourceType, resourceId)).execute();

        OperationOutcome opeOutcome = (OperationOutcome) outcome.getOperationOutcome();
        if (opeOutcome != null) {
            response = opeOutcome.getIssueFirstRep().getDetails().getCodingFirstRep().getCode();
        }
        else{
            response = "Resource not found";
        }
        return add_timestamp(response);
    }

    public static String update(IBaseResource resource, String resourceId, String iisUrl, String tenantId, String username, String password) {
        IGenericClient client = new CustomClientBuilder(iisUrl, tenantId, username, password).getClient();
        String response;
        MethodOutcome outcome = client.update().resource(resource).execute();

        IIdType id = outcome.getId();

        if (id != null){
            response = "Updated resource, got ID: " + id.getIdPart();
        }
        // OperationOutcome opeOutcome = (OperationOutcome) outcome.getOperationOutcome();

        // if (opeOutcome != null) {
        //     response = opeOutcome.getIssueFirstRep().getDetails().getCodingFirstRep().getCode();
        // }
        else{
            response = "Resource not found";
        }
        return add_timestamp(response);
    }
    
}
