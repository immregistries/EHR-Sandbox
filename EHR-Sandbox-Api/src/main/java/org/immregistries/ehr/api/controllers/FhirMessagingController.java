package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgw.v2tofhir.converter.MessageParser;
import org.hl7.fhir.r4.model.Bundle;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.AcknowledgmentObjectRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FeedbackRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.smm.tester.connectors.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

/**
 * Prototype for FHIR Messaging paradigm implementation by integrating v2ToFHIR dependency
 */
@RestController
@RequestMapping()
public class FhirMessagingController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HL7printer hl7printer;
    @Autowired
    @Qualifier("fhirContextR4")
    private FhirContext fhirContextR4;

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


    @GetMapping(PATIENT_ID_PATH + "/qbp/fhir")
    public ResponseEntity<String> qbp(@PathVariable(PATIENT_ID) Integer patientId) {
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        Facility facility = ehrPatient.getFacility();
        String qbp = hl7printer.buildQbp(facility, ehrPatient);
        MessageParser messageParser = new MessageParser();
        try {
            Bundle bundle = messageParser.convert(qbp);
            return ResponseEntity.ok(fhirContextR4.newJsonParser().encodeResourceToString(bundle));
        } catch (HL7Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(PATIENT_ID_PATH + "/vxu/fhir")
    public ResponseEntity<String> vxuAll(@PathVariable(PATIENT_ID) Integer patientId) {
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        Facility facility = ehrPatient.getFacility();
        String vxu = hl7printer.buildVxu(facility, ehrPatient, ehrPatient.getVaccinationEvents());
        MessageParser messageParser = new MessageParser();
        try {
            Bundle bundle = messageParser.convert(vxu);
            return ResponseEntity.ok(fhirContextR4.newJsonParser().encodeResourceToString(bundle));
        } catch (HL7Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(VACCINATION_ID_PATH + "/vxu/fhir")
    public ResponseEntity<String> vxuSingle(@PathVariable(VACCINATION_ID) Integer vaccinationId) {
        GsonJsonParser gson = new GsonJsonParser();
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        Vaccine vaccine = vaccinationEvent.getVaccine();
        EhrPatient patient = vaccinationEvent.getPatient();
        Facility facility = vaccinationEvent.getAdministeringFacility();
        String vxu = hl7printer.buildVxu(facility, patient, Set.of(vaccinationEvent));

        MessageParser messageParser = new MessageParser();
        try {
            Bundle bundle = messageParser.convert(vxu);
            return ResponseEntity.ok(fhirContextR4.newJsonParser().encodeResourceToString(bundle));
        } catch (HL7Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(PATIENT_ID_PATH + "/qbp/fhir")
    public ResponseEntity<?> qbpSend(@RequestParam(REGISTRY_ID) Integer registryId,
                                     @PathVariable(FACILITY_ID) Integer facilityId,
                                     @PathVariable(PATIENT_ID) Integer patientId,
                                     @RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        try {
            connector = Hl7v2Controller.getConnector(immunizationRegistry);
            connector.setUrl(immunizationRegistry.getIisFhirMessagingUrl());
            String rsp = connector.submitMessage(message, false);
//            AcknowledgmentObject acknowledgmentObject = processAck(registryId, facilityId, patientId, Optional.empty(), message, rsp);
            return ResponseEntity.ok(rsp);
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
    @PostMapping({VACCINATION_ID_PATH + "/vxu/fhir", PATIENT_ID_PATH + "/vxu/fhir"})
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
            connector = Hl7v2Controller.getConnector(immunizationRegistry);
            connector.setUrl(immunizationRegistry.getIisFhirMessagingUrl());
            String ack = connector.submitMessage(message, false);
//            AcknowledgmentObject acknowledgmentObject = processAck(registryId, facilityId, patientId, vaccinationId, message, ack);
//            if (vaccinationEvent.isPresent() && (vaccinationEvent.get().getVaccine().getActionCode().equals("D") || message.indexOf("|D") > 0)) {
//
//            }
            return ResponseEntity.ok(ack);
        } catch (Exception e1) {
            logger.error("ERROR {}", "SOAP Client", e1);
            return ResponseEntity.internalServerError().body("SOAP Error: " + e1.getMessage());
        }
    }
}
