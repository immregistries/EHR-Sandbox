package org.immregistries.ehr.api.controllers;

import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.AcknowledgmentObjectRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FeedbackRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
import org.immregistries.smm.tester.manager.query.QueryConverter;
import org.immregistries.smm.tester.manager.query.QueryType;
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
        QueryConverter queryConverter;
        if (ProcessingFlavor.Z44.isActive()) {
            queryConverter = QueryConverter.getQueryConverter(QueryType.QBP_Z44);
        } else {
            queryConverter = QueryConverter.getQueryConverter(QueryType.QBP_Z34);
        }
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
//        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findByPatientId(patientId);
//        Vaccine vaccine = vaccinationEvent.getVaccine()
        Facility facility = ehrPatient.getFacility();
        String vxu = hl7printer.buildVxu(facility, ehrPatient, null);
        String qbp = queryConverter.convert(vxu);
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

    @PostMapping(PATIENT_ID_PATH + "/qbp")
    public ResponseEntity<?> qbpSend(@RequestParam(REGISTRY_ID) Integer registryId,
                                     @PathVariable(FACILITY_ID) Integer facilityId,
                                     @PathVariable(PATIENT_ID) Integer patientId,
                                     @RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        try {
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            connector.setUserid(immunizationRegistry.getIisUsername());
            connector.setPassword(immunizationRegistry.getIisPassword());
            connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            String rsp = connector.submitMessage(message, false);
            AcknowledgmentObject acknowledgmentObject = feedbackController.extractAckInfo(
                    Optional.of(registryId),
                    Optional.of(facilityId),
                    Optional.of(patientId),
                    Optional.empty(),
                    rsp);
            acknowledgmentObject.setRawSource(message);
            acknowledgmentObject = acknowledgmentObjectRepository.save(acknowledgmentObject);
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getInfos());
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getNotices());
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getWarnings());
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getErrors());
            acknowledgmentObject = acknowledgmentObjectRepository.save(acknowledgmentObject);
            return ResponseEntity.ok(acknowledgmentObject);
        } catch (Exception e1) {
            e1.printStackTrace();
            return new ResponseEntity<>("SOAP Error: " + e1.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * TODO SUPPORT Err - Vaccination attribution when multiple Vaccinations are sent
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
        Connector connector;
        Optional<VaccinationEvent> vaccinationEvent = Optional.empty();
        if (vaccinationId.isPresent()) {
            vaccinationEvent = vaccinationEventRepository.findById(vaccinationId.get());
        }
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        try {
            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
            if (StringUtils.isNotBlank(immunizationRegistry.getIisUsername())) {
                connector.setUserid(immunizationRegistry.getIisUsername());
                connector.setPassword(immunizationRegistry.getIisPassword());
                connector.setFacilityid(immunizationRegistry.getIisFacilityId());
            }
//            else  {
//                connector.setUserid("nist");
//                connector.setKeyStore(new KeyStore());
//            }

            String ack = connector.submitMessage(message, false);
            AcknowledgmentObject acknowledgmentObject = feedbackController.extractAckInfo(
                    Optional.of(registryId),
                    Optional.of(facilityId),
                    Optional.of(patientId),
                    vaccinationId,
                    ack);
            acknowledgmentObject.setRawSource(message);
            acknowledgmentObject = acknowledgmentObjectRepository.save(acknowledgmentObject);
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getInfos());
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getNotices());
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getWarnings());
            feedbackRepository.saveAll(acknowledgmentObject.getSortedResult().getErrors());
            acknowledgmentObject = acknowledgmentObjectRepository.save(acknowledgmentObject);
//            logger.info("CONNECTOR {} {} {}", connector.getAckType(), connector.getType(), connector.getLabelDisplay());
            if (vaccinationEvent.isPresent() && (vaccinationEvent.get().getVaccine().getActionCode().equals("D") || message.indexOf("|D") > 0)) {

            }
            return ResponseEntity.ok(acknowledgmentObject);
        } catch (Exception e1) {
            e1.printStackTrace();
            return ResponseEntity.internalServerError().body("SOAP Error: " + e1.getMessage());
        }
    }
}
