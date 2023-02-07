package org.immregistries.ehr.fhir.ServerR4;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.fhir.annotations.OnR4Condition;
import org.immregistries.ehr.logic.mapping.PatientMapperR4;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Controller
@Conditional(OnR4Condition.class)
public class PatientProviderR4 implements IResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(PatientProviderR4.class);

    @Autowired
    private PatientMapperR4 patientHandler;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Create
    public MethodOutcome createPatient(@ResourceParam Patient fhirPatient, RequestDetails requestDetails) {
        return createPatient(fhirPatient,
                facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    public MethodOutcome createPatient(Patient fhirPatient, Facility facility) {
        MethodOutcome methodOutcome = new MethodOutcome();
        EhrPatient patient = patientHandler.fromFhir(fhirPatient);
        patient.setFacility(facility);
        patient.setTenant(facility.getTenant());
        patient.setCreatedDate(new Date());
        patient.setUpdatedDate(new Date());
        // TODO set received information status and make sure history of patient info if already exists
        patient = patientRepository.save(patient);
        return methodOutcome.setId(new IdType().setValue(patient.getId().toString()));
    }

}
