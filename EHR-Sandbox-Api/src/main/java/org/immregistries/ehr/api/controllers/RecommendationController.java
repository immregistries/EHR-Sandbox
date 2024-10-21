package org.immregistries.ehr.api.controllers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.ImmunizationRegistryService;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.VaccinationEvent;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.VaccinationEventRepository;
import org.immregistries.ehr.fhir.FhirComponentsDispatcher;
import org.immregistries.ehr.logic.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static org.immregistries.ehr.api.controllers.ControllerHelper.*;

@RestController()
public class RecommendationController {
    Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    @Autowired()
    private FhirComponentsDispatcher fhirComponentsDispatcher;
    @Autowired
    private ImmunizationRegistryService immunizationRegistryService;

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private EhrPatientRepository ehrPatientRepository;
    @Autowired
    private VaccinationEventRepository vaccinationEventRepository;

    @GetMapping(PATIENT_ID_PATH + "/recommendations")
    public ResponseEntity<Set<String>> getAll(@PathVariable(FACILITY_ID) Integer facilityId, @PathVariable(PATIENT_ID) Integer patientId) {
        IParser parser = fhirComponentsDispatcher.fhirContext().newJsonParser();
        Set<String> set = recommendationService.getPatientMap(facilityId, patientId).entrySet().stream().map(
                        entry -> parser.encodeResourceToString(entry.getValue()))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(set);
    }

    @GetMapping(PATIENT_ID_PATH + "/fhir-client" + REGISTRY_COMPLETE_SUFFIX + "/$immds-forecast")
    public String immdsForecastOperation(@PathVariable(REGISTRY_ID) Integer registryId, @PathVariable(PATIENT_ID) Integer patientId, @PathVariable(FACILITY_ID) Integer facilityId) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryService.getImmunizationRegistry(registryId);
        IGenericClient client = fhirComponentsDispatcher.clientFactory().newGenericClient(immunizationRegistry);
        EhrPatient ehrPatient = ehrPatientRepository.findById(patientId).get();
        IBaseParameters out = null;
        if (ProcessingFlavor.R4.isActive()) {
            org.hl7.fhir.r4.model.Patient patient = (org.hl7.fhir.r4.model.Patient) fhirComponentsDispatcher.patientMapper().toFhir(ehrPatient);
            org.hl7.fhir.r4.model.Parameters in = new org.hl7.fhir.r4.model.Parameters();
            in.addParameter().setName("assessmentDate").setValue(new org.hl7.fhir.r4.model.DateType(new Date()));
            in.addParameter().setName("patient").setResource(patient);
            for (VaccinationEvent vaccinationEvent : vaccinationEventRepository.findByPatientId(patientId)) {
                org.hl7.fhir.r4.model.Immunization immunization = (org.hl7.fhir.r4.model.Immunization) fhirComponentsDispatcher.immunizationMapper().toFhir(vaccinationEvent, "");
                in.addParameter().setName("immunization").setResource(immunization);
            }
            out = client.operation().onServer().named("$immds-forecast").withParameters(in).execute();
            recommendationService.saveInStore((IDomainResource) ((org.hl7.fhir.r4.model.Parameters) out).getParameter("recommendation").getResource(), facilityId, patientId, immunizationRegistry);
        } else {
            org.hl7.fhir.r5.model.Patient patient = (org.hl7.fhir.r5.model.Patient) fhirComponentsDispatcher.patientMapper().toFhir(ehrPatient);
            org.hl7.fhir.r5.model.Parameters in = new org.hl7.fhir.r5.model.Parameters();
            in.addParameter().setName("assessmentDate").setValue(new org.hl7.fhir.r5.model.DateType(new Date()));
            in.addParameter().setName("patient").setResource(patient);
            for (VaccinationEvent vaccinationEvent : vaccinationEventRepository.findByPatientId(patientId)) {
                org.hl7.fhir.r5.model.Immunization immunization = (org.hl7.fhir.r5.model.Immunization) fhirComponentsDispatcher.immunizationMapper().toFhir(vaccinationEvent, "");
                in.addParameter().setName("immunization").setResource(immunization);
            }
            out = client.operation().onServer().named("$immds-forecast").withParameters(in).execute();
            recommendationService.saveInStore((IDomainResource) ((org.hl7.fhir.r5.model.Parameters) out).getParameter("recommendation").getResource(), facilityId, patientId, immunizationRegistry);
        }

        return fhirComponentsDispatcher.fhirContext().newJsonParser().encodeResourceToString(out);
    }


}
