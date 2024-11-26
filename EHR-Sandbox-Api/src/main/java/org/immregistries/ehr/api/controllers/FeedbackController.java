package org.immregistries.ehr.api.controllers;

import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.smm.tester.manager.HL7Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
//@RequestMapping({""})
/**
 * Deprecated, formerly used to test
 */
public class FeedbackController {

    public static final String FEEDBACKS_PATH_HEADER = "/feedbacks";
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
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
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @GetMapping(FACILITY_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Iterable<Feedback> getPatientFeedback(@PathVariable(TENANT_ID) Integer tenantId,
                                                 @PathVariable(FACILITY_ID) Integer facilityId) {
        return facilityController.getFacility(tenantId, facilityId).get().getFeedbacks();
    }


    @GetMapping(PATIENT_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Optional<Feedback> getPatientFeedback(@PathVariable(PATIENT_ID) Integer patientId) {
        return feedbackRepository.findByPatientId(patientId);
    }

    @PostMapping(PATIENT_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Feedback postPatientFeedback(@PathVariable(FACILITY_ID) Integer facilityId,
                                        @PathVariable(PATIENT_ID) Integer patientId,
                                        @RequestBody Feedback feedback) {
        Optional<EhrPatient> patient = ehrPatientRepository.findById(patientId);
        if (patient.isPresent()) {
            Facility facility = patient.get().getFacility();
            feedback.setPatient(patient.get());
            feedback.setFacility(facility);
            return feedbackRepository.save(feedback);
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Patient not found");
    }

    @PostMapping(VACCINATION_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Feedback postVaccinationFeedback(@PathVariable(FACILITY_ID) Integer facilityId,
                                            @PathVariable(PATIENT_ID) Integer patientId,
                                            @PathVariable(VACCINATION_ID) Integer vaccinationId,
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

    @PostMapping({
            FACILITY_ID_PATH + FEEDBACKS_PATH_HEADER + "/$extract-ack",
            PATIENT_ID_PATH + FEEDBACKS_PATH_HEADER + "/$extract-ack",
            VACCINATION_ID_PATH + FEEDBACKS_PATH_HEADER + "/$extract-ack",
    })
    public Map<String, List<Feedback>> extractAckInfo(
            @RequestParam(REGISTRY_ID) Integer registryId,
            @PathVariable(FACILITY_ID) Integer facilityId,
            @PathVariable(PATIENT_ID) Optional<Integer> patientId,
            @PathVariable(VACCINATION_ID) Optional<Integer> vaccinationId,
            @RequestBody String ack) {
        HL7Reader hl7Reader = new HL7Reader(ack);
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        Map<String, List<Feedback>> map = new HashMap<>(4);
        List<Feedback> errors = new ArrayList<>(4);
        List<Feedback> warnings = new ArrayList<>(4);
        List<Feedback> notices = new ArrayList<>(4);
        List<Feedback> infos = new ArrayList<>(4);
        map.put("errors", errors);
        map.put("warnings", warnings);
        map.put("notices", notices);
        map.put("infos", infos);
        while (hl7Reader.advanceToSegment("ERR")) {
            String severity = hl7Reader.getValue(4);
            Feedback feedback = new Feedback();
            feedback.setIis(String.valueOf(immunizationRegistry.getId()));
            feedback.setFacility(facilityRepository.findById(facilityId).orElse(null));
            patientId.ifPresent(id -> feedback.setPatient(ehrPatientRepository.findById(id).orElse(null)));
            vaccinationId.ifPresent(id -> feedback.setVaccinationEvent(vaccinationEventRepository.findById(id).orElse(null)));
            feedback.setSeverity(severity);
            feedback.setContent(hl7Reader.getOriginalSegment());

            switch (severity) {
                case "E": {
                    errors.add(feedback);
                    break;
                }
                case "W": {
                    warnings.add(feedback);
                    break;
                }
                case "N": {
                    notices.add(feedback);
                    break;
                }
                case "I": {
                    infos.add(feedback);
                    break;
                }
            }
        }
        return map;
    }


}
