package org.immregistries.ehr.fhir.ServerR5;

import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r5.model.Group;
import org.hl7.fhir.r5.model.ResourceType;
import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.fhir.EhrFhirProvider;
import org.immregistries.ehr.fhir.annotations.OnR5Condition;
import org.immregistries.ehr.logic.mapping.GroupMapperR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;

import static org.immregistries.ehr.api.AuditRevisionListener.*;

@Controller
@Conditional(OnR5Condition.class)
public class GroupProviderR5 implements IResourceProvider, EhrFhirProvider<Group> {
    private static final Logger logger = LoggerFactory.getLogger(GroupProviderR5.class);
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;
    @Override
    public Class<Group> getResourceType() {
        return Group.class;
    }
    public ResourceType getResourceName() {
        return ResourceType.Group;
    }
    @Resource
    Map<Integer, Map<Integer, Map<String, Group>>> groupsStore;

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    EhrGroupRepository ehrGroupRepository;
    @Autowired
    GroupMapperR5 groupMapperR5;


    /**
     * Currently unusable as is, as request
     * @param group
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome update(@ResourceParam Group group, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (int) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return update(group,requestDetails, immunizationRegistry) ;
    }

    public MethodOutcome update(@ResourceParam Group group, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(requestDetails.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        return update(group,facility,immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Group group, Facility facility, ImmunizationRegistry immunizationRegistry) {

        groupsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        groupsStore.get(facility.getId())
                .putIfAbsent(immunizationRegistry.getId(), new HashMap<>(5));
        groupsStore.get(facility.getId())
                .get(immunizationRegistry.getId())
                .put(group.getIdElement().getIdPart(), group);
        Optional<EhrGroup> old = ehrGroupRepository.findByFacilityIdAndImmunizationRegistryIdAndName(facility.getId(),immunizationRegistry.getId(), group.getName());
        if (old.isEmpty()) {
            return create(group,facility,immunizationRegistry);
        } else {
            EhrGroup ehrGroup = groupMapperR5.toEhrGroup(group,facility,immunizationRegistry);
            ehrGroup.setId(old.get().getId());
            ehrGroupRepository.save(ehrGroup);
            return new MethodOutcome().setResource(group);
        }
    }

    /**
     * Currently unusable as is, as request
     * @param group
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome create(@ResourceParam Group group, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (int) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return create(group,requestDetails, immunizationRegistry) ;
    }

    public MethodOutcome create(@ResourceParam Group group, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(requestDetails.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        return create(group,facility,immunizationRegistry);
    }

    public MethodOutcome create(@ResourceParam Group group, Facility facility, ImmunizationRegistry immunizationRegistry) {
        groupsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        groupsStore.get(facility.getId())
                .putIfAbsent(immunizationRegistry.getId(), new HashMap<>(5));
        groupsStore.get(facility.getId())
                .get(immunizationRegistry.getId())
                .put(group.getIdElement().getIdPart(),group);
        EhrGroup ehrGroup = groupMapperR5.toEhrGroup(group,facility,immunizationRegistry);
        ehrGroupRepository.save(ehrGroup);
        return new MethodOutcome().setResource(group);
    }

}
