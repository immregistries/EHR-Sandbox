package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.fhir.EhrFhirProvider;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.OrganizationMapperR5;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.immregistries.ehr.logic.mapping.PractitionerMapperR5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController()
public class MacroEndpointsController {

    @Autowired
    private FhirContext fhirContext;
    @Autowired
    private AuthController authController;
    @Autowired
    private TenantController tenantController;
    @Autowired
    private FacilityController facilityController;
    @Autowired
    private EhrPatientController ehrPatientController;
    @Autowired
    private ClinicianController clinicianController;
    @Autowired
    private PatientMapperR5 patientMapper;
    @Autowired
    private ImmunizationMapperR5 immunizationMapper;
    @Autowired
    private PractitionerMapperR5 practitionnerMapper;
    @Autowired
    private OrganizationMapperR5 organizationMapper;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private VaccineRepository vaccineRepository;
    @Autowired
    private EhrFhirProvider<ImmunizationRecommendation> immunizationRecommendationProvider;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private FacilityRepository facilityRepository;

    @PostMapping(value = "/$create", consumes = {"application/json"})
    @Transactional()
    public ResponseEntity createAll(HttpServletRequest httpRequest, @RequestBody String bundleString) {
        Bundle globalBundle = fhirContext.newJsonParser().parseResource(Bundle.class, bundleString);
        User user = new User();
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64 = authHeader.substring("Basic ".length());
            String base64decoded = new String(Base64.decodeBase64(base64));
            String[] parts = base64decoded.split(":");
            user.setUsername(parts[0]);
            user.setPassword(parts[1]);
            authController.registerUser(user);
        } else {
            throw new AuthenticationException("no basic auth header found");
        }

        for (Bundle.BundleEntryComponent entry : globalBundle.getEntry()) {
            if (entry.getResource() instanceof Bundle) {
                createTenant((Bundle) entry.getResource());
            } else {
                throw new InvalidRequestException("Bundle for user creation should only contain bundles");
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "tenants/$create", consumes = {"application/json"})
    @Transactional()
    public ResponseEntity createTenant(@RequestBody String bundleString) {
        Bundle tenantBundle = fhirContext.newJsonParser().parseResource(Bundle.class, bundleString);
        return createTenant(tenantBundle);
    }

    public ResponseEntity createTenant(Bundle tenantBundle) {
        Tenant tenant = null;
        for (Bundle.BundleEntryComponent entry : tenantBundle.getEntry()) {
            if (entry.getResource() instanceof Organization) {
                if (tenant != null) {
                    throw new InvalidRequestException("More than one organisation present in tenant creation bundle");
                }
                tenant = organizationMapper.tenantFromFhir((Organization) entry.getResource());
            }
        }
        if (tenant == null) {
            throw new InvalidRequestException("No organization found for tenant definition");
        }
        tenant = tenantController.postTenant(tenant).getBody();

        for (Bundle.BundleEntryComponent entry : tenantBundle.getEntry()) {
            if (entry.getResource() instanceof Bundle) {
                createFacility(tenant, (Bundle) entry.getResource());
            } else if (!(entry.getResource() instanceof Organization)) {
                throw new InvalidRequestException("Bundle for creating tenant should only contain bundles");
            }
        }
        return ResponseEntity.ok(tenant.getId());
    }

    @PostMapping(value = "/tenants/{tenantId}/facilities/$create", consumes = {"application/json"})
    @Transactional()
    public ResponseEntity createFacility(@PathVariable() String tenantId, @RequestBody String bundleString) {
        Bundle facilityBundle = fhirContext.newJsonParser().parseResource(Bundle.class, bundleString);
        return createFacility(tenantRepository.findById(tenantId).get(), facilityBundle);
    }

    public ResponseEntity createFacility(Tenant tenant, Bundle facilityBundle) {
        Facility facility = null;
        for (Bundle.BundleEntryComponent entry : facilityBundle.getEntry()) {
            if (entry.getResource() instanceof Organization) {
                if (facility != null) {
                    throw new InvalidRequestException("More than one organisation present");
                }
                facility = organizationMapper.facilityFromFhir((Organization) entry.getResource());
                facility.setTenant(tenant);
            }
        }
        if (facility == null) {
            throw new InvalidRequestException("No organisation found for facility definition");
        }
        facility = facilityController.postFacility(tenant, facility, Optional.empty()).getBody();

        fillFacility(tenant, facility, facilityBundle);

        return ResponseEntity.ok(facility.getId());
    }

    @PostMapping(value = "/tenants/{tenantId}/facilities/{facilityId}/$create", consumes = {"application/json"})
    @Transactional()
    public ResponseEntity fillFacility(@PathVariable() String tenantId, @PathVariable() String facilityId, @RequestBody String bundleString) {
        Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, bundleString);
        return fillFacility(tenantRepository.findById(tenantId).get(), facilityRepository.findById(facilityId).get(), bundle);
    }

    public ResponseEntity fillFacility(Tenant tenant, Facility facility, Bundle bundle) {
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Practitioner) {
                Practitioner practitioner = (Practitioner) entry.getResource();
                Clinician clinician = practitionnerMapper.toClinician(practitioner);
                clinician.setTenant(tenant);
                clinician = clinicianController.postClinicians(tenant, clinician);
            }
        }

        /**
         * Map<remoteId,newLocalId>
         */
        Map<String, String> ehrPatients = new HashMap<>(bundle.getEntry().size() - 1);
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();
                EhrPatient ehrPatient = patientMapper.toEhrPatient(patient);
                String localId = ehrPatientController.postPatient(tenant, facility, ehrPatient, Optional.empty()).getBody();
                ehrPatients.put(patient.getIdElement().getIdPart(), localId);
            }
        }

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Immunization) {
                Immunization immunization = (Immunization) entry.getResource();
                VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(immunization);
                vaccinationEvent.setAdministeringFacility(facility);
                if (StringUtils.isNotBlank(immunization.getPatient().getReference())) {
//                    vaccinationEvent.setPatient(ehrPatientRepository.findByFacilityIdAndId(facility.getId(), immunization.getPatient().getReference())
//                            .orElseThrow(() -> new InvalidRequestException("Invalid id")));
                    vaccinationEvent.setPatient(ehrPatientRepository.findById(ehrPatients.get(immunization.getPatient().getReference()))
                            .orElseThrow(() -> new InvalidRequestException("Missing patient " + immunization.getPatient().getReference())));
                } else if (immunization.getPatient().getIdentifier() != null) {
                    vaccinationEvent.setPatient(ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), immunization.getPatient().getIdentifier().getValue())
                            .orElseThrow(() -> new InvalidRequestException("Mrn not recognised")));
                }
                vaccinationEvent.setVaccine(vaccineRepository.save(vaccinationEvent.getVaccine()));
                vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
            }
        }

        /**
         * TODO determine if it makes sense to initialise with it
         */
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof ImmunizationRecommendation) {
//                ImmunizationRecommendation immunizationRecommendation = (ImmunizationRecommendation) entry.getResource();
//                if (StringUtils.isNotBlank(immunizationRecommendation.getPatient().getReference())) {
////                    vaccinationEvent.setPatient(ehrPatientRepository.findByFacilityIdAndId(facility.getId(), immunization.getPatient().getReference())
////                            .orElseThrow(() -> new InvalidRequestException("Invalid id")));
//
////                    vaccinationEvent.setPatient(ehrPatients.get(immunization.getPatient().getReference()));
//                } else if (immunizationRecommendation.getPatient().getIdentifier() != null) {
//                    immunizationRecommendationProvider.update(re)
//                }
//                vaccinationEvent.setVaccine(vaccineRepository.save(vaccinationEvent.getVaccine()));
//                vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
            }
        }
        return ResponseEntity.ok().build();
    }


}
