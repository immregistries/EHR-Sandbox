package org.immregistries.ehr.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.immregistries.ehr.EhrApiApplication;
import org.immregistries.ehr.entities.*;
import org.immregistries.ehr.logic.PatientHandler;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.logic.ResourceClient;
import org.immregistries.ehr.repositories.*;
import org.immregistries.ehr.security.AuthTokenFilter;
import org.immregistries.ehr.security.JwtUtils;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Date;
import java.util.Optional;

@RestController
//@RequestMapping({""})
public class FeedbackController {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);


    @GetMapping("/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/feedbacks")
    public Optional<Feedback> getPatientFeedback(@PathVariable() int patientId) {
        return feedbackRepository.findByPatientId(patientId);
    }

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/feedbacks")
    public Feedback postPatientFeedback(@PathVariable() int facilityId,
                                        @PathVariable() int patientId,
                                        @RequestBody Feedback feedback) {
        Patient patient = patientRepository.findById(patientId).get();
        Facility facility = patient.getFacility();
        feedback.setPatient(patient);
        feedback.setFacility(facility);
        return feedbackRepository.save(feedback);
    }


}
