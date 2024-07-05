package org.immregistries.ehr.fhir.Client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NOT WORKING
 * For some reason setting it as narrativeGenerator in FhirContext doesn't change anything, no logs
 */
public class CustomNarrativeGenerator implements INarrativeGenerator {
    Logger logger = LoggerFactory.getLogger(CustomNarrativeGenerator.class);

    public boolean populateResourceNarrative(FhirContext var1, IBaseResource var2) {
//        logger.info("populateResourceNarrative");
        return true;
    }

    public String generateResourceNarrative(FhirContext var1, IBaseResource var2) {
//        logger.info("generateResourceNarrative");
        return "";
    }
}
