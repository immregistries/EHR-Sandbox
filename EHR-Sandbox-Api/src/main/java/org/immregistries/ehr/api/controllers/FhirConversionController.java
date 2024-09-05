package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

import static org.immregistries.ehr.api.controllers.FhirClientController.*;

@RestController
public class FhirConversionController {
    private static final int MAX_SINGLE_JWS_SIZE = 1195;
    private static final int MAX_CHUNK_SIZE = 1191;

    private static final int MAXIMUM_DATA_SIZE = 30000;
    private static final int SMALLEST_B64_CHAR_CODE = 45;
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
    public ResponseEntity<List<String>> qrCode(@PathVariable() String facilityId, @RequestBody String resourceString, HttpServletRequest request) {
//        Facility facility = facilityRepository.findById(facilityId).orElseThrow();
//        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser().setPrettyPrint(true).setSuppressNarratives(true);
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

        String issuerUrl = request.getRequestURL().substring(0, request.getRequestURL().indexOf("/tenants"));
        Claims claims = Jwts.claims()
                .notBefore(new Date())
                .issuer(issuerUrl)
                .issuedAt(new Date())
//                .subject("testSubject")
                .add("vc", mapVc)
                .build();

        Gson gson = new Gson();
        String notDeflated = gson.toJson(claims).strip();
        /**
         * Compressing the content
         */
        byte[] output = new byte[MAXIMUM_DATA_SIZE];
        Deflater deflater = new Deflater();
        deflater.setInput(notDeflated.getBytes());
        deflater.finish();
        int compressedDataSize = deflater.deflate(output);
        if (compressedDataSize >= MAXIMUM_DATA_SIZE) {
            throw new InternalErrorException("Resource is too large");
        }
        byte[] deflated = Arrays.copyOfRange(output, 0, compressedDataSize);

//        logger.info("df {}", compressedDataSize);
//        try {
//            Inflater decompresser = new Inflater();
//            decompresser.setInput(deflated);
//            byte[] result = new byte[notDeflated.length() + 1];
//            int resultLength = decompresser.inflate(result);
//            decompresser.end();
//            logger.info("result {} {} ", resultLength, StringUtils.toEncodedString(result, StandardCharsets.UTF_8),
//                    Base64.getEncoder().encodeToString(result));
//        } catch (DataFormatException e) {
//            throw new RuntimeException(e);
//        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String compact = jwtUtils.signForQrCode(authentication, deflated);

        // for download file
//        Map<String, ArrayList<String>> shcMap = new HashMap<>(1);
//        ArrayList<String> arrayList = new ArrayList<>(1);
//        arrayList.add(compact);
//        shcMap.put("verifiableCredential", arrayList);
//        logger.info("shcMap: {}", shcMap);
        String encodedForQrCode = compact.
                chars().map(value -> value - SMALLEST_B64_CHAR_CODE)
                .boxed()
                .map(integer -> String.valueOf(integer / 10) + integer % 10)
                .collect(Collectors.joining());
        int finalLength = encodedForQrCode.length();
        List<String> result;
        if (finalLength < MAX_SINGLE_JWS_SIZE) {
            result = List.of("shc:/" + encodedForQrCode);
        } else {
            int chunkNumber;
            chunkNumber = finalLength % MAX_CHUNK_SIZE > 0 ? finalLength / MAX_CHUNK_SIZE + 1 : finalLength / MAX_CHUNK_SIZE;
            result = new ArrayList<>(chunkNumber);
            int chunkSize = finalLength / chunkNumber;
            int i = 1;
            while (i < chunkNumber - 1) {
                result.add("shc:/" + i + "/" + chunkNumber + "/" +
                        encodedForQrCode.substring((i - 1) * chunkSize, i * chunkSize));
                i++;
            }
            result.add("shc:/" + i + "/" + chunkNumber + "/" +
                    encodedForQrCode.substring((i - 1) * chunkSize, finalLength - 1));
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(1);
        headers.add("issuerKey", jwtUtils.getUserPublicJwk(authentication).toString());
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
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
