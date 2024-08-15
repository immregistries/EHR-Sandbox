package org.immregistries.ehr.fhir.Server.ServerR4;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.PatientMapperR4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static org.immregistries.ehr.api.AuditRevisionListener.IMMUNIZATION_REGISTRY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;

@Controller
public class PatientProviderR4 implements IResourceProvider, EhrFhirProviderR4<Patient> {
    private static final Logger logger = LoggerFactory.getLogger(PatientProviderR4.class);

    @Autowired
    private PatientMapperR4 patientMapper;
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
                facilityRepository.findById(requestDetails.getTenantId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    public MethodOutcome create(Patient patient, Facility facility) {
        EhrPatient ehrPatient = patientMapper.toEhrPatient(patient);
        ehrPatient.setFacility(facility);
        ehrPatient.setCreatedDate(new Date());
        ehrPatient.setUpdatedDate(new Date());
        ehrPatient = patientRepository.save(ehrPatient);
        return new MethodOutcome().setId(new IdType().setValue(ehrPatient.getId())).setCreated(true);
    }

    /**
     * Currently unusable as is, as request
     *
     * @param patient
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome update(@ResourceParam Patient patient, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (String) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return update(patient, requestDetails, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Patient patient, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(requestDetails.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        return update(patient, facility, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Patient patient, Facility facility, ImmunizationRegistry immunizationRegistry) {
        /**
         * Fixing references with ids and stored ids
         *
         * if not recognised store unmatched reference ?
         */
        String dbPatientId = resourceIdentificationService.getLocalPatientId(patient, immunizationRegistry, facility);
        EhrPatient oldPatient = patientRepository.findByFacilityIdAndId(facility.getId(), dbPatientId).orElse(null);

        if (oldPatient != null) {
            EhrPatient ehrPatient;
            ehrPatient = patientMapper.toEhrPatient(patient);
            ehrPatient.setFacility(facility);
            ehrPatient.setUpdatedDate(new Date());
            // old patient is still stored in hibernate envers table
            ehrPatient.setId(oldPatient.getId());
            ehrPatient.setCreatedDate(oldPatient.getCreatedDate());

            ehrPatient = patientRepository.save(ehrPatient);
            return new MethodOutcome()
                    .setId(new IdType().setValue(ehrPatient.getId()))
                    .setResource(patientMapper.toFhir(ehrPatient, facility));
        } else {
            return create(patient, facility);
        }
    }

}
