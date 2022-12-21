package org.immregistries.ehr.api.controllers;


import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;

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
        Patient patient = patientRepository.findByFacilityIdAndId(facilityId,patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id"));
        return randomGenerator.randomVaccinationEvent(patient, patient.getFacility());
    }

    @PostMapping()
    public ResponseEntity<String> postVaccinationEvents(@PathVariable() int patientId,
                                                        @RequestBody VaccinationEvent vaccination) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        vaccination.setAdministeringClinician(clinicianRepository.save(vaccination.getAdministeringClinician()));
        vaccination.setOrderingClinician(clinicianRepository.save(vaccination.getOrderingClinician()));
        vaccination.setEnteringClinician(clinicianRepository.save(vaccination.getEnteringClinician()));
        vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
        vaccination.setPatient(patient);
        vaccination.setAdministeringFacility(patient.getFacility());
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
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No facility found"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        VaccinationEvent oldVaccination = vaccinationEventRepository.findByPatientIdAndId(patientId, vaccination.getId())
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        vaccination.setAdministeringClinician(clinicianRepository.save(vaccination.getAdministeringClinician()));
        vaccination.setOrderingClinician(clinicianRepository.save(vaccination.getOrderingClinician()));
        vaccination.setEnteringClinician(clinicianRepository.save(vaccination.getEnteringClinician()));
        vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
        vaccination.setPatient(patient);
        vaccination.setAdministeringFacility(facility);
        return vaccinationEventRepository.save(vaccination);
    }

    @GetMapping("/{vaccinationId}/vxu")
    public ResponseEntity<String> vxu(@PathVariable() int vaccinationId) {
        GsonJsonParser gson = new GsonJsonParser();
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        Vaccine vaccine = vaccinationEvent.getVaccine();
        Patient patient = vaccinationEvent.getPatient();
        Facility facility = vaccinationEvent.getAdministeringFacility();
        String vxu = hl7printer.buildVxu(vaccine, patient, facility);
        return ResponseEntity.ok(vxu);
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
