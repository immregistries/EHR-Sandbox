package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Immunization;
import org.hl7.fhir.r5.model.ResourceType;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.fhir.EhrFhirProvider;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import static org.immregistries.ehr.api.AuditRevisionListener.IMMUNIZATION_REGISTRY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;

@Controller
@Conditional(OnR5Condition.class)
public class ImmunizationProviderR5 implements IResourceProvider, EhrFhirProvider<Immunization> {
    private static final Logger logger = LoggerFactory.getLogger(ImmunizationProviderR5.class);

    @Autowired
    private ImmunizationMapperR5 immunizationMapper;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private VaccineRepository vaccineRepository;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;
    @Override
    public Class<Immunization> getResourceType() {
        return Immunization.class;
    }
    @Override
    public ResourceType getResourceName() {
        return ResourceType.Immunization;
    }

    @Create
    public MethodOutcome create(@ResourceParam Immunization immunization, RequestDetails requestDetails) {
        return create(immunization,
                facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    /**
     * Currently unusable as is, as request
     * @param immunization
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome update(@ResourceParam Immunization immunization, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (int) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return update(immunization,requestDetails, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Immunization immunization, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        /**
         * Fixing references with ids and stored ids
         *
         *  TODO if not recognised store unmatched reference ?
         */
        String dbPatientId = resourceIdentificationService.getPatientLocalId(immunization.getPatient(), immunizationRegistry, facility);
        immunization.getPatient().setId(dbPatientId + "");
        logger.info("{}", dbPatientId);
        return update(immunization, immunizationRegistry, facility, dbPatientId);
    }

    public MethodOutcome update(@ResourceParam Immunization immunization, ImmunizationRegistry immunizationRegistry, Facility facility, String dbPatientId) {
        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(immunization);
        String vaccinationId = resourceIdentificationService.getImmunizationLocalId(immunization, immunizationRegistry, facility);
        if (vaccinationId == null) {
            return create(immunization,facility, dbPatientId);
        } else {
            VaccinationEvent old = vaccinationEventRepository.findById(vaccinationId).get();
            vaccinationEvent.setAdministeringFacility(facility);
            vaccinationEvent.setId(vaccinationId);
            vaccinationEvent.setPatient(
                    patientRepository.findByFacilityIdAndId(facility.getId(), dbPatientId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id")));

            vaccinationEvent.setVaccine(vaccineRepository.save(vaccinationEvent.getVaccine()));

            vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setId(new IdType().setValue(vaccinationEvent.getId()));
            methodOutcome.setResource(immunizationMapper.toFhirImmunization(vaccinationEvent,
                    resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility),
                    resourceIdentificationService.getFacilityPatientIdentifierSystem(facility)));
            return methodOutcome;
        }
    }


    public MethodOutcome create(Immunization immunization, Facility facility) {
        String patientId = new IdType(immunization.getPatient().getReference()).getIdPart();
        return create(immunization,facility,patientId);
    }
    public MethodOutcome create(Immunization immunization, Facility facility, String patientId) {
        MethodOutcome methodOutcome = new MethodOutcome();
        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(immunization);
        vaccinationEvent.setAdministeringFacility(facility);
        vaccinationEvent.setPatient(
                patientRepository.findByFacilityIdAndId(facility.getId(), patientId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id")));
        // TODO set received information status and make sure history of patient info if already exists
        vaccineRepository.save(vaccinationEvent.getVaccine());
        vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
        return methodOutcome.setId(new IdType().setValue(vaccinationEvent.getId()));
    }

    public MethodOutcome deleteConditional(IdType theId, String theConditionalUrl, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        return new MethodOutcome(); //TODO
    }
}
