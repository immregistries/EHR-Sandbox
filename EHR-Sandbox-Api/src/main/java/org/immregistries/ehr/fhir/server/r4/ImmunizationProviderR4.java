package org.immregistries.ehr.fhir.server.r4;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ResourceType;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.fhir.server.ServerHelper;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.forR4.ImmunizationMapperR4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import static org.immregistries.ehr.api.AuditRevisionListener.IMMUNIZATION_REGISTRY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;

@Controller
public class ImmunizationProviderR4 implements IResourceProvider, EhrFhirProviderR4<Immunization> {
    private static final Logger logger = LoggerFactory.getLogger(ImmunizationProviderR4.class);

    @Autowired
    private ImmunizationMapperR4 immunizationMapper;
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

    @Read
    @Transactional
    public Immunization Read(@IdParam IdType theId, RequestDetails requestDetails) {
        User user = ServerHelper.currentUser();
        return vaccinationEventRepository.findByUserIdAndId(user, Integer.valueOf(theId.getIdPart()))
                .map(vaccinationEvent -> immunizationMapper.toFhir(vaccinationEvent, null))
                .orElseThrow(() -> new InvalidRequestException("HAPI-1996: Resource " + theId + " is not known"));
    }

    @Create
    public MethodOutcome create(@ResourceParam Immunization immunization, RequestDetails requestDetails) {
        return create(immunization,
                facilityRepository.findById(EhrUtils.convert(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    /**
     * Currently unusable as is, as request
     *
     * @param immunization
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome update(@ResourceParam Immunization immunization, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (Integer) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return update(immunization, requestDetails, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Immunization immunization, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(EhrUtils.convert(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        /**
         * Fixing references with ids and stored ids
         *
         *  TODO if not recognised store unmatched reference ?
         */
        Integer dbPatientId = resourceIdentificationService.getLocalPatientId(immunization.getPatient(), immunizationRegistry, facility);
        immunization.getPatient().setId(dbPatientId + "");
        return update(immunization, immunizationRegistry, facility, dbPatientId);
    }

    public MethodOutcome update(@ResourceParam Immunization immunization, ImmunizationRegistry immunizationRegistry, Facility facility, Integer dbPatientId) {
        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(facility, immunization);
        Integer vaccinationId = resourceIdentificationService.getImmunizationLocalId(immunization, immunizationRegistry, facility);
        if (vaccinationId == null) {
            return create(immunization, facility, dbPatientId);
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
            methodOutcome.setId(new IdType().setValue(EhrUtils.convert(vaccinationEvent.getId())));
            methodOutcome.setResource(immunizationMapper.toFhir(vaccinationEvent,
                    resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility)));
            return methodOutcome;
        }
    }


    public MethodOutcome create(Immunization immunization, Facility facility) {
        Integer patientId = EhrUtils.convert(new IdType(immunization.getPatient().getReference()).getIdPart());
        return create(immunization, facility, patientId);
    }

    public MethodOutcome create(Immunization immunization, Facility facility, Integer patientId) {
        MethodOutcome methodOutcome = new MethodOutcome();
        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(facility, immunization);
        vaccinationEvent.setAdministeringFacility(facility);
        vaccinationEvent.setPatient(
                patientRepository.findByFacilityIdAndId(facility.getId(), patientId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id")));
        // TODO set received information status and make sure history of patient info if already exists
        vaccineRepository.save(vaccinationEvent.getVaccine());
        vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
        return methodOutcome.setId(new IdType().setValue(EhrUtils.convert(vaccinationEvent.getId()))).setCreated(true);
    }
}
