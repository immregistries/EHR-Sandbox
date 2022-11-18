package org.immregistries.ehr.controllers;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r5.model.Immunization;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.*;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.ImmunizationHandler;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.ResourceClient;
import org.immregistries.ehr.repositories.*;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations"})
public class VaccinationController {
    @Autowired
    HL7printer hl7printer;
    @Autowired
    RandomGenerator randomGenerator;

    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private VaccineRepository vaccineRepository;
    @Autowired
    private ImmRegistryController immRegistryController;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(VaccinationController.class);

    @GetMapping()
    public Iterable<VaccinationEvent> vaccinationEvents(@PathVariable() int patientId) {
        return vaccinationEventRepository.findByPatientId(patientId);
    }

    @GetMapping("/{vaccinationId}")
    public Optional<VaccinationEvent> vaccinationEvent(@PathVariable() int vaccinationId) {
        return  vaccinationEventRepository.findById(vaccinationId);
    }

    @GetMapping("/random")
    public VaccinationEvent random( @PathVariable() int facilityId,
                                    @PathVariable() int patientId) {
        Optional<Patient> patient = patientRepository.findByFacilityIdAndId(facilityId,patientId);
        if (!patient.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid patient id");
        }
        return randomGenerator.randomVaccinationEvent(patient.get(), patient.get().getFacility());
    }

    @PostMapping()
    public ResponseEntity<String> postVaccinationEvents(@PathVariable() int patientId,
                                                        @RequestBody VaccinationEvent vaccination) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (!patient.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No patient found");
        }
        vaccination.setAdministeringClinician(clinicianRepository.save(vaccination.getAdministeringClinician()));
        vaccination.setOrderingClinician(clinicianRepository.save(vaccination.getOrderingClinician()));
        vaccination.setEnteringClinician(clinicianRepository.save(vaccination.getEnteringClinician()));
        vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
        vaccination.setPatient(patient.get());
        vaccination.setAdministeringFacility(patient.get().getFacility());
        VaccinationEvent newEntity = vaccinationEventRepository.save(vaccination);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping()
    public VaccinationEvent putVaccinationEvents(@PathVariable() int facilityId,
                                                 @PathVariable() int patientId,
                                                 @RequestBody VaccinationEvent vaccination) {
        Optional<Facility> facility = facilityRepository.findById(facilityId);
        Optional<Patient> patient = patientRepository.findById(patientId);
        Optional<VaccinationEvent> oldVaccination = vaccinationEventRepository.findByPatientIdAndId(patientId, vaccination.getId());
        if (!patient.isPresent() || !oldVaccination.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No patient found");
        }
        vaccination.setAdministeringClinician(clinicianRepository.save(vaccination.getAdministeringClinician()));
        vaccination.setOrderingClinician(clinicianRepository.save(vaccination.getOrderingClinician()));
        vaccination.setEnteringClinician(clinicianRepository.save(vaccination.getEnteringClinician()));
        vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
        vaccination.setPatient(patient.get());
        vaccination.setAdministeringFacility(facility.get());
        VaccinationEvent newEntity = vaccinationEventRepository.save(vaccination);
        return newEntity;
    }

    @GetMapping("/{vaccinationId}/vxu")
    public ResponseEntity<String> vxu(@PathVariable() int vaccinationId) {
        GsonJsonParser gson = new GsonJsonParser();
        Optional<VaccinationEvent> vaccinationEvent = vaccinationEventRepository.findById(vaccinationId);
        if (!vaccinationEvent.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No vaccination found");
        }
        Vaccine vaccine = vaccinationEvent.get().getVaccine();
        Patient patient = vaccinationEvent.get().getPatient();
        Facility facility = vaccinationEvent.get().getAdministeringFacility();
        String vxu = hl7printer.buildVxu(vaccine,patient,facility);
        return ResponseEntity.ok( vxu );
    }

    @PostMapping("/{vaccinationId}/vxu" + FhirClientController.IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String>  vxuSend(@PathVariable() Integer immRegistryId, @RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immRegistryController.settings(immRegistryId);
        try {
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            connector.setUserid(immunizationRegistry.getIisUsername());
            connector.setPassword(immunizationRegistry.getIisPassword());
            connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            return ResponseEntity.ok(connector.submitMessage(message, false));
        } catch (Exception e1) {
            e1.printStackTrace();
            return new ResponseEntity<>("SOAP Error: " + e1.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
