package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.immregistries.ehr.logic.mapping.PractitionnerMapperR5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("/tenants/{tenantId}/$create")
public class MacroEndpointsController {

    @Autowired
    PatientMapperR5 patientMapper;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private ImmunizationMapperR5 immunizationMapper;
    @Autowired
    private PractitionnerMapperR5 practitionnerMapper;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;

    @PostMapping()
    public ResponseEntity create(@RequestBody Bundle bundle, @RequestAttribute Tenant tenant) {
        Facility facility = null;

        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            if (entry.getResource() instanceof Organization) {
                if (facility != null) {
                    throw new InvalidRequestException("More than one organisation present");
                }
//                Facility facility = organizationMapper.
                facility = new Facility();
            }
        }
        if (facility == null) {
            throw new InvalidRequestException("No organisation present");
        }
        facility = facilityRepository.save(facility);

        Map<String,EhrPatient> ehrPatients = new HashMap<>(bundle.getEntry().size() - 1);
        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();
                EhrPatient ehrPatient = patientMapper.toEhrPatient(patient);
                ehrPatient.setFacility(facility);
                ehrPatient = ehrPatientRepository.save(ehrPatient);
                ehrPatients.put(patient.getIdElement().getIdPart(),ehrPatient);
            }
        }

        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            if (entry.getResource() instanceof Practitioner) {
                Practitioner practitioner = (Practitioner) entry.getResource();
                Clinician clinician = practitionnerMapper.fromFhir(practitioner);
                clinician.setTenant(tenant);
                clinician = clinicianRepository.save(clinician);
            }
        }

        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
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
                            .orElseThrow(() -> new InvalidRequestException("Invalid Mrn")));
                }
                // TODO maybe
                vaccinationEvent = vaccinationEventRepository.save(vaccinationEvent);
            }
        }



        return ResponseEntity.ok().build();
    }





}
