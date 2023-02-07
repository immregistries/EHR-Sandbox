package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Immunization;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
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

@Controller
@Conditional(OnR5Condition.class)
public class ImmunizationProviderR5 implements IResourceProvider {
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

    @Create
    public MethodOutcome createImmunization(@ResourceParam Immunization immunization, RequestDetails requestDetails) {
        return createImmunization(immunization,
                facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }
    @Update
    public MethodOutcome updateImmunization(@ResourceParam Immunization immunization, RequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = null;
//                immunizationRegistryRepository
//                .findByUserIdAndIisFhirUrl(facility.getTenant().getUser().getId(), requestDetails.getHeader("origin")) // TODO find best way to determine origin, oAuth ? or subscription
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return  updateImmunization(immunization,requestDetails, immunizationRegistry);
    }

    public MethodOutcome updateImmunization(@ResourceParam Immunization immunization, RequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        /**
         * Fixing references with ids and stored ids
         *
         *  TODO if not recognised store unmatched reference ?
         */
        String dbPatientId = resourceIdentificationService.getPatientLocalId(immunization.getPatient(), immunizationRegistry, facility);
        immunization.getPatient().setId(dbPatientId + "");
        // TODO Historical registering


        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(immunization);
        String vaccinationId = resourceIdentificationService.getImmunizationLocalId(immunization, immunizationRegistry, facility);
        VaccinationEvent old = vaccinationEventRepository.findById(vaccinationId).get();
//        vaccinationEvent.getVaccine().setId(old.getVaccine().getId()); // TODO change fetchtype ?
        vaccinationEvent.setAdministeringFacility(facility);
        vaccinationEvent.setId(vaccinationId);
        vaccinationEvent.setPatient(
                patientRepository.findByFacilityIdAndId(facility.getId(), dbPatientId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id")));

        vaccinationEvent.setVaccine(vaccineRepository.save(vaccinationEvent.getVaccine()));

        vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setId(new IdType().setValue(vaccinationEvent.getId()));
        methodOutcome.setResource(immunizationMapper.toFhirImmunization(vaccinationEvent,resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility), resourceIdentificationService.getFacilityPatientIdentifierSystem(facility)));
        return methodOutcome;
    }




    public MethodOutcome createImmunization(Immunization immunization, Facility facility) {
        MethodOutcome methodOutcome = new MethodOutcome();
        VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(immunization);
        vaccinationEvent.setAdministeringFacility(facility);
        String patientId = new IdType(immunization.getPatient().getReference()).getIdPart();
        vaccinationEvent.setPatient(
                patientRepository.findByFacilityIdAndId(facility.getId(), patientId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id")));
        // TODO set received information status and make sure history of patient info if already exists
        vaccineRepository.save(vaccinationEvent.getVaccine());
        vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
        return methodOutcome.setId(new IdType().setValue(vaccinationEvent.getId().toString()));
    }
}
