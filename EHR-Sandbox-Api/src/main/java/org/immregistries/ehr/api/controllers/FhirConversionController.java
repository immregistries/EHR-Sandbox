package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.util.BundleBuilder;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.logic.BundleImportService;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.immregistries.ehr.logic.mapping.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.immregistries.ehr.api.controllers.FhirClientController.*;

@RestController
public class FhirConversionController {
    Logger logger = LoggerFactory.getLogger(FhirConversionController.class);

    @Autowired
    private IPatientMapper patientMapper;
    @Autowired
    private IImmunizationMapper immunizationMapper;
    @Autowired
    private IGroupMapper groupMapper;
    @Autowired
    private IOrganizationMapper organizationMapper;
    @Autowired
    private IPractitionerMapper practitionerMapper;

    @Autowired
    private BundleImportService bundleImportService;
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
        IBaseResource patient = patientMapper.toFhir(ehrPatient, facility);
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
        /**
         * not sure why this is a necessary step, but not problematic as links were checked in authorization filter
         */
        vaccinationEvent.setPatient(patient);
        IBaseResource immunization = immunizationMapper.toFhir(vaccinationEvent,
                resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility));
        String resource = parser.encodeResourceToString(immunization);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(GROUPS_PREFIX + "/{groupId}/resource")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> groupResource(
            @PathVariable() String groupId,
            @RequestAttribute() Facility facility) {
        EhrGroup ehrGroup = ehrGroupRepository.findById(groupId).get();
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        IBaseResource group = groupMapper.toFhir(ehrGroup);
        String resource = parser.encodeResourceToString(group);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(FACILITY_PREFIX + "/{facilityId}/resource")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> facilityResource(
            @RequestAttribute() Facility facility) {
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        IBaseResource organization = organizationMapper.toFhir(facility);
        String resource = parser.encodeResourceToString(organization);
        return ResponseEntity.ok(resource);
    }


    @GetMapping(FACILITY_PREFIX + "/{facilityId}/bundle")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> facilityAllResourcesTransaction(@RequestAttribute() Facility facility) {
        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        Bundle bundle = new Bundle(Bundle.BundleType.TRANSACTION);

        Organization organization = (Organization) organizationMapper.toFhir(facility);
        Bundle.BundleEntryComponent organizationEntry = bundle.addEntry().setResource(organization)
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setRequest(new Bundle.BundleEntryRequestComponent(
                        Bundle.HTTPVerb.PUT,
                        identifierUrl("Organization", organization.getIdentifierFirstRep())));;
        for (EhrPatient ehrPatient : patientRepository.findByFacilityId(facility.getId())) {
            Patient patient = (Patient) patientMapper.toFhir(ehrPatient);
            patient.setManagingOrganization(new Reference(organizationEntry.getFullUrl()));
//            String patientRequestUrl = identifierUrl("Patient", patient.getIdentifierFirstRep());
            String patientRequestUrl = "Patient";
            Bundle.BundleEntryComponent patientEntry = bundle.addEntry()
                    .setFullUrl("urn:uuid:" + UUID.randomUUID())
                    .setResource(patient)
                    .setRequest(new Bundle.BundleEntryRequestComponent(Bundle.HTTPVerb.POST, patientRequestUrl));
            for (VaccinationEvent vaccinationEvent : ehrPatient.getVaccinationEvents()) {
                Immunization immunization = (Immunization) immunizationMapper.toFhir(vaccinationEvent,
                        resourceIdentificationService.getFacilityImmunizationIdentifierSystem(facility));
                immunization.setPatient(new Reference(patientEntry.getFullUrl()));
                String immunizationRequestUrl = "Immunization";
//                String immunizationRequestUrl = identifierUrl("Immunization", immunization.getIdentifierFirstRep());
                Bundle.BundleEntryComponent immunizationEntry = bundle.addEntry()
                        .setFullUrl("urn:uuid:" + UUID.randomUUID())
                        .setResource(immunization)
                        .setRequest(new Bundle.BundleEntryRequestComponent(Bundle.HTTPVerb.POST, immunizationRequestUrl));
            }
        }
//        for (Clinician clinician: clinicianRepository.findByTenantId(facility.getTenant().getId())) {
//            Practitioner practitioner = (Practitioner) practitionerMapper.toFhir(clinician);
//            bundleBuilder.addTransactionCreateEntry()
//        }
//        for (EhrGroup ehrGroup: facility.getGroups()) {
//            Group group = groupMapper.toFhir(E)
//        }
        String resource = parser.encodeResourceToString(bundle);
        return ResponseEntity.ok(resource);
    }

    private String identifierUrl(String base, Identifier identifier) {
        if (identifier != null) {
            base += "?identifier=";
            if (identifier.hasSystem()) {
                base += identifier.getSystem() + "|";
            }
            base += identifier.getValue();
        }
        return base;

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
