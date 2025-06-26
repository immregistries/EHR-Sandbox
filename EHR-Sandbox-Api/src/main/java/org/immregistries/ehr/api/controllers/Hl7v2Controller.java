package org.immregistries.ehr.api.controllers;

import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.AcknowledgmentObjectRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FeedbackRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController
@RequestMapping()
public class Hl7v2Controller {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HL7printer hl7printer;

    @Autowired
    private FeedbackController feedbackController;
    @Autowired
    private AcknowledgmentObjectRepository acknowledgmentObjectRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;


    @GetMapping(PATIENT_ID_PATH + "/qbp")
    public ResponseEntity<String> qbp(@PathVariable(PATIENT_ID) Integer patientId) {
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        Facility facility = ehrPatient.getFacility();
        String qbp = hl7printer.buildQbp(facility, ehrPatient);
        return ResponseEntity.ok(qbp);
    }

    @GetMapping(PATIENT_ID_PATH + "/vxu")
    public ResponseEntity<String> vxuAll(@PathVariable(PATIENT_ID) Integer patientId) {

        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        Facility facility = ehrPatient.getFacility();
        String vxu = hl7printer.buildVxu(facility, ehrPatient, ehrPatient.getVaccinationEvents());
        return ResponseEntity.ok(vxu);
    }

    @GetMapping(VACCINATION_ID_PATH + "/vxu")
    public ResponseEntity<String> vxuSingle(@PathVariable(VACCINATION_ID) Integer vaccinationId) {
        GsonJsonParser gson = new GsonJsonParser();
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        Vaccine vaccine = vaccinationEvent.getVaccine();
        EhrPatient patient = vaccinationEvent.getPatient();
        Facility facility = vaccinationEvent.getAdministeringFacility();
        String vxu = hl7printer.buildVxu(facility, patient, Set.of(vaccinationEvent));
        return ResponseEntity.ok(vxu);
    }

    /**
     * @param registryId ImmunizationRegistryId of receiver
     * @param facilityId Sending facility
     * @param patientId
     * @param message
     * @return
     */
    @PostMapping(PATIENT_ID_PATH + "/qbp")
    public ResponseEntity<?> qbpSend(@RequestParam(REGISTRY_ID) Integer registryId,
                                     @PathVariable(FACILITY_ID) Integer facilityId,
                                     @PathVariable(PATIENT_ID) Integer patientId,
                                     @RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        try {
            connector = getConnector(immunizationRegistry);
            String rsp = connector.submitMessage(message, false);
            AcknowledgmentObject acknowledgmentObject = processAck(registryId, facilityId, patientId, Optional.empty(), message, rsp);
            return ResponseEntity.ok(acknowledgmentObject);
        } catch (Exception e1) {
            e1.printStackTrace();
            return new ResponseEntity<>("SOAP Error: " + e1.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sends message through SOAP
     *
     * @param registryId
     * @param facilityId
     * @param patientId
     * @param vaccinationId
     * @param message
     * @return Response entity with Acknowledgement object or Error message
     */
    @PostMapping({VACCINATION_ID_PATH + "/vxu", PATIENT_ID_PATH + "/vxu"})
    public ResponseEntity<?> vxuSend(@RequestParam(REGISTRY_ID) Integer registryId,
                                     @PathVariable(FACILITY_ID) Integer facilityId,
                                     @PathVariable(PATIENT_ID) Integer patientId,
                                     @PathVariable(VACCINATION_ID) Optional<Integer> vaccinationId,
                                     @RequestBody String message) {
//        Optional<VaccinationEvent> vaccinationEvent = Optional.empty();
//        if (vaccinationId.isPresent()) {
//            vaccinationEvent = vaccinationEventRepository.findById(vaccinationId.get());
//        }
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        Connector connector;
        try {
            connector = getConnector(immunizationRegistry);

            String ack = connector.submitMessage(message, false);
            AcknowledgmentObject acknowledgmentObject = processAck(registryId, facilityId, patientId, vaccinationId, message, ack);
//            if (vaccinationEvent.isPresent() && (vaccinationEvent.get().getVaccine().getActionCode().equals("D") || message.indexOf("|D") > 0)) {
//
//            }
            return ResponseEntity.ok(acknowledgmentObject);
        } catch (Exception e1) {
            logger.error("ERROR {}", "SOAP Client", e1);
            return ResponseEntity.internalServerError().body("SOAP Error: " + e1.getMessage());
        }
    }

    /**
     * TODO figure out how to add a proxy to the Http Process, maybe customize smm-tester
     *
     * @param immunizationRegistry
     * @param url
     * @return
     * @throws Exception
     */
    public static Connector getConnector(ImmunizationRegistry immunizationRegistry, String url) throws Exception {
        Connector connector;
//        javax.net.ssl.SSLContext sslContext = new SSLContext();
        connector = new SoapConnector("Test", url);
        if (StringUtils.isNotBlank(immunizationRegistry.getIisUsername())) {
            connector.setUserid(immunizationRegistry.getIisUsername());
            connector.setPassword(immunizationRegistry.getIisPassword());
            connector.setFacilityid(immunizationRegistry.getIisFacilityId());
        }
//            else  {
//                connector.setUserid("nist");
//                connector.setKeyStore(new KeyStore());
//            }
        return connector;
    }

    public static Connector getConnector(ImmunizationRegistry immunizationRegistry) throws Exception {
        return getConnector(immunizationRegistry, immunizationRegistry.getIisHl7Url());
    }

    public AcknowledgmentObject processAck(Integer registryId, Integer facilityId, Integer patientId, Optional<Integer> vaccinationId, String message, String ack) {
        AcknowledgmentObject acknowledgmentObject;
        if (vaccinationId.isPresent()) {
            acknowledgmentObject = feedbackController.extractAckInfo(
                    Optional.of(registryId),
                    Optional.of(facilityId),
                    Optional.of(patientId),
                    vaccinationId,
                    ack);
        } else {
            acknowledgmentObject = feedbackController.extractAckInfo(
                    Optional.of(registryId),
                    Optional.of(facilityId),
                    Optional.of(patientId),
                    vaccinationEventRepository.findByPatientId(patientId),
                    ack);
        }
        acknowledgmentObject.setRawSource(message);
        acknowledgmentObject = acknowledgmentObjectRepository.save(acknowledgmentObject);
        feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getInfos());
        feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getNotices());
        feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getWarnings());
        feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getErrors());
        return acknowledgmentObject;
    }
}
