package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import ca.uhn.fhir.rest.gclient.TokenCriterion;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.ClinicianRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Tenant;
import org.immregistries.ehr.fhir.Client.CustomClientBuilder;
import org.immregistries.ehr.fhir.ServerR5.ImmunizationProviderR5;
import org.immregistries.ehr.logic.RandomGenerator;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.immregistries.ehr.api.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.immregistries.ehr.api.controllers.FhirClientController.IMM_REGISTRY_SUFFIX;
import static org.immregistries.ehr.logic.mapping.PatientMapperR5.MRN_SYSTEM;

@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/patients", "/facilities/{facilityId}/patients"})
public class EhrPatientController {

    @Autowired
    FhirContext fhirContext;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    RandomGenerator randomGenerator;
    @Autowired
    FhirClientController fhirClientController;

    private static final Logger logger = LoggerFactory.getLogger(EhrPatientController.class);
    @Autowired
    private CustomClientBuilder customClientBuilder;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private ImmunizationProviderR5 immunizationProviderR5;
    @Autowired
    private ClinicianRepository clinicianRepository;


    @GetMapping()
    public Iterable<EhrPatient> patients(@PathVariable() int facilityId) {
        return  ehrPatientRepository.findByFacilityId(facilityId);
    }

    @GetMapping("/{patientId}")
    public Optional<EhrPatient> patient(
                                        @PathVariable() int facilityId,
                                        @PathVariable() String patientId) {
        return  ehrPatientRepository.findById(patientId);
    }
    @GetMapping("/{patientId}/$history")
    public List<Revision<Integer, EhrPatient>> patientHistory(
                                        @PathVariable() int facilityId,
                                        @PathVariable() String patientId) {

        Revisions<Integer, EhrPatient> revisions = ehrPatientRepository.findRevisions(patientId);
//        logger.info("{}", revisions.getLatestRevision() );
        return revisions.getContent();
    }



    @PostMapping()
    public ResponseEntity<String> postPatient(
//            @PathVariable() int tenantId,
                                               @PathVariable() int facilityId,
                                               @RequestBody EhrPatient patient) {
        // patient data check + flavours
//        Optional<Tenant> tenant = tenantRepository.findById(tenantId);
        Optional<Facility> facility = facilityRepository.findById(facilityId);
        if (
//                !tenant.isPresent() ||
                !facility.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid tenant id or facilityId");
        }
        // patient.setTenant(tenant.get());
        patient.setFacility(facility.get());
        patient.setCreatedDate(new Date());
        patient.setUpdatedDate(new Date());
        EhrPatient newEntity = ehrPatientRepository.save(patient);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEntity.getId())
                .toUri();
        return ResponseEntity.created(location).body(newEntity.getId());
//        return ResponseEntity.created(location).body("Patient " + newEntity.getId() + " saved");
    }

    @PutMapping("")
    public EhrPatient putPatient(@PathVariable() int tenantId,
                                 @PathVariable() int facilityId,
                                 @RequestBody EhrPatient patient) {
        // patient data check + flavours
        Optional<Tenant> tenant = tenantRepository.findById(tenantId);
        Optional<Facility> facility = facilityRepository.findById(facilityId);
        Optional<EhrPatient> oldPatient = ehrPatientRepository.findByFacilityIdAndId(facilityId,patient.getId());
        if (!tenant.isPresent() || !facility.isPresent() || !oldPatient.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Invalid ids");
        }
        // patient.setTenant(tenant.get());
        patient.setFacility(facility.get());
        patient.setUpdatedDate(new Date());
        EhrPatient newEntity = ehrPatientRepository.save(patient);
        return newEntity;
//        return ResponseEntity.created(location).body("Patient " + newEntity.getId() + " saved");
    }

    @GetMapping("/{patientId}/fhir-client" + IMM_REGISTRY_SUFFIX + "/$fetchAndLoad")
    public ResponseEntity<String> fetchAndLoadImmunizationsFromIIS(@PathVariable() int facilityId,
                                                      @PathVariable() String patientId,
                                                      @PathVariable() Integer immRegistryId,
                                                      @RequestParam Optional<Long> _since) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));

        EhrPatient patient = ehrPatientRepository.findByFacilityIdAndId(facilityId,patientId).orElseThrow();
        ImmunizationRegistry immunizationRegistry = immunizationRegistryController.settings(immRegistryId);
        IGenericClient client = customClientBuilder.newGenericClient(immRegistryId);

        Bundle searchBundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode(MRN_SYSTEM,patient.getMrn()))
                .returnBundle(Bundle.class).execute();
        String id = null;
        for (Bundle.BundleEntryComponent entry: searchBundle.getEntry()) {
            if (entry.getResource().getMeta().hasTag()) { // TODO better condition to check if golden record
                id = new IdType(entry.getResource().getId()).getIdPart();
                break;
            }
        }
        if (id != null) {
            Parameters in = new Parameters()
                    .addParameter("_mdm", "true")
                    .addParameter("_type", "Immunization,ImmunizationRecommendation");
            Bundle outBundle = client.operation()
                    .onInstance("Patient/" + id)
                    .named("$everything")
                    .withParameters(in)
                    .prettyPrint()
                    .useHttpGet()
                    .returnResourceType(Bundle.class).execute();
            RequestDetails requestDetails = new ServletRequestDetails();
            requestDetails.setTenantId(String.valueOf(facilityId));
            for (Bundle.BundleEntryComponent entry: outBundle.getEntry()) {
                try {
                    MethodOutcome methodOutcome = immunizationProviderR5.updateImmunization((Immunization) entry.getResource(), requestDetails,immunizationRegistry, facility, patientId);
                } catch (ClassCastException classCastException ){
                    //Ignoring other resources
                }
                //TODO Recommendations
            }
        }
        return ResponseEntity.internalServerError().body("IDENTIFIER ERROR");
    }
}
