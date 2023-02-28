package org.immregistries.ehr.logic;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationIdentifier;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.PatientIdentifier;
import org.immregistries.ehr.api.repositories.ImmunizationIdentifierRepository;
import org.immregistries.ehr.api.repositories.PatientIdentifierRepository;
import org.immregistries.ehr.fhir.ServerR5.ImmunizationProviderR5;
import org.immregistries.ehr.fhir.ServerR5.PatientProviderR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service()
public class BundleImportService {
    private static final Logger logger = LoggerFactory.getLogger(BundleImportService.class);


    @Autowired
    PatientProviderR5 patientProvider;
    @Autowired
    ImmunizationProviderR5 immunizationProvider;

    @Autowired
    private ImmunizationIdentifierRepository immunizationIdentifierRepository;
    @Autowired
    private PatientIdentifierRepository patientIdentifierRepository;

    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    public ResponseEntity<String> importBundle(ImmunizationRegistry immunizationRegistry, Facility facility, Bundle bundle ) {
        StringBuilder responseBuilder = new StringBuilder(); // todo bundle loader as service ?
        int count = 0;
        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            switch (entry.getResource().getResourceType()) {
                case Patient: {
                    Patient patient = (Patient) entry.getResource();
                    String receivedId = new IdType(patient.getId()).getIdPart();
                    String dbId; // = patientIdentifier.get(immunizationRegistry.getId()).getOrDefault(receivedId,-1);
                    MethodOutcome methodOutcome = patientProvider.createPatient(patient,facility);
                    dbId = methodOutcome.getId().getValue();
                    patientIdentifierRepository.save(new PatientIdentifier(dbId,immunizationRegistry.getId(),receivedId));
                    responseBuilder.append("\nPatient ").append(receivedId).append(" loaded as patient ").append(dbId);
                    logger.info("Patient  {}  loaded as patient  {}",receivedId,dbId);
                    count++;
                    break;
                }
                case Immunization: {
                    Immunization immunization = (Immunization) entry.getResource();
                    String dbId; // = immunizationIdentifier.get(immunizationRegistry.getId()).getOrDefault(immunization.getId(),-1);
                    String receivedId = new IdType(immunization.getId()).getIdPart();
                    String receivedPatientId = new IdType(immunization.getPatient().getReference()).getIdPart();
                    Optional<PatientIdentifier> patientIdentifier = patientIdentifierRepository.findByPatientIdAndImmunizationRegistryId(receivedPatientId,immunizationRegistry.getId());
                    if (patientIdentifier.isPresent()) {
                        immunization.setPatient(new Reference("Patient/" + patientIdentifier.get().getPatientId()));
                        MethodOutcome methodOutcome = immunizationProvider.createImmunization(immunization,facility);
                        dbId = methodOutcome.getId().getValue();
                        immunizationIdentifierRepository.save(new ImmunizationIdentifier(dbId,immunizationRegistry.getId(),receivedId));
                        responseBuilder.append("\nImmunization ").append(receivedId).append(" loaded as Immunization ").append(dbId);
                        logger.info("Immunization {} loaded as Immunization {}",receivedId,dbId);
                        count++;
                    } else {
                        responseBuilder.append("\nERROR : ").append(immunization.getPatient().getReference()).append(" Unknown");
                        logger.info("ERROR : Patient  {}  Unknown",immunization.getPatient().getReference());
                    }
                    break;
                }
            }
        }
        responseBuilder.append("\nNumber of successful load in facility ").append(facility.getNameDisplay()).append(": ").append(count);
        return ResponseEntity.ok(responseBuilder.toString());
    }
}
