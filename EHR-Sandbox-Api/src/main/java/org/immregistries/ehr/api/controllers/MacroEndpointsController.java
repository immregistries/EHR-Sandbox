package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.OrganizationMapperR5;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.immregistries.ehr.logic.mapping.PractitionnerMapperR5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.*;

@RestController()
public class MacroEndpointsController {

    @Autowired
    private FhirContext fhirContext;
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
    private PractitionnerMapperR5 practitionnerMapper;
    @Autowired
    private OrganizationMapperR5 organizationMapper;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private VaccineRepository vaccineRepository;

    @PostMapping(value="tenants/$create",consumes={"application/json"})
    @Transactional()
    public ResponseEntity createTenant(@RequestBody String bundleString) {

        Bundle tenantBundle = fhirContext.newJsonParser().parseResource(Bundle.class,bundleString);
        Tenant tenant = null;

        for (Bundle.BundleEntryComponent entry: tenantBundle.getEntry()) {
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

        for (Bundle.BundleEntryComponent entry: tenantBundle.getEntry()) {
            if (entry.getResource() instanceof Bundle) {
                createFacility(tenant, (Bundle) entry.getResource());
            } else if (!(entry.getResource() instanceof  Organization)) {
                throw new InvalidRequestException("Bundle for creating tenant should only contain bundles");
            }
        }
        return ResponseEntity.ok(tenant.getId());
    }


    @PostMapping(value="/tenants/{tenantId}/facilities/$create",consumes={"application/json"})
    @Transactional()
    public ResponseEntity createFacility(@RequestAttribute Tenant tenant, @RequestBody String bundleString) {
        Bundle facilityBundle = fhirContext.newJsonParser().parseResource(Bundle.class,bundleString);
        return  createFacility(tenant, facilityBundle);
    }


    public ResponseEntity createFacility(Tenant tenant,  Bundle facilityBundle) {
        Facility facility = null;
        for (Bundle.BundleEntryComponent entry: facilityBundle.getEntry()) {
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
        facility = facilityController.postFacility(tenant, facility).getBody();

        /**
         * Map<remoteId,newLocalId>
         */
        Map<String,String> ehrPatients = new HashMap<>(facilityBundle.getEntry().size() - 1);
        for (Bundle.BundleEntryComponent entry: facilityBundle.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();
                EhrPatient ehrPatient = patientMapper.toEhrPatient(patient);
                String localId = ehrPatientController.postPatient(facility,ehrPatient, Optional.empty(),Optional.empty()).getBody();
                ehrPatients.put(patient.getIdElement().getIdPart(), localId);
            }
        }

        for (Bundle.BundleEntryComponent entry: facilityBundle.getEntry()) {
            if (entry.getResource() instanceof Practitioner) {
                Practitioner practitioner = (Practitioner) entry.getResource();
                Clinician clinician = practitionnerMapper.fromFhir(practitioner);
                clinician.setTenant(tenant);
                clinician = clinicianController.postClinicians(tenant,clinician);
            }
        }

        for (Bundle.BundleEntryComponent entry: facilityBundle.getEntry()) {
            if (entry.getResource() instanceof Immunization) {
                Immunization immunization = (Immunization) entry.getResource();
                VaccinationEvent vaccinationEvent = immunizationMapper.toVaccinationEvent(immunization);
                vaccinationEvent.setAdministeringFacility(facility);
                if (StringUtils.isNotBlank(immunization.getPatient().getReference())) {
//                    vaccinationEvent.setPatient(ehrPatientRepository.findByFacilityIdAndId(facility.getId(), immunization.getPatient().getReference())
//                            .orElseThrow(() -> new InvalidRequestException("Invalid id")));
                    vaccinationEvent.setPatient(ehrPatients.get(immunization.getPatient().getReference()));
                } else if (immunization.getPatient().getIdentifier() != null) {
                    vaccinationEvent.setPatient(ehrPatientRepository.findByFacilityIdAndMrn(facility.getId(), immunization.getPatient().getIdentifier().getValue())
                            .orElseThrow(() -> new InvalidRequestException("Mrn not recognised")));
                }
                vaccinationEvent.setVaccine(vaccineRepository.save(vaccinationEvent.getVaccine()));
                vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
            }
        }
        return ResponseEntity.ok(facility.getId());

    }





}
