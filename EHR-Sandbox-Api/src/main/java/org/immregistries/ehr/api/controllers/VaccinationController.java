package org.immregistries.ehr.api.controllers;


import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;


@RestController
@RequestMapping({VACCINATION_PATH, FACILITY_ID_PATH + VACCINATION_PATH_HEADER})
public class VaccinationController {
    @Autowired
    HL7printer hl7printer;
    @Autowired
    RandomGenerator randomGenerator;

    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    ClinicianController clinicianController;
    @Autowired
    private VaccineRepository vaccineRepository;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(VaccinationController.class);
    @Autowired
    private TenantRepository tenantRepository;

    @GetMapping()
    public Iterable<VaccinationEvent> getVaccinationEvents(@PathVariable() String facilityId, @PathVariable() Optional<String> patientId) {
        if (patientId.isPresent()) {
            return vaccinationEventRepository.findByPatientId(patientId.get());
        } else {
            return vaccinationEventRepository.findByAdministeringFacilityId(facilityId);
        }
    }

    @GetMapping(ControllerHelper.VACCINATION_ID_SUFFIX)
    public Optional<VaccinationEvent> vaccinationEvent(@PathVariable() String vaccinationId) {
        return vaccinationEventRepository.findById(vaccinationId);
    }

    @GetMapping("/$random")
    public VaccinationEvent random(@PathVariable() String tenantId,
                                   @PathVariable() String facilityId,
                                   @PathVariable() Optional<String> patientId) {
        patientId.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id"));
        return randomGenerator.randomVaccinationEvent(patientRepository.findById(patientId.get()).get(), tenantRepository.findById(tenantId).get(), facilityRepository.findById(facilityId).get());
    }

    @PostMapping()
    public ResponseEntity<String> postVaccinationEvents(@PathVariable() String tenantId,
                                                        @PathVariable() Optional<String> patientId,
                                                        @RequestBody VaccinationEvent vaccination) {
        patientId.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid patient id"));
        return postVaccinationEvents(tenantRepository.findById(tenantId).get(), patientRepository.findById(patientId.get()).get(), vaccination);
    }

    public ResponseEntity<String> postVaccinationEvents(Tenant tenant,
                                                        EhrPatient patient,
                                                        VaccinationEvent vaccination) {
        VaccinationEvent newEntity = createVaccinationEvent(tenant, patient, vaccination);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        return ResponseEntity.created(location).body(newEntity.getId());
    }

    private VaccinationEvent createVaccinationEvent(Tenant tenant,
                                                    EhrPatient patient,
                                                    VaccinationEvent vaccination) {
        if (vaccination.getAdministeringClinician() != null && vaccination.getAdministeringClinician().getId() == null) {
            vaccination.setAdministeringClinician(clinicianController.postClinicians(tenant, vaccination.getAdministeringClinician()));
        }
        if (vaccination.getEnteringClinician() != null && vaccination.getEnteringClinician().getId() == null) {
            vaccination.setEnteringClinician(clinicianController.postClinicians(tenant, vaccination.getEnteringClinician()));
        }
        if (vaccination.getOrderingClinician() != null && vaccination.getOrderingClinician().getId() == null) {
            vaccination.setOrderingClinician(clinicianController.postClinicians(tenant, vaccination.getOrderingClinician()));
        }
        vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
        vaccination.setPatient(patient);
        vaccination.setAdministeringFacility(patient.getFacility());
        return vaccinationEventRepository.save(vaccination);
    }

    @PutMapping()
    public VaccinationEvent putVaccinationEvents(@PathVariable() String tenantId,
                                                 @PathVariable() String facilityId,
                                                 @PathVariable() Optional<String> patientId,
                                                 @RequestBody VaccinationEvent vaccination) {
        patientId.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient Id"));
        return putVaccinationEvents(tenantRepository.findById(tenantId).get(), facilityRepository.findById(facilityId).get(), patientRepository.findById(patientId.get()).get(), vaccination);
    }

    public VaccinationEvent putVaccinationEvents(Tenant tenant,
                                                 Facility facility,
                                                 EhrPatient patient,
                                                 VaccinationEvent vaccination) {
        VaccinationEvent oldVaccination;
        Optional<VaccinationEvent> old = vaccinationEventRepository.findByPatientIdAndId(patient.getId(), vaccination.getId());
        if (old.isEmpty()) {
            return createVaccinationEvent(tenant, patient, vaccination);
        } else {
            oldVaccination = old.get();
            vaccination.getVaccine().setCreatedDate(oldVaccination.getVaccine().getCreatedDate());
            if (vaccination.getAdministeringClinician() != null && vaccination.getAdministeringClinician().getId() == null) {
                vaccination.setAdministeringClinician(clinicianController.postClinicians(tenant, vaccination.getAdministeringClinician()));
            }
            if (vaccination.getEnteringClinician() != null && vaccination.getEnteringClinician().getId() == null) {
                vaccination.setEnteringClinician(clinicianController.postClinicians(tenant, vaccination.getEnteringClinician()));
            }
            if (vaccination.getOrderingClinician() != null && vaccination.getOrderingClinician().getId() == null) {
                vaccination.setOrderingClinician(clinicianController.postClinicians(tenant, vaccination.getOrderingClinician()));
            }
            vaccination.setVaccine(vaccineRepository.save(vaccination.getVaccine()));
            vaccination.setPatient(patient);
            vaccination.setAdministeringFacility(facility);
            return vaccinationEventRepository.save(vaccination);
        }
    }

    @GetMapping(ControllerHelper.VACCINATION_ID_SUFFIX + "/vxu")
    public ResponseEntity<String> vxu(@PathVariable() String vaccinationId) {
        GsonJsonParser gson = new GsonJsonParser();
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        Vaccine vaccine = vaccinationEvent.getVaccine();
        EhrPatient patient = vaccinationEvent.getPatient();
        Facility facility = vaccinationEvent.getAdministeringFacility();
        String vxu = hl7printer.buildVxu(vaccinationEvent, patient, facility);
        return ResponseEntity.ok(vxu);
    }

//    @PostMapping(ControllerHelper.VACCINATION_ID_SUFFIX + "/vxu" + REGISTRY_COMPLETE_SUFFIX)
//    public ResponseEntity<String> vxuSend(@PathVariable() String registryId, @PathVariable() String vaccinationId, @RequestBody String message) {
//        Connector connector;
//        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId).get();
//        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
//        try {
//            connector = new SoapConnector("Test", immunizationRegistry.getIisHl7Url());
//            if (StringUtils.isNotBlank(immunizationRegistry.getIisUsername())) {
//                connector.setUserid(immunizationRegistry.getIisUsername());
//                connector.setPassword(immunizationRegistry.getIisPassword());
//                connector.setFacilityid(immunizationRegistry.getIisFacilityId());
//            }
////            else  {
////                connector.setUserid("nist");
////                connector.setKeyStore(new KeyStore());
////            }
//
//            String result = connector.submitMessage(message, false);
//            logger.info("CONNECTOR {} {} {}", connector.getAckType(), connector.getType(), connector.getLabelDisplay());
//            if (vaccinationEvent.getVaccine().getActionCode().equals("D") || message.indexOf("|D") > 0) {
//
//            }
//            return ResponseEntity.ok(result);
//        } catch (Exception e1) {
//            e1.printStackTrace();
//            return ResponseEntity.internalServerError().body("SOAP Error: " + e1.getMessage());
//        }
//    }

    @GetMapping(ControllerHelper.VACCINATION_ID_SUFFIX + "/$history")
    public List<Revision<Integer, VaccinationEvent>> vaccinationHistory(
            @PathVariable() String vaccinationId) {
        Revisions<Integer, VaccinationEvent> revisions = vaccinationEventRepository.findRevisions(vaccinationId);
        return revisions.getContent();
    }

    private static final String LOT_NUMBER_VALIDATION_URL = "https://sabbia.westus2.cloudapp.azure.com/lot";

    @RequestMapping("/$lotNumberValidation")
//    @PostMapping("/$lotNumberValidation")
    public ResponseEntity<byte[]> vaccinationLotNumberValidation(
            @RequestParam(name = "lotNumber") String lotNumber, @RequestParam(name = "cvx") String cvx, @RequestParam(name = "mvx") String mvx) {
        HttpURLConnection con = null;
        URL url;
        try {
            url = UriComponentsBuilder.fromUriString(LOT_NUMBER_VALIDATION_URL)
                    .queryParam("lotNumber", URLEncoder.encode(lotNumber, StandardCharsets.UTF_8))
                    .queryParam("cvx", URLEncoder.encode(cvx, StandardCharsets.UTF_8))
                    .queryParam("mvx", URLEncoder.encode(mvx, StandardCharsets.UTF_8))
                    .build(true)
                    .toUri().toURL();
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(5000);
            int status = con.getResponseCode();
            if (status == 200) {
                return ResponseEntity.ok().build();
            } else if (status == 400) {
                return ResponseEntity.accepted().body(con.getInputStream().readAllBytes());
            } else if (status == 404) {
                return ResponseEntity.accepted().body("NOT FOUND".getBytes(StandardCharsets.UTF_8));
            } else {
                return ResponseEntity.internalServerError().body(con.getResponseMessage().getBytes());
            }
        } catch (MalformedURLException | ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.accepted().body(("Verification Service Unreachable").getBytes(StandardCharsets.UTF_8));
//            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
