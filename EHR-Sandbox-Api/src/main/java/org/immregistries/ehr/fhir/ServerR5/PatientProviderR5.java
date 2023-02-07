package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Patient;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.PatientRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Controller
@Conditional(OnR5Condition.class)
public class PatientProviderR5 implements IResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(PatientProviderR5.class);

    @Autowired
    private PatientMapperR5 patientMapper;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

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
        EhrPatient patient = patientMapper.fromFhir(fhirPatient);
        patient.setFacility(facility);
        patient.setTenant(facility.getTenant());
        patient.setCreatedDate(new Date());
        patient.setUpdatedDate(new Date());
        // TODO set received information status and make sure history of patient info if already exists
        patient = patientRepository.save(patient);
        return methodOutcome.setId(new IdType().setValue(patient.getId().toString()));
    }

    @Update
    public MethodOutcome updatePatient(@ResourceParam Patient patient, RequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = null;
//                immunizationRegistryRepository
//                .findByUserIdAndIisFhirUrl(facility.getTenant().getUser().getId(), requestDetails.getHeader("origin")) // TODO find best way to determine origin, oAuth ? or subscription
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return  updatePatient(patient,requestDetails, immunizationRegistry);
    }

    public MethodOutcome updatePatient(@ResourceParam Patient patient, RequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        // TODO check facility & tenant
        /**
         * Fixing references with ids and stored ids
         * TODO identifiers
         * if not recognised store unmatched reference ?
         */
        String dbPatientId = resourceIdentificationService.getPatientLocalId(patient,immunizationRegistry,facility);
        // TODO Historical registering
        EhrPatient oldPatient = patientRepository.findById(dbPatientId).get();
        // Save old patient in other table or annotate or SQL Temporal Tables
        EhrPatient ehrPatient = patientMapper.fromFhir(patient);
        ehrPatient.setId(dbPatientId);
        ehrPatient.setCreatedDate(oldPatient.getCreatedDate());
        ehrPatient.setUpdatedDate(new Date());
        ehrPatient.setFacility(facility);
        ehrPatient.setTenant(facility.getTenant());

        ehrPatient = patientRepository.save(ehrPatient);
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setId(new IdType().setValue(ehrPatient.getId()));
        methodOutcome.setResource(patientMapper.dbPatientToFhirPatient(ehrPatient));
        return methodOutcome;
    }

}
