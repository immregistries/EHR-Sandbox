package org.immregistries.ehr.logic;

import org.hl7.fhir.r4.model.Bundle;
import org.immregistries.ehr.api.ProcessingFlavor;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.mitre.synthea.engine.Generator;
import org.mitre.synthea.export.FhirR4;
import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.world.agents.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RandomGeneratorService extends FullRandomGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RandomGeneratorService.class);


    @Autowired
    private FullRandomGenerator fullRandomGenerator;

    @Autowired
    private BundleImportServiceR4 bundleImportServiceR4;

    private final Generator generator;

    public RandomGeneratorService() {
        Generator.GeneratorOptions options = new Generator.GeneratorOptions();
        options.population = 0;
        options.enabledModules = List.of("patient", "immunization");

        Config.set("modules.enabled", "[]");
        Config.set("exporter.hospital.fhir.export", "false");
        Config.set("exporter.practitioner.fhir.export", "false");
        Config.set("exporter.fhir_r4.export", "true");
        Config.set("exporter.fhir.export", "true");
//        Exporter.ExporterRuntimeOptions ero = new Exporter.ExporterRuntimeOptions();
        generator = new Generator(options);
    }


    public EhrPatient randomPatient(Facility facility) {
        if (ProcessingFlavor.BLACKJACK.isActive()) {
            return fullRandomGenerator.fullRandomPatient(facility);
        }
        return randomSyntheaPatient(facility);
    }

    private EhrPatient randomSyntheaPatient(Facility facility) {
        Random random = new Random();
        Map<String, Object> demographics = generator.randomDemographics(generator.getRandomizer());
        Person person = generator.createPerson(random.nextLong(), demographics);
//        Person person = generator.generatePerson(2, random.nextLong());
        Bundle bundle = FhirR4.convertToFHIR(person, new Date().getTime());
//        logger.info("Synthea Gen {}", bundle.getEntry().size());
        EhrPatient ehrPatient = bundleImportServiceR4.convertToLocalPatients(bundle, facility).stream().findFirst().orElse(null);
        assert ehrPatient != null;
        if (ehrPatient.getIdentifiers().isEmpty()) {
            ehrPatient.getIdentifiers().add(fullRandomGenerator.randomEhrIdentifierMrn(facility));
        }
//        logger.info("Vaccinations generated {}", ehrPatient.getVaccinationEvents());
        return ehrPatient;
    }

    public Set<EhrPatient> randomSyntheaPatientList(Facility facility) {
        Set<EhrPatient> ehrPatients = new HashSet<>(3);
        ehrPatients.add(randomSyntheaPatient(facility));
        ehrPatients.add(randomSyntheaPatient(facility));
        ehrPatients.add(randomSyntheaPatient(facility));
        return ehrPatients;
    }
}
