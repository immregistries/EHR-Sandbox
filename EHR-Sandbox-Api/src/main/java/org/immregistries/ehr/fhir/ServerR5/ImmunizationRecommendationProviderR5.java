package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
@Conditional(OnR5Condition.class)
public class ImmunizationRecommendationProviderR5 implements IResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(ImmunizationRecommendationProviderR5.class);
    public Class<ImmunizationRecommendation> getResourceType() {
        return ImmunizationRecommendation.class;
    }

    @Resource
    Map<Integer, Map<String, Set<ImmunizationRecommendation>>> immunizationRecommendationsStore;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;
    @Autowired
    private FacilityRepository facilityRepository;

    @Update
    public MethodOutcome updateImmunizationRecommendation(@ResourceParam ImmunizationRecommendation immunizationRecommendation, RequestDetails requestDetails) {
        return updateImmunizationRecommendation(immunizationRecommendation,requestDetails, null) ;
    }

    public MethodOutcome updateImmunizationRecommendation(@ResourceParam ImmunizationRecommendation immunizationRecommendation, RequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        immunizationRecommendationsStore.putIfAbsent(facility.getId(), new HashMap<>(10));
        String dbPatientID = resourceIdentificationService.getPatientLocalId(immunizationRecommendation.getPatient(), immunizationRegistry, facility);
        immunizationRecommendationsStore.get(facility.getId()).putIfAbsent(dbPatientID, new HashSet<>(10));
        immunizationRecommendationsStore.get(facility.getId()).get(dbPatientID).add(immunizationRecommendation);
        return new MethodOutcome();
    }


}
