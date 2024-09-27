package org.immregistries.ehr.api.controllers;

import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.Feedback;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
//@RequestMapping({""})
/**
 * Deprecated, formerly used to test
 */
public class FeedbackController {

    public static final String FEEDBACKS_PATH_HEADER = "/feedbacks";
    @Autowired
    private EhrPatientRepository patientRepository;
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

    @GetMapping(FACILITY_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Iterable<Feedback> getPatientFeedback(@PathVariable() String tenantId,
                                                 @PathVariable() String facilityId) {
        return facilityController.getFacility(tenantId, facilityId).get().getFeedbacks();
    }


    @GetMapping(PATIENT_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Optional<Feedback> getPatientFeedback(@PathVariable() String patientId) {
        return feedbackRepository.findByPatientId(patientId);
    }

    @PostMapping(PATIENT_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Feedback postPatientFeedback(@PathVariable() String facilityId,
                                        @PathVariable() String patientId,
                                        @RequestBody Feedback feedback) {
        Optional<EhrPatient> patient = patientRepository.findById(patientId);
        if (patient.isPresent()) {
            Facility facility = patient.get().getFacility();
            feedback.setPatient(patient.get());
            feedback.setFacility(facility);
            return feedbackRepository.save(feedback);
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient not found");
    }

    @PostMapping(VACCINATION_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Feedback postVaccinationFeedback(@PathVariable() String facilityId,
                                            @PathVariable() String patientId,
                                            @PathVariable() String vaccinationId,
                                            @RequestBody Feedback feedback) {
        Optional<VaccinationEvent> vaccination = vaccinationEventRepository.findById(vaccinationId);
        if (vaccination.isPresent()) {
            EhrPatient patient = vaccination.get().getPatient();
            Facility facility = patient.getFacility();
            feedback.setVaccinationEvent(vaccination.get());
            feedback.setPatient(patient);
            feedback.setFacility(facility);
            return feedbackRepository.save(feedback);
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "vaccination not found");
    }


}
