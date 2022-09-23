package org.immregistries.ehr.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r5.model.Immunization;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.ImmunizationRegistry;
import org.immregistries.ehr.entities.Patient;
import org.immregistries.ehr.entities.VaccinationEvent;
import org.immregistries.ehr.logic.ImmunizationHandler;
import org.immregistries.ehr.logic.PatientHandler;
import org.immregistries.ehr.logic.ResourceClient;
import org.immregistries.ehr.repositories.*;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class FhirClientController {
    private static final String PATIENT_PREFIX = "/tenants/{tenantId}/facilities/{facilityId}/patients";
    private static final String IMMUNIZATION_PREFIX = "/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations";
    @Autowired
    MainController mainController;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    private static final Logger logger = LoggerFactory.getLogger(FhirClientController.class);


    @GetMapping("/iis/{resourceType}/{id}")
    public ResponseEntity<String> getFhirResourceFromIIS(@PathVariable() String resourceType,
                                                 @PathVariable() String id) {
        return ResponseEntity.ok(ResourceClient.read(resourceType, id, mainController.settings()));
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/resource")
    public ResponseEntity<String> getPatientResource(
            HttpServletRequest request,
            @PathVariable() int patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        FhirContext ctx = EhrApiApplication.fhirContext;
        IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        if (!patient.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No patient found");
        }
        org.hl7.fhir.r5.model.Patient fhirPatient = PatientHandler.dbPatientToFhirPatient(patient.get(),
                request.getRequestURI().split("/patients")[0]);
        String resource = parser.encodeResourceToString(fhirPatient);
        return ResponseEntity.ok(resource);
    }

    @PostMapping(PATIENT_PREFIX + "/{patientId}/fhir")
    public  ResponseEntity<String>  postPatient(@RequestBody String message) {
        IParser parser = parser(message);
        org.hl7.fhir.r5.model.Patient patient = parser.parseResource(org.hl7.fhir.r5.model.Patient.class,message);
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        MethodOutcome outcome = ResourceClient.create(patient, ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @PutMapping(PATIENT_PREFIX + "/{patientId}/fhir")
    public  ResponseEntity<String>  updatePatient(@RequestBody String message) {
        IParser parser = parser(message);
        org.hl7.fhir.r5.model.Patient patient = parser.parseResource(org.hl7.fhir.r5.model.Patient.class,message);
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        MethodOutcome outcome = ResourceClient.updateOrCreate(patient, "Patient",patient.getIdentifierFirstRep(), ir);
        if (outcome.getOperationOutcome() != null) {
            logger.info(parser.encodeResourceToString(outcome.getOperationOutcome()));
        }
        logger.info(String.valueOf(outcome.getResponseHeaders()));
        return ResponseEntity.ok(outcome.getId().getIdPart());
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/fhir")
    public ResponseEntity<String>  getPatient(@PathVariable() int patientId) {
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        return ResponseEntity.ok(ResourceClient.read("patient", String.valueOf(patientId), ir));
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/resource")
    public ResponseEntity<String> immunizationResource(
            HttpServletRequest request,
            @PathVariable() int vaccinationId) {
        Optional<VaccinationEvent> vaccinationEvent = vaccinationEventRepository.findById(vaccinationId);
        FhirContext ctx = EhrApiApplication.fhirContext;
        IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        if (!vaccinationEvent.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "No vaccination found");
        }
        org.hl7.fhir.r5.model.Immunization immunization =
                ImmunizationHandler.dbVaccinationToFhirVaccination(vaccinationEvent.get(),
                        request.getRequestURI().split("/patients")[0]) ;
        String resource = parser.encodeResourceToString(immunization);
        return ResponseEntity.ok(resource);
    }

    @PostMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir")
    public ResponseEntity<String> postImmunization(@RequestBody String message) {
        IParser parser;
        if (message.startsWith("<")) {
            parser = EhrApiApplication.fhirContext.newXmlParser().setPrettyPrint(true);
        } else {
            parser = EhrApiApplication.fhirContext.newJsonParser().setPrettyPrint(true);
        }
        Immunization immunization = parser.parseResource(Immunization.class,message);
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        MethodOutcome outcome;
        try {
            outcome = ResourceClient.updateOrCreate(immunization, "Immunization",immunization.getIdentifierFirstRep(), ir);
            return ResponseEntity.ok(outcome.getId().getIdPart());

        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/fhir")
    public ResponseEntity<String>  getImmunization(@PathVariable() int vaccinationId) {
        ImmunizationRegistry ir = immunizationRegistryRepository.findByUserId(userDetailsService.currentUserId());
        return ResponseEntity.ok(ResourceClient.read("immunization", String.valueOf(vaccinationId), ir));
    }

    private IParser parser(String message) {
        IParser parser;
        if (message.startsWith("<")) {
            parser = EhrApiApplication.fhirContext.newXmlParser().setPrettyPrint(true);
        } else {
            parser = EhrApiApplication.fhirContext.newJsonParser().setPrettyPrint(true);
        }
        return parser;
    }



}
