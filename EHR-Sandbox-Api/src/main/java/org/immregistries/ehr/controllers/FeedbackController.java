package org.immregistries.ehr.controllers;

import org.immregistries.ehr.entities.*;
import org.immregistries.ehr.repositories.*;
import org.immregistries.ehr.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
//@RequestMapping({""})
public class FeedbackController {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
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
    @Autowired
    private FacilityController facilityController;

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @GetMapping("/tenants/{tenantId}/facilities/{facilityId}/feedbacks")
    public Iterable<Feedback> getPatientFeedback(@PathVariable() int tenantId,
                                                 @PathVariable() int facilityId) {
        return facilityController.getFacility(tenantId,facilityId).get().getFeedbacks();
    }


    @GetMapping("/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/feedbacks")
    public Optional<Feedback> getPatientFeedback(@PathVariable() int patientId) {
        return feedbackRepository.findByPatientId(patientId);
    }

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/feedbacks")
    public Feedback postPatientFeedback(@PathVariable() int facilityId,
                                        @PathVariable() int patientId,
                                        @RequestBody Feedback feedback) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if(patient.isPresent()){
            Facility facility = patient.get().getFacility();
            feedback.setPatient(patient.get());
            feedback.setFacility(facility);
            return feedbackRepository.save(feedback);
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient not found");
    }

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations/{vaccinationId}/feedbacks")
    public Feedback postVaccinationFeedback(@PathVariable() int facilityId,
                                        @PathVariable() int patientId,
                                        @PathVariable() int vaccinationId,
                                        @RequestBody Feedback feedback) {
        Optional<VaccinationEvent>  vaccination = vaccinationEventRepository.findById(vaccinationId);
        if(vaccination.isPresent()){
            Patient patient = vaccination.get().getPatient();
            Facility facility = patient.getFacility();
            feedback.setVaccinationEvent(vaccination.get());
            feedback.setPatient(patient);
            feedback.setFacility(facility);
            return feedbackRepository.save(feedback);
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "vaccination not found");
    }


}
