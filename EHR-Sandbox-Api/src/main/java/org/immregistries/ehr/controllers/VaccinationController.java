package org.immregistries.ehr.controllers;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.Immunization;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.*;
import org.immregistries.ehr.entities.repositories.*;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.ImmunizationHandler;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.ResourceClient;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
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
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private VaccineRepository vaccineRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
//    @Autowired(required = false)
//    private ResourceClient resourceClient;

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
        return RandomGenerator.randomVaccinationEvent(patient.get(), patient.get().getFacility());
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
    public VaccinationEvent putVaccinationEvents(@PathVariable() int patientId,
                                                 @RequestBody VaccinationEvent vaccination) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        Optional<VaccinationEvent> oldVaccination =  vaccinationEventRepository.findByPatientIdAndId(patientId, vaccination.getId());
        if (!patient.isPresent() || !oldVaccination.isPresent()) {
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
        String vxu = new HL7printer().buildVxu(vaccine,patient,facility);
        return ResponseEntity.ok( vxu );
    }

    @PostMapping("/{vaccinationId}/vxu")
    public ResponseEntity<String>  vxuSend(@RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        try {
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            connector.setUserid(immunizationRegistry.getIisUsername());
            connector.setPassword(immunizationRegistry.getIisPassword());
            connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            return ResponseEntity.ok(connector.submitMessage(message, false));
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return ResponseEntity.ok("SOAP Error");
        }
    }

    @GetMapping("/{vaccinationId}/resource")
    public ResponseEntity<String> resource(@PathVariable() int vaccinationId) {
        Optional<VaccinationEvent> vaccinationEvent = vaccinationEventRepository.findById(vaccinationId);
        FhirContext ctx = EhrApiApplication.fhirContext;
        IParser parser = ctx.newXmlParser().setPrettyPrint(true);
        if (!vaccinationEvent.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No vaccination found");
        }
        org.hl7.fhir.r4.model.Immunization immunization = ImmunizationHandler.dbVaccinationToFhirVaccination(vaccinationEvent.get()) ;
        String resource = parser.encodeResourceToString(immunization);
        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{vaccinationId}/fhir")
    public ResponseEntity<String> fhirSend(@RequestBody String message) {
        IParser parser = EhrApiApplication.fhirContext.newXmlParser().setPrettyPrint(true);
        Immunization immunization = parser.parseResource(Immunization.class,message);
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
//        return ResponseEntity.ok(ResourceClient.write(immunization,ir));
        MethodOutcome outcome;
        try {
            outcome = ResourceClient.write(immunization,ir);
            return ResponseEntity.ok(outcome.getId().getIdPart());

        } catch (Exception e) {
            throw e;
        }

    }

    @GetMapping("/{vaccinationId}/fhir")
    public ResponseEntity<String>  fhirGet(@PathVariable() int vaccinationId) {
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        return ResponseEntity.ok(ResourceClient.read("immunization", String.valueOf(vaccinationId), ir));
    }






}
