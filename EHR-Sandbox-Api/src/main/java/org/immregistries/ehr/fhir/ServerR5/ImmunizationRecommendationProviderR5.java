package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;

@Controller
@Conditional(OnR5Condition.class)
public class ImmunizationRecommendationProviderR5 implements IResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(ImmunizationRecommendationProviderR5.class);
    public Class<ImmunizationRecommendation> getResourceType() {
        return ImmunizationRecommendation.class;
    }

    @Update
    public MethodOutcome updateImmunizationRecommendation(@ResourceParam ImmunizationRecommendation immunizationRecommendation, RequestDetails requestDetails) {
        logger.info("{}", immunizationRecommendation);
        return new MethodOutcome() ;
    }

}
