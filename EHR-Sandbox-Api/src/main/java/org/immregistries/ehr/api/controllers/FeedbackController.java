package org.immregistries.ehr.api.controllers;

import org.apache.commons.lang3.math.NumberUtils;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.Hl7Location;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.smm.tester.manager.HL7Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
//@RequestMapping({""})
/**
 * Deprecated, formerly used to test
 */
public class FeedbackController {

    public static final String FEEDBACKS_PATH_HEADER = "/feedbacks";
    public static final String $_EXTRACT_ACK = "/$extract-ack";
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
            FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK,
            FACILITY_ID_PATH + FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK,
            PATIENT_ID_PATH + FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK,
            VACCINATION_ID_PATH + FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK,
    })
    public AcknowledgmentObject extractAckInfo(
            @RequestParam(REGISTRY_ID) Optional<Integer> registryId,
            @PathVariable(FACILITY_ID) Optional<Integer> facilityId,
            @PathVariable(PATIENT_ID) Optional<Integer> patientId,
            @PathVariable(VACCINATION_ID) Optional<Integer> vaccinationId,
            @RequestBody String ack) {
        HL7Reader hl7Reader = new HL7Reader(ack);
        AcknowledgmentObject acknowledgmentObject = new AcknowledgmentObject();
        acknowledgmentObject.setRawAck(ack);
        if (hl7Reader.advanceToSegment("MSH")) {
            acknowledgmentObject.setMessageId(hl7Reader.getValue(9));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmssZ");
            String timestamp = hl7Reader.getValue(6);
            try {
                acknowledgmentObject.setTimestamp(new Timestamp(simpleDateFormat.parse(timestamp).getTime()));
            } catch (ParseException e) {
            }

        }

        if (hl7Reader.advanceToSegment("MSA")) {
            acknowledgmentObject.setMsa_2(hl7Reader.getValue(1));
            acknowledgmentObject.setMessageId(hl7Reader.getValue(2)); // TODO choose which control Id

        }
        while (hl7Reader.advanceToSegment("ERR")) {
            String severity = hl7Reader.getValue(4);
            Feedback feedback = new Feedback();
            registryId.ifPresent(id -> feedback.setIis(String.valueOf(registryId)));
            facilityId.ifPresent(id -> feedback.setFacility(facilityRepository.findById(id).orElse(null)));
            patientId.ifPresent(id -> feedback.setPatient(ehrPatientRepository.findById(id).orElse(null)));
            vaccinationId.ifPresent(id -> feedback.setVaccinationEvent(vaccinationEventRepository.findById(id).orElse(null)));
            feedback.setSeverity(severity);
            feedback.setContent(hl7Reader.getOriginalSegment());
            feedback.setCode(hl7Reader.getValue(5));
            feedback.setTimestamp(new Timestamp(new Date().getTime()));
            switch (severity) {
                case "E": {
                    acknowledgmentObject.getErrors().add(feedback);
                    break;
                }
                case "W": {
                    acknowledgmentObject.getWarnings().add(feedback);
                    break;
                }
                case "N": {
                    acknowledgmentObject.getNotices().add(feedback);
                    break;
                }
                case "I": {
                    acknowledgmentObject.getInfos().add(feedback);
                    break;
                }
            }
            int locationsNumbers = hl7Reader.getComponentCount(2);
            for (int i = 0; i < locationsNumbers; i++) {
                Hl7Location hl7Location = new Hl7Location();
                hl7Location.setSegmentId(hl7Reader.getValueRepeat(2, 0, i));
                hl7Location.setSegmentSequence(NumberUtils.toInt(hl7Reader.getValueRepeat(2, 1, i), 0));
                hl7Location.setFieldPosition(NumberUtils.toInt(hl7Reader.getValueRepeat(2, 2, i), 0));
                hl7Location.setFieldRepetition(NumberUtils.toInt(hl7Reader.getValueRepeat(2, 3, i), 0));
                hl7Location.setComponentNumber(NumberUtils.toInt(hl7Reader.getValueRepeat(2, 4, i), 0));
                hl7Location.setSubComponentNumber(NumberUtils.toInt(hl7Reader.getValueRepeat(2, 5, i), 0));
                feedback.getHl7Locations().add(hl7Location);
            }
//            feedbackRepository.save(feedback);
        }
        return acknowledgmentObject;
    }


}
