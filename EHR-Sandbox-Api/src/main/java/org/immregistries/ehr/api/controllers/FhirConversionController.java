package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.fhir.Client.SmartHealthCardService;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.immregistries.ehr.api.controllers.FhirClientController.*;

@RestController
public class FhirConversionController {
    Logger logger = LoggerFactory.getLogger(FhirConversionController.class);

    @Autowired
    FhirComponentsDispatcher fhirComponentsDispatcher;

    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private ClinicianRepository clinicianRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;


    @Autowired
    SmartHealthCardService smartHealthCardService;


    @GetMapping(PATIENT_PREFIX + "/{patientId}/resource")
    public ResponseEntity<String> getPatientAsResource(
            @PathVariable() String patientId, @PathVariable() String facilityId) {
        EhrPatient ehrPatient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility found"));
        IBaseResource patient = fhirComponentsDispatcher.patientMapper().toFhir(ehrPatient, facility);
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true).setSuppressNarratives(true);
        String resource = parser.encodeResourceToString(patient);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/resource")
    public ResponseEntity<String> immunizationResource(@PathVariable() String facilityId, @PathVariable() String patientId, @PathVariable() String vaccinationId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        IBaseResource immunization = fhirComponentsDispatcher.immunizationMapper().toFhir(vaccinationEventRepository.findById(vaccinationId).get(),
                resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facilityRepository.findById(facilityId).get()));
        String resource = parser.encodeResourceToString(immunization);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(GROUPS_PREFIX + "/{groupId}/resource")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> groupResource(
            @PathVariable() String groupId) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        IBaseResource group = fhirComponentsDispatcher.groupMapper().toFhir(ehrGroup);
        String resource = parser.encodeResourceToString(group);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(FACILITY_PREFIX + "/{facilityId}/resource")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> facilityResource(
            @PathVariable() String facilityId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        IBaseResource organization = fhirComponentsDispatcher.organizationMapper().toFhir(facilityRepository.findById(facilityId).get());
        String resource = parser.encodeResourceToString(organization);
        return ResponseEntity.ok(resource);
    }


    @GetMapping(FACILITY_PREFIX + "/{facilityId}/bundle")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> facilityAllResourcesTransaction(@PathVariable() String facilityId) {
        Facility facility = facilityRepository.findById(facilityId).get();
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        IBaseBundle baseBundle = fhirComponentsDispatcher.fhirTransactionWriter().transactionBundle(facility);
        String resource = parser.encodeResourceToString(baseBundle);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/bundle")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> qpdEquivalentTransaction(@PathVariable() String facilityId, @PathVariable() String patientId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        IBaseBundle iBaseBundle = fhirComponentsDispatcher.fhirTransactionWriter().
                qpdBundle(facilityRepository.findById(facilityId).get(), patientRepository.findById(patientId).get());
        String resource = parser.encodeResourceToString(iBaseBundle);
        return ResponseEntity.ok(resource);
    }


    @GetMapping(CLINICIAN_PREFIX + "/{clinicianId}/resource")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> clinicianResource(@PathVariable() String tenantId, @PathVariable() String clinicianId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        Clinician clinician = clinicianRepository.findById(clinicianId).orElseThrow();
        IBaseResource practitioner = fhirComponentsDispatcher.practitionerMapper().toFhir(clinician);
        String resource = parser.encodeResourceToString(practitioner);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/ips")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> patientIPS(@PathVariable() String facilityId, @PathVariable() String patientId) {
        Facility facility = facilityRepository.findById(facilityId).orElseThrow();
        EhrPatient ehrPatient = patientRepository.findById(patientId).orElseThrow();

        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        IBaseResource iBaseResource = fhirComponentsDispatcher.ipsWriter().ipsBundle(ehrPatient, facility);
        String resource = parser.encodeResourceToString(iBaseResource);
//        CredentialSubject
        return ResponseEntity.ok(resource);
    }

    @PostMapping(FACILITY_PREFIX + "/{facilityId}/$qrCode")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<List<String>> qrCode(@PathVariable() String facilityId, @RequestBody String resourceString, HttpServletRequest request) {
        Facility facility = facilityRepository.findById(facilityId).orElseThrow();
        return smartHealthCardService.qrCode(facility, resourceString, request);
    }


    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/bundle")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> vxuEquivalentTransaction(@PathVariable() String facilityId, @PathVariable() String patientId, @PathVariable() String vaccinationId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        IBaseBundle iBaseBundle = fhirComponentsDispatcher.fhirTransactionWriter()
                .vxuBundle(facilityRepository.findById(facilityId).get(), vaccinationEventRepository.findById(vaccinationId).get());
        String resource = parser.encodeResourceToString(iBaseBundle);
        return ResponseEntity.ok(resource);
    }


    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$loadJson")
    public ResponseEntity<String> loadNdJsonBundle(
            @PathVariable() String facilityId,
            @PathVariable() String registryId,
            @RequestBody IBaseBundle bundle) {
        return fhirComponentsDispatcher.bundleImportService().importBundle(
                immunizationRegistryService.getImmunizationRegistry(registryId),
                facilityRepository.findById(facilityId).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified")
                ), bundle);
    }


    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}" + IMM_REGISTRY_SUFFIX + "/$loadNdJson")
    public ResponseEntity bulkResultLoad(@PathVariable() String facilityId, @PathVariable() String registryId, @RequestBody String ndjson) {
        ImmunizationRegistry ir = immunizationRegistryService.getImmunizationRegistry(registryId);
        return loadNdJson(ir, facilityRepository.findById(facilityId).get(), ndjson);
    }

    private ResponseEntity<String> loadNdJson(ImmunizationRegistry immunizationRegistry, Facility facility, String ndJson) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newNDJsonParser();
        IBaseBundle bundle = (IBaseBundle) parser.parseResource(ndJson);
        return fhirComponentsDispatcher.bundleImportService().importBundle(immunizationRegistry, facility, bundle);
    }
}
