package org.immregistries.ehr.fhir.Server.ServerR4;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.fhir.Server.ServerHelper;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.forR4.PatientMapperR4;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.immregistries.ehr.api.AuditRevisionListener.IMMUNIZATION_REGISTRY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;
import static org.immregistries.ehr.fhir.Server.FhirAuthInterceptor.FACILITY;

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

    @Read
    @Transactional
    public Patient Read(@IdParam IdType theId, RequestDetails requestDetails) {
        User user = ServerHelper.currentUser();
        Facility facility = (Facility) requestDetails.getAttribute(FACILITY);
        return patientRepository.findByFacilityIdAndId(facility.getId(), Integer.valueOf(theId.getIdPart()))
                .map(ehrPatient -> patientMapper.toFhir(ehrPatient))
                .orElseThrow(() -> new InvalidRequestException("HAPI-1996: Resource " + theId + " is not known"));
    }

    @Search
    @Transactional
    public List<Patient> search(RequestDetails requestDetails, @OptionalParam(name = Patient.SP_FAMILY) StringParam theFamilyName,
                                @OptionalParam(name = Patient.SP_IDENTIFIER)
                                TokenAndListParam theIdentifier) {
        Stream<EhrPatient> ehrPatientStream = null;
        User user = ServerHelper.currentUser();
        Facility facility = (Facility) requestDetails.getAttribute(FACILITY);

        int size = theIdentifier.size();
        if (size > 0) {
            ehrPatientStream = getEhrPatientStreamIdentifierParam(theIdentifier, facility, size);
        } else {
            ehrPatientStream = StreamSupport.stream(patientRepository.findByFacilityId(facility.getId()).spliterator(), false);
        }
        return ehrPatientStream
                .map(ehrPatient -> patientMapper.toFhir(ehrPatient)).collect(Collectors.toList());
    }

    @NotNull
    private Stream<EhrPatient> getEhrPatientStreamIdentifierParam(TokenAndListParam theIdentifier, Facility facility, int size) {
        Stream<EhrPatient> ehrPatientStream;
        Iterable<EhrPatient> ehrPatientIterable = null;
        TokenParam identifier = theIdentifier.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
        if (StringUtils.isNoneBlank(identifier.getSystem(), identifier.getValue())) {
            ehrPatientIterable = patientRepository.findByFacilityIdAndIdentifier(facility.getId(), identifier.getSystem(), identifier.getValue());
        } else if (StringUtils.isNotBlank(identifier.getSystem())) {
            ehrPatientIterable = patientRepository.findByFacilityIdAndIdentifierSystem(facility.getId(), identifier.getSystem());
        } else {
            ehrPatientIterable = patientRepository.findByFacilityIdAndIdentifierValue(facility.getId(), identifier.getValue());
        }

        ehrPatientStream = StreamSupport.stream(ehrPatientIterable.spliterator(), false);
        for (int i = 1; i < size; i++) {
            TokenParam tokenParam = theIdentifier.getValuesAsQueryTokens().get(i).getValuesAsQueryTokens().get(0);
            final Predicate<EhrIdentifier> predicate;
            if (StringUtils.isNoneBlank(identifier.getSystem(), identifier.getValue())) {
                predicate = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getSystem(), tokenParam.getSystem())
                        && StringUtils.equals(ehrIdentifier.getValue(), tokenParam.getValue());
            } else if (StringUtils.isNotBlank(identifier.getSystem())) {
                predicate = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getSystem(), tokenParam.getSystem());
            } else {
                predicate = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getValue(), tokenParam.getValue());
            }
            ehrPatientStream = ehrPatientStream
                    .filter(ehrPatient -> ehrPatient.getIdentifiers()
                            .stream().anyMatch(predicate));

        }
        return ehrPatientStream;
    }


    @Create
    public MethodOutcome create(@ResourceParam Patient fhirPatient, RequestDetails requestDetails) {
        return create(fhirPatient,
                facilityRepository.findById(EhrUtils.convert(requestDetails.getTenantId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id")));
    }

    public MethodOutcome create(Patient patient, Facility facility) {
        EhrPatient ehrPatient = patientMapper.toEhrPatient(patient);
        ehrPatient.setFacility(facility);
        ehrPatient.setCreatedDate(new Date());
        ehrPatient.setUpdatedDate(new Date());
        ehrPatient = patientRepository.save(ehrPatient);
        return new MethodOutcome().setId(new IdType().setValue(EhrUtils.convert(ehrPatient.getId()))).setCreated(true);
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
                (Integer) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return update(patient, requestDetails, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Patient patient, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(EhrUtils.convert(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        return update(patient, facility, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Patient patient, Facility facility, ImmunizationRegistry immunizationRegistry) {
        /**
         * Fixing references with ids and stored ids
         *
         * if not recognised store unmatched reference ?
         */
        Integer dbPatientId = resourceIdentificationService.getLocalPatientId(patient, immunizationRegistry, facility);
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
                    .setId(new IdType().setValue(EhrUtils.convert(ehrPatient.getId())))
                    .setResource(patientMapper.toFhir(ehrPatient, facility));
        } else {
            return create(patient, facility);
        }
    }

}
