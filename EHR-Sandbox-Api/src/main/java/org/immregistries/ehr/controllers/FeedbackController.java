package org.immregistries.ehr.controllers;

import org.immregistries.ehr.entities.*;
import org.immregistries.ehr.entities.repositories.*;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
