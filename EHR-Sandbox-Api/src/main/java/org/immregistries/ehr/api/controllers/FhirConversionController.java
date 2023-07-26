package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Immunization;
import org.hl7.fhir.r5.model.Patient;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.logic.BundleImportService;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

import static org.immregistries.ehr.api.controllers.FhirClientController.*;

@RestController
public class FhirConversionController {
    @Autowired
    private PatientMapperR5 patientMapper;
    @Autowired
    private ImmunizationMapperR5 immunizationMapper;

    @Autowired
    private BundleImportService bundleImportService;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;
    @Autowired
    private EhrPatientRepository patientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private FhirContext fhirContext;

    @GetMapping(PATIENT_PREFIX + "/{patientId}/resource")
    public ResponseEntity<String> getPatientAsResource(
            @PathVariable() String patientId, @PathVariable() Integer facilityId) {
        EhrPatient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No facility found"));

        Patient fhirPatient = patientMapper.toFhirPatient(patient,
                resourceIdentificationService.getFacilityPatientIdentifierSystem(facility));
        fhirPatient.setText(null);
        String resource = parser.encodeResourceToString(fhirPatient);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/resource")
    public ResponseEntity<String> immunizationResource(
            HttpServletRequest request,
            @PathVariable() String vaccinationId,
            @PathVariable() Integer facilityId) {
        VaccinationEvent vaccinationEvent = vaccinationEventRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No vaccination found"));
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_ACCEPTABLE, "No facility found"));
        Immunization immunization =
                immunizationMapper.toFhirImmunization(vaccinationEvent,
                        resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility),
                        resourceIdentificationService.getFacilityPatientIdentifierSystem(facility));
        String resource = parser.encodeResourceToString(immunization);
        return ResponseEntity.ok(resource);
    }

    @PostMapping("/tenant/{tenantId}/facilities/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$loadJson")
    public ResponseEntity<String> loadNdJsonBundle(
            @PathVariable() Integer facilityId,
            @PathVariable() Integer registryId,
            @RequestBody Bundle bundle) {
        return bundleImportService.importBundle(
                immunizationRegistryController.settings(registryId),
                facilityRepository.findById(facilityId).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified")
                ), bundle);
    }


    @PostMapping("/tenant/{tenantId}/facilities/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$loadNdJson")
    public ResponseEntity bulkResultLoad(@PathVariable() Integer registryId, @RequestBody String ndjson, @PathVariable Integer facilityId) {
        ImmunizationRegistry ir = immunizationRegistryController.settings(registryId);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified"));
        return loadNdJson(ir, facility,ndjson);
    }

    private ResponseEntity<String> loadNdJson(ImmunizationRegistry immunizationRegistry, Facility facility, String ndJson) {
        IParser parser = fhirContext.newNDJsonParser();
        Bundle bundle = (Bundle) parser.parseResource(ndJson);
        return bundleImportService.importBundle(immunizationRegistry,facility, bundle);
    }

    private String validateNdJsonBundle(Bundle bundle ) {
//        IValidator validator = new
        FhirValidator validator = fhirContext.newValidator();

//        IValidatorModule coreModule = new
        IValidatorModule module = new FhirInstanceValidator(fhirContext);
        validator.registerValidatorModule(module);

        return "";
    }
}
