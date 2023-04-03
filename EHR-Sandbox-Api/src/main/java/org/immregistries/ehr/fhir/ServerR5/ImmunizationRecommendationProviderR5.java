package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.fhir.EhrFhirProvider;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.immregistries.ehr.api.AuditRevisionListener.IMMUNIZATION_REGISTRY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;

@Controller
@Conditional(OnR5Condition.class)
public class ImmunizationRecommendationProviderR5 implements IResourceProvider, EhrFhirProvider<ImmunizationRecommendation> {
    private static final Logger logger = LoggerFactory.getLogger(ImmunizationRecommendationProviderR5.class);
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;

    public Class<ImmunizationRecommendation> getResourceType() {
        return ImmunizationRecommendation.class;
    }

    @Resource
    Map<Integer, Set<ImmunizationRecommendation>> immunizationRecommendationsStore;
    @Autowired
    private ResourceIdentificationService resourceIdentificationService;
    @Autowired
    private FacilityRepository facilityRepository;

    /**
     * Currently unusable as is, as request
     * @param immunizationRecommendation
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome update(@ResourceParam ImmunizationRecommendation immunizationRecommendation, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (int) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return update(immunizationRecommendation,requestDetails, immunizationRegistry) ;
    }

    public MethodOutcome update(@ResourceParam ImmunizationRecommendation immunizationRecommendation, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));

        String dbPatientID = resourceIdentificationService.getPatientLocalId(immunizationRecommendation.getPatient(), immunizationRegistry, facility);
        immunizationRecommendation.setPatient(new Reference(dbPatientID));

        immunizationRecommendationsStore.putIfAbsent(facility.getId(), new HashSet<>(10));
        immunizationRecommendationsStore.get(facility.getId()).add(immunizationRecommendation); // TODO add
        return new MethodOutcome();
    }


    public MethodOutcome deleteConditional(IdType theId, String theConditionalUrl, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(Integer.parseInt(requestDetails.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));

//        String dbPatientID = resourceIdentificationService.getPatientLocalId(immunizationRecommendation.getPatient(), immunizationRegistry, facility);
//        immunizationRecommendation.setPatient(new Reference(dbPatientID));
        Predicate<ImmunizationRecommendation> predicate;
        if (theId != null && !StringUtils.isBlank(theId.getIdPart())) {
            predicate = immunizationRecommendation -> immunizationRecommendation.getId().equals(theId.getIdPart());
        } else  {
            UrlType urlType = new UrlType(theConditionalUrl);
            String[] paramIdentifier = Stream.of(urlType.getValue().split("\\?")[1].split("&"))
                    .map(kv -> kv.split("="))
                    .filter(kv -> "identifier".equalsIgnoreCase(kv[0]))
                    .map(kv -> kv[1])
                    .findFirst()
                    .orElse("|").split("\\|",2);
            Predicate<Identifier> identifierPredicate;
            if (paramIdentifier.length > 1) {
                identifierPredicate = identifier -> identifier.getSystem().equals(paramIdentifier[0]) && identifier.getValue().equals(paramIdentifier[1]);
            } else {
                identifierPredicate = identifier ->  identifier.getValue().equals(paramIdentifier[0]);
            }
            predicate = immunizationRecommendation -> immunizationRecommendation.getIdentifier().stream().anyMatch(identifierPredicate);
        }
        immunizationRecommendationsStore.getOrDefault(facility.getId(), new HashSet<>(0))
                .removeIf(predicate);
//        immunizationRecommendationsStore.get(facility.getId()).get(theId)
        return new MethodOutcome();
    }
}
