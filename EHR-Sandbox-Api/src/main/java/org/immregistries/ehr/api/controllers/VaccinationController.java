package org.immregistries.ehr.api.controllers;


import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.logic.HL7printer;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
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

import java.net.URI;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients/{patientId}/vaccinations", "/tenants/{tenantId}/facilities/{facilityId}/vaccinations"})
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
    private ImmunizationRegistryController immRegistryController;
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

    @GetMapping("/{vaccinationId}")
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

    @GetMapping("/{vaccinationId}/vxu")
    public ResponseEntity<String> vxu(@PathVariable() String vaccinationId) {
        GsonJsonParser gson = new GsonJsonParser();
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        Vaccine vaccine = vaccinationEvent.getVaccine();
        EhrPatient patient = vaccinationEvent.getPatient();
        Facility facility = vaccinationEvent.getAdministeringFacility();
        String vxu = hl7printer.buildVxu(vaccine, patient, facility);
        return ResponseEntity.ok(vxu);
    }

    @PostMapping("/{vaccinationId}/vxu" + FhirClientController.IMM_REGISTRY_SUFFIX)
    public ResponseEntity<String> vxuSend(@PathVariable() Integer registryId, @RequestBody String message) {
        Connector connector;
        ImmunizationRegistry immunizationRegistry = immRegistryController.getImmunizationRegistry(registryId);
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

            return ResponseEntity.ok(connector.submitMessage(message, false));
        } catch (Exception e1) {
            e1.printStackTrace();
            return new ResponseEntity<>("SOAP Error: " + e1.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{vaccinationId}/$history")
    public List<Revision<Integer, VaccinationEvent>> vaccinationHistory(
            @PathVariable() String vaccinationId) {
        Revisions<Integer, VaccinationEvent> revisions = vaccinationEventRepository.findRevisions(vaccinationId);
        return revisions.getContent();
    }
}
