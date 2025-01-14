package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.DataFormatException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.Hl7Location;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.fhir.Server.ServerR4.OperationOutcomeProviderR4;
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
import java.util.*;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
public class FeedbackController {

    private Map<Integer, List<AcknowledgmentObject>> cacheAck = new HashMap<>(5);

    public static final String FEEDBACKS_PATH_HEADER = "/feedbacks";
    public static final String ACKS_PATH_HEADER = "/acks";
    public static final String $_EXTRACT_ACK = "/$extract-ack";
    public static final String $_EXTRACT_ACK_FHIR = "/$extract-operationOutcome";
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
    @Autowired
    private FhirComponentsDispatcher fhirComponentsDispatcher;

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @GetMapping(FACILITY_ID_PATH + FEEDBACKS_PATH_HEADER)
    public Iterable<Feedback> getPatientFeedback(@PathVariable(TENANT_ID) Integer tenantId,
                                                 @PathVariable(FACILITY_ID) Integer facilityId) {
        return facilityController.getFacility(tenantId, facilityId).get().getFeedbacks();
    }

    @GetMapping(FACILITY_ID_PATH + ACKS_PATH_HEADER)
    public List<AcknowledgmentObject> getFacilityAcks(@PathVariable(FACILITY_ID) Integer facilityId) {
        return cacheAck.get(facilityId);
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
        acknowledgmentObject.setRaw(ack);
        Optional<ImmunizationRegistry> immunizationRegistry = Optional.empty();
        if (registryId.isPresent()) {
            immunizationRegistry = Optional.of(immunizationRegistryService.getImmunizationRegistry(registryId.get()));
        }
        Optional<Facility> facility = Optional.empty();
        if (facilityId.isPresent()) {
            facility = facilityRepository.findById(facilityId.get());
        }

        Optional<EhrPatient> patient = Optional.empty();
        if (patientId.isPresent()) {
            patient = ehrPatientRepository.findById(patientId.get());
        }
        Optional<VaccinationEvent> vaccinationEvent = Optional.empty();
        if (vaccinationId.isPresent()) {
            vaccinationEvent = vaccinationEventRepository.findById(vaccinationId.get());
        }
        if (hl7Reader.advanceToSegment("MSH")) {
            acknowledgmentObject.setMessageId(hl7Reader.getValue(9));
            acknowledgmentObject.setSenderSoftware(hl7Reader.getValue(3));
            acknowledgmentObject.setSender(hl7Reader.getValue(4));
            acknowledgmentObject.setDestinationSoftware(hl7Reader.getValue(5));
            acknowledgmentObject.setDestination(hl7Reader.getValue(6));
            facility.ifPresent(acknowledgmentObject::setFacility);
            patient.ifPresent(acknowledgmentObject::setPatient);
            vaccinationEvent.ifPresent(acknowledgmentObject::setVaccination);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmssZ");
            String timestamp = hl7Reader.getValue(7);
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
            feedback.setRaw(hl7Reader.getOriginalSegment());
            feedback.setSeverity(severity);
            immunizationRegistry.ifPresent(obj -> feedback.setIis(String.valueOf(obj.getId())));
            facility.ifPresent(feedback::setFacility);
            patient.ifPresent(feedback::setPatient);
            vaccinationEvent.ifPresent(feedback::setVaccinationEvent);

            if (StringUtils.isNotBlank(hl7Reader.getValue(8))) {
                if (StringUtils.isNotBlank(hl7Reader.getValue(8, 2))) {
                    feedback.setContent(hl7Reader.getValue(8, 2));
                    feedback.setCode(hl7Reader.getValue(8));
                } else {
                    feedback.setContent(hl7Reader.getValue(8, 1));
                }
            } else if (StringUtils.isNotBlank(hl7Reader.getValue(5, 2))) {
                feedback.setContent(hl7Reader.getValue(5, 2));
                feedback.setCode(hl7Reader.getValue(5));
            }
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
            feedbackRepository.save(feedback);
        }
        if (facilityId.isPresent()) {
            cacheAck.putIfAbsent(facilityId.get(), new ArrayList<>(10));
            cacheAck.get(facilityId.get()).add(acknowledgmentObject);
        }
        return acknowledgmentObject;
    }

    @PostMapping({
            FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK_FHIR,
            FACILITY_ID_PATH + FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK_FHIR,
            PATIENT_ID_PATH + FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK_FHIR,
            VACCINATION_ID_PATH + FEEDBACKS_PATH_HEADER + $_EXTRACT_ACK_FHIR,
    })
    public AcknowledgmentObject extractAckInfoFHIR(
            @RequestParam(REGISTRY_ID) Optional<Integer> registryId,
            @PathVariable(FACILITY_ID) Optional<Integer> facilityId,
            @PathVariable(PATIENT_ID) Optional<Integer> patientId,
            @PathVariable(VACCINATION_ID) Optional<Integer> vaccinationId,
            @RequestBody String resource) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId.get());
        Facility facility = null;
        if (facilityId.isPresent()) {
            facility = facilityRepository.findById(facilityId.get()).orElse(null);
        }
        EhrPatient patient = null;
        if (patientId.isPresent()) {
            patient = ehrPatientRepository.findById(patientId.get()).orElse(null);
        }

        VaccinationEvent vaccinationEvent = null;
        if (vaccinationId.isPresent()) {
            vaccinationEvent = vaccinationEventRepository.findById(vaccinationId.get()).orElse(null);
        }
        IBaseOperationOutcome iBaseOperationOutcome = null;
        AcknowledgmentObject acknowledgmentObject = null;
        try {
            iBaseOperationOutcome = (IBaseOperationOutcome) fhirComponentsDispatcher.parser(resource).parseResource(resource);
            acknowledgmentObject = OperationOutcomeProviderR4.acknowledgmentObject((OperationOutcome) iBaseOperationOutcome, immunizationRegistry, facility, patient, vaccinationEvent);
        } catch (DataFormatException dataFormatException) {
        }
//            feedbackRepository.save(feedback);
        if (facilityId.isPresent()) {
            cacheAck.putIfAbsent(facilityId.get(), new ArrayList<>(10));
            cacheAck.get(facilityId.get()).add(acknowledgmentObject);
        }
        return acknowledgmentObject;
    }


}
