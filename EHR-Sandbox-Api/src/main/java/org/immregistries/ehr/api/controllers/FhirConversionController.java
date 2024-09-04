package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrSegment;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.*;
import org.immregistries.ehr.api.security.JwtUtils;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.BundleImportService;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.Deflater;

import static org.immregistries.ehr.api.controllers.FhirClientController.*;

@RestController
public class FhirConversionController {
    Logger logger = LoggerFactory.getLogger(FhirConversionController.class);

    @Autowired
    FhirComponentsDispatcher fhirComponentsDispatcher;

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
    private ImmunizationRegistryService immunizationRegistryService;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;

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
        Bundle bundle = new Bundle(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryComponent organizationEntry = addOrganizationEntry(bundle, facility);
        for (EhrPatient ehrPatient : patientRepository.findByFacilityId(facilityId)) {
            Bundle.BundleEntryComponent patientEntry = addPatientEntry(bundle, organizationEntry.getFullUrl(), ehrPatient);
            for (VaccinationEvent vaccinationEvent : ehrPatient.getVaccinationEvents()) {
                Bundle.BundleEntryComponent immunizationEntry = addVaccinationEntry(bundle, patientEntry.getFullUrl(), vaccinationEvent);
            }
        }
//        for (Clinician clinician: clinicianRepository.findByTenantId(facility.getTenant().getId())) {
//            Practitioner practitioner = (Practitioner) fhirComponentsService.practitionerMapper().toFhir(clinician);
//            bundleBuilder.addTransactionCreateEntry()
//        }
//        for (EhrGroup ehrGroup: facility.getGroups()) {
//            Group group = groupMapper.toFhir(E)
//        }
        String resource = parser.encodeResourceToString(bundle);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(PATIENT_PREFIX + "/{patientId}/bundle")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> qpdEquivalentTransaction(@PathVariable() String facilityId, @PathVariable() String patientId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        Bundle bundle = new Bundle(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryComponent organizationEntry = addOrganizationEntry(bundle, facilityRepository.findById(facilityId).get());
        Bundle.BundleEntryComponent patientEntry = addPatientEntry(bundle, organizationEntry.getFullUrl(), patientRepository.findById(patientId).get());
        String resource = parser.encodeResourceToString(bundle);
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

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping(FACILITY_PREFIX + "/{facilityId}/$qrCode")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<?> qrCode(@PathVariable() String facilityId, @RequestBody String resourceString) {
        Facility facility = facilityRepository.findById(facilityId).orElseThrow();
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true).setSuppressNarratives(true);
//        IBaseResource iBaseResource = parser.parseResource(resourceString);

        Map<String, Object> mapVc = new HashMap<>(2);
        ArrayList<String> type = new ArrayList<>(3);
        type.add("VerifiableCredential");
        type.add("https://smarthealth.cards#health-card");
        type.add("https://smarthealth.cards#immunization");
        mapVc.put("type", type);

        Map<String, Object> credentialSubject = new HashMap<>(2);
        credentialSubject.put("fhirVersion", fhirComponentsDispatcher.fhirContext().getVersion().getVersion().getFhirVersionString());
        credentialSubject.put("fhirBundle", JsonParser.parseString(resourceString).getAsJsonObject());
        mapVc.put("credentialSubject", credentialSubject);

        Claims claims = Jwts.claims()
                .notBefore(new Date())
                .issuer("test/ehr-sandbox")
                .issuedAt(new Date())
                .subject("testSubject")
                .add("vc", mapVc)
                .build();

        Gson gson = new Gson();
//        logger.info("gson {}", gson.toJson(claims));
        Deflater deflater = new Deflater();
        deflater.setInput(StringUtils.getBytes(gson.toJson(claims).strip(), StandardCharsets.UTF_8));
        byte[] deflated = new byte[100];
        deflater.deflate(deflated);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String compact = jwtUtils.qrCodeEncoder(authentication, claims, deflated);

        Map<String, ArrayList<String>> shcMap = new HashMap<>(1);
        ArrayList<String> arrayList = new ArrayList<>(1);
        arrayList.add(compact);

        shcMap.put("verifiableCredential", arrayList);
        String finalString = gson.toJson(shcMap);

        QrSegment shcSegment = QrSegment.makeBytes("shc:/".getBytes());
        QrSegment segmentContent = QrSegment.makeNumeric(finalString);
//        List<QrSegment> segs = QrSegment.makeSegments(finalString);
//        ArrayList<String> result = new ArrayList<>(segs.size());
//        for (QrSegment segment: segs
//             ) {
//                QrCode qrCode = QrCode.encodeSegments(Arrays.asList(shcSegment, segment), QrCode.Ecc.MEDIUM);
//        }
        QrCode qrCode = QrCode.encodeSegments(Arrays.asList(shcSegment, segmentContent), QrCode.Ecc.MEDIUM);
//        qrCode.
//        QrCode qrCode = QrCode.encodeText(sh, QrCode.Ecc.MEDIUM);
        return ResponseEntity.ok(finalString);
    }


    @GetMapping(IMMUNIZATION_PREFIX + "/{vaccinationId}/bundle")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public ResponseEntity<String> vxuEquivalentTransaction(@PathVariable() String facilityId, @PathVariable() String patientId, @PathVariable() String vaccinationId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true);
        Bundle bundle = new Bundle(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryComponent organizationEntry = addOrganizationEntry(bundle, facilityRepository.findById(facilityId).get());
        Bundle.BundleEntryComponent patientEntry = addPatientEntry(bundle, organizationEntry.getFullUrl(), patientRepository.findById(patientId).get());
        Bundle.BundleEntryComponent vaccinationEntry = addVaccinationEntry(bundle, patientEntry.getFullUrl(), vaccinationEventRepository.findById(vaccinationId).get());
        String resource = parser.encodeResourceToString(bundle);
        return ResponseEntity.ok(resource);
    }

    private Bundle.BundleEntryComponent addOrganizationEntry(Bundle bundle, Facility facility) {
        Organization organization = (Organization) fhirComponentsDispatcher.organizationMapper().toFhir(facility);
        return bundle.addEntry().setResource(organization)
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setRequest(new Bundle.BundleEntryRequestComponent(
                        Bundle.HTTPVerb.PUT,
                        identifierUrl("Organization", organization.getIdentifierFirstRep())));
    }

    private Bundle.BundleEntryComponent addPatientEntry(Bundle bundle, String organizationUrl, EhrPatient ehrPatient) {
        Patient patient = (Patient) fhirComponentsDispatcher.patientMapper().toFhir(ehrPatient);
        patient.setManagingOrganization(new Reference(organizationUrl));
//            String patientRequestUrl = identifierUrl("Patient", patient.getIdentifierFirstRep());
        String patientRequestUrl = "Patient";
        Bundle.BundleEntryComponent patientEntry = bundle.addEntry()
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setResource(patient)
                .setRequest(new Bundle.BundleEntryRequestComponent(Bundle.HTTPVerb.POST, patientRequestUrl));
        return patientEntry;
    }

    private Bundle.BundleEntryComponent addVaccinationEntry(Bundle bundle, String patientUrl, VaccinationEvent vaccinationEvent) {
        Immunization immunization = (Immunization) fhirComponentsDispatcher.immunizationMapper().toFhir(vaccinationEvent,
                resourceIdentificationService.getFacilityImmunizationIdentifierSystem(vaccinationEvent.getAdministeringFacility()));
        immunization.setPatient(new Reference(patientUrl));
        String immunizationRequestUrl = "Immunization";
//        Bundle.BundleEntryComponent clinicianEntry = addClinicianEntry(bundle, vaccinationEvent.getAdministeringClinician());
//        if (clinicianEntry != null) {
//            immunization.addPerformer()
//        }
//        Bundle.BundleEntryComponent clinicianEntry2 = addClinicianEntry(bundle, vaccinationEvent.getEnteringClinician());
//        Bundle.BundleEntryComponent clinicianEntry3 = addClinicianEntry(bundle, vaccinationEvent.getOrderingClinician());
        return bundle.addEntry()
                .setFullUrl("urn:uuid:" + UUID.randomUUID())
                .setResource(immunization)
                .setRequest(new Bundle.BundleEntryRequestComponent(Bundle.HTTPVerb.POST, immunizationRequestUrl));
    }

    private Bundle.BundleEntryComponent addClinicianEntry(Bundle bundle, Clinician clinician) {
        if (Objects.nonNull(clinician)) {
            String immunizationRequestUrl = "Practitioner";
            Practitioner practitioner = (Practitioner) fhirComponentsDispatcher.practitionerMapper().toFhir(clinician);

            return bundle.addEntry()
                    .setFullUrl("urn:uuid:" + UUID.randomUUID())
                    .setResource(practitioner)
                    .setRequest(new Bundle.BundleEntryRequestComponent(Bundle.HTTPVerb.POST, immunizationRequestUrl));
        } else {
            return null;
        }
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

    @PostMapping("/tenants/{tenantId}/facilities/{facilityId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$loadJson")
    public ResponseEntity<String> loadNdJsonBundle(
            @PathVariable() String facilityId,
            @PathVariable() String registryId,
            @RequestBody Bundle bundle) {
        return bundleImportService.importBundle(
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
        Bundle bundle = (Bundle) parser.parseResource(ndJson);
        return bundleImportService.importBundle(immunizationRegistry, facility, bundle);
    }

    private String validateNdJsonBundle(Bundle bundle) {
        FhirValidator validator = fhirComponentsDispatcher.fhirContext().newValidator();
        IValidatorModule module = new FhirInstanceValidator(fhirComponentsDispatcher.fhirContext());
        validator.registerValidatorModule(module);
        return "";
    }
}
