package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.ResourceType;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.fhir.EhrFhirProvider;
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

import static org.immregistries.ehr.api.AuditRevisionListener.IMMUNIZATION_REGISTRY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;
import static org.immregistries.ehr.logic.mapping.PatientMapperR5.MRN_SYSTEM;

@Controller
@Conditional(OnR5Condition.class)
public class PatientProviderR5 implements IResourceProvider, EhrFhirProvider<Patient> {
    private static final Logger logger = LoggerFactory.getLogger(PatientProviderR5.class);

    @Autowired
    private PatientMapperR5 patientMapper;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }
    @Override
    public ResourceType getResourceName() {
        return ResourceType.Patient;
    }

    @Create
    public MethodOutcome create(@ResourceParam Patient fhirPatient, RequestDetails requestDetails) {
        return create(fhirPatient,
                facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    public MethodOutcome create(Patient patient, Facility facility) {
        MethodOutcome methodOutcome = new MethodOutcome();
        EhrPatient ehrPatient = patientMapper.toEhrPatient(patient);
        ehrPatient.setFacility(facility);
        // ehrPatient.setTenant(facility.getTenant());
        ehrPatient.setCreatedDate(new Date());
        ehrPatient.setUpdatedDate(new Date());
        // TODO set received information status and make sure history of patient info if already exists
        ehrPatient = patientRepository.save(ehrPatient);
        return methodOutcome.setId(new IdType().setValue(ehrPatient.getId().toString()));
    }

    /**
     * Currently unusable as is, as request
     * @param patient
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome update(@ResourceParam Patient patient, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                    (int) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                    (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
                ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return  update(patient,requestDetails, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Patient patient, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        // TODO check facility & tenant
        /**
         * Fixing references with ids and stored ids
         * TODO identifiers
         * if not recognised store unmatched reference ?
         */
        String dbPatientId;
        Identifier mrn = patient.getIdentifier().stream().filter((Identifier i) -> i.getSystem().equals(MRN_SYSTEM)).findFirst().orElse(null);
        EhrPatient oldPatient = null;
        if (mrn != null) {
            oldPatient = patientRepository.findByFacilityIdAndMrn(facility.getId(),mrn.getValue()).orElse(null);
        } else {
            dbPatientId = resourceIdentificationService.getPatientLocalId(patient,immunizationRegistry,facility);
            if (dbPatientId != null) {
                oldPatient = patientRepository.findById(dbPatientId).orElse(null);
            }
        }

        EhrPatient ehrPatient;
        ehrPatient = patientMapper.toEhrPatient(patient);
        ehrPatient.setFacility(facility);
        ehrPatient.setUpdatedDate(new Date());
        if (oldPatient == null) {
            ehrPatient.setCreatedDate(new Date());
        } else {
            // old patient is still stored in hibernate envers table
            ehrPatient.setId(oldPatient.getId());
            ehrPatient.setCreatedDate(oldPatient.getCreatedDate());
        }

        ehrPatient = patientRepository.save(ehrPatient);
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setId(new IdType().setValue(ehrPatient.getId()));
        methodOutcome.setResource(patientMapper.toFhirPatient(ehrPatient));
        return methodOutcome;
    }

    public MethodOutcome deleteConditional(IdType theId, String theConditionalUrl, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        return new MethodOutcome(); //TODO
    }

}
