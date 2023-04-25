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
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;

import java.net.URI;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations","/tenants/{tenantId}/facilities/{facilityId}/vaccinations"})
public class VaccinationController {
    @Autowired
    HL7printer hl7printer;
    @Autowired
    RandomGenerator randomGenerator;

    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private VaccineRepository vaccineRepository;
    @Autowired
    private ImmunizationRegistryController immRegistryController;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(VaccinationController.class);

    @GetMapping()
    public Iterable<VaccinationEvent> getVaccinationEvents(@PathVariable() int facilityId, @PathVariable() Optional<String> patientId) {
        if(patientId.isPresent()) {
            return vaccinationEventRepository.findByPatientId(patientId.get());
        } else {
            return vaccinationEventRepository.findByAdministeringFacilityId(facilityId);
        }
    }

    @GetMapping("/{vaccinationId}")
    public Optional<VaccinationEvent> vaccinationEvent(@PathVariable() String vaccinationId) {
        return  vaccinationEventRepository.findById(vaccinationId);
    }

    @GetMapping("/random")
    public VaccinationEvent random( @PathVariable() int facilityId,
                                    @PathVariable() Optional<String> patientId) {
        String patientId1 = patientId.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id"));
        EhrPatient patient = patientRepository.findByFacilityIdAndId(facilityId,patientId1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id"));
        return randomGenerator.randomVaccinationEvent(patient, patient.getFacility());
    }

    @PostMapping()
    public ResponseEntity<String> postVaccinationEvents(@PathVariable() Optional<String> patientId,
                                                        @RequestBody VaccinationEvent vaccination) {
        String patientId1 = patientId.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id"));

        EhrPatient patient = patientRepository.findById(patientId1)
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
        return ResponseEntity.created(location).body(newEntity.getId());
    }

    @PutMapping()
    public VaccinationEvent putVaccinationEvents(@PathVariable() int facilityId,
                                                 @PathVariable() Optional<String> patientId,
                                                 @RequestBody VaccinationEvent vaccination) {
        String patientId1 = patientId.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id"));

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No facility found"));
        EhrPatient patient = patientRepository.findById(patientId1)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        VaccinationEvent oldVaccination = vaccinationEventRepository.findByPatientIdAndId(patientId1, vaccination.getId())
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
    public ResponseEntity<String> vxu(@PathVariable() String vaccinationId) {
        GsonJsonParser gson = new GsonJsonParser();
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        Vaccine vaccine = vaccinationEvent.getVaccine();
        EhrPatient patient = vaccinationEvent.getPatient();
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

    @GetMapping("/{vaccinationId}/$history")
    public List<Revision<Integer, VaccinationEvent>> vaccinationHistory(
            @PathVariable() String vaccinationId) {
        Revisions<Integer, VaccinationEvent> revisions = vaccinationEventRepository.findRevisions(vaccinationId);
        return revisions.getContent();
    }
}
