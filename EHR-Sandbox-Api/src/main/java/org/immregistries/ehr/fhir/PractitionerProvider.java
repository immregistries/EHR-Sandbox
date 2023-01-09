package org.immregistries.ehr.fhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.Practitioner;
import org.immregistries.ehr.api.entities.Clinician;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.logic.mapping.PractitionnerMapperR4;
import org.immregistries.ehr.logic.mapping.PractitionnerMapperR5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PractitionerProvider implements IResourceProvider {
    @Autowired
    private PractitionnerMapperR4 practitionnerHandler;
    @Autowired
    private FacilityRepository facilityRepository;

    @Override
    public Class<Practitioner> getResourceType() {
        return Practitioner.class;
    }

    @Create
    public MethodOutcome createPractitioner(@ResourceParam Practitioner practitioner, RequestDetails requestDetails) {
        return createPractitioner(practitioner,
                facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    public MethodOutcome createPractitioner(Practitioner practitioner, Facility facility) {
        MethodOutcome methodOutcome = new MethodOutcome();
        Clinician clinician = practitionnerHandler.fromFhir(practitioner);
        // TODO set received information status and make sure history of patient info if already exists
        return methodOutcome;
    }
}
