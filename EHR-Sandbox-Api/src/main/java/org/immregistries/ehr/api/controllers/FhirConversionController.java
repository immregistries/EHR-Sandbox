package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Group;
import org.hl7.fhir.r5.model.Immunization;
import org.hl7.fhir.r5.model.Patient;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.logic.BundleImportService;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.GroupMapperR5;
import org.immregistries.ehr.logic.mapping.ImmunizationMapperR5;
import org.immregistries.ehr.logic.mapping.PatientMapperR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.immregistries.ehr.api.controllers.FhirClientController.*;

@RestController
public class FhirConversionController {
    Logger logger = LoggerFactory.getLogger(FhirConversionController.class);

    @Autowired
    private PatientMapperR5 patientMapper;
    @Autowired
    private ImmunizationMapperR5 immunizationMapper;
    @Autowired
    private GroupMapperR5 groupMapperR5;

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
    @Autowired
    private EhrGroupRepository ehrGroupRepository;

    @GetMapping(PATIENT_PREFIX + "/{patientId}/resource")
    public ResponseEntity<String> getPatientAsResource(
            @PathVariable() String patientId, @PathVariable() String facilityId) {
        EhrPatient ehrPatient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No patient found"));
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility found"));
        Patient patient = patientMapper.toFhirPatient(ehrPatient, facility);
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(true);
        String resource = parser.encodeResourceToString(patient);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/resource")
    public ResponseEntity<String> immunizationResource(
            @RequestAttribute() VaccinationEvent vaccinationEvent,
            @RequestAttribute() EhrPatient patient,
            @RequestAttribute Facility facility) {
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
//        System.out.println(vaccinationEvent.getVaccine());
        /**
         * not sure why this is a necessary step, but not problematic as links were checked in authorization filter
         */
        vaccinationEvent.setPatient(patient);
        Immunization immunization =
                immunizationMapper.toFhirImmunization(vaccinationEvent,
                        resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility));
        String resource = parser.encodeResourceToString(immunization);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(GROUPS_PREFIX + "/{groupId}/resource")
    @Transactional(readOnly=true, noRollbackFor=Exception.class)
    public ResponseEntity<String> groupResource(
            @PathVariable() String groupId,
            @RequestAttribute() Facility facility) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        Group group = groupMapperR5.toFhirGroup(ehrGroup);
        String resource = parser.encodeResourceToString(group);
        return ResponseEntity.ok(resource);
    }

    @PostMapping("/tenant/{tenantId}/facilities/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$loadJson")
    public ResponseEntity<String> loadNdJsonBundle(
            @PathVariable() String facilityId,
            @PathVariable() Integer registryId,
            @RequestBody Bundle bundle) {
        return bundleImportService.importBundle(
                immunizationRegistryController.getImmunizationRegistry(registryId),
                facilityRepository.findById(facilityId).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No facility name specified")
                ), bundle);
    }


    @PostMapping("/tenant/{tenantId}/facilities/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$loadNdJson")
    public ResponseEntity bulkResultLoad(@PathVariable() Integer registryId, @RequestBody String ndjson, @RequestAttribute Facility facility) {
        ImmunizationRegistry ir = immunizationRegistryController.getImmunizationRegistry(registryId);
        return loadNdJson(ir, facility, ndjson);
    }

    private ResponseEntity<String> loadNdJson(ImmunizationRegistry immunizationRegistry, Facility facility, String ndJson) {
        IParser parser = fhirContext.newNDJsonParser();
        Bundle bundle = (Bundle) parser.parseResource(ndJson);
        return bundleImportService.importBundle(immunizationRegistry, facility, bundle);
    }

    private String validateNdJsonBundle(Bundle bundle) {
        FhirValidator validator = fhirContext.newValidator();
        IValidatorModule module = new FhirInstanceValidator(fhirContext);
        validator.registerValidatorModule(module);
        return "";
    }
}
