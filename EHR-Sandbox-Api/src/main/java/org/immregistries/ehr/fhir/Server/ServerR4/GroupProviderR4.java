package org.immregistries.ehr.fhir.Server.ServerR4;

import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResourceType;
import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.api.repositories.FacilityRepository;
import org.immregistries.ehr.api.repositories.ImmunizationRegistryRepository;
import org.immregistries.ehr.fhir.Server.IGroupProvider;
import org.immregistries.ehr.logic.mapping.GroupMapperR4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.immregistries.ehr.api.AuditRevisionListener.IMMUNIZATION_REGISTRY_ID;
import static org.immregistries.ehr.api.AuditRevisionListener.USER_ID;

@Controller
public class GroupProviderR4 implements IGroupProvider<Group>, EhrFhirProviderR4<Group> {
    private static final Logger logger = LoggerFactory.getLogger(GroupProviderR4.class);
    @Autowired
    private ImmunizationRegistryRepository immunizationRegistryRepository;

    @Override
    public Class<Group> getResourceType() {
        return Group.class;
    }

    public ResourceType getResourceName() {
        return ResourceType.Group;
    }

    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    EhrGroupRepository ehrGroupRepository;
    @Autowired
    GroupMapperR4 groupMapperR4;


    /**
     * Currently unusable as is, as request
     *
     * @param group
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome update(@ResourceParam Group group, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (String) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return update(group, requestDetails, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Group group, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(requestDetails.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        return update(group, facility, immunizationRegistry);
    }

    public MethodOutcome update(@ResourceParam Group group, Facility facility, ImmunizationRegistry immunizationRegistry) {
        Optional<EhrGroup> old = ehrGroupRepository.findByFacilityIdAndImmunizationRegistryIdAndName(facility.getId(), immunizationRegistry.getId(), group.getName());
        if (old.isEmpty()) {
            return create(group, facility, immunizationRegistry);
        } else {
            EhrGroup ehrGroup = groupMapperR4.toEhrGroup(group, facility, immunizationRegistry);
            ehrGroup.setId(old.get().getId());
            ehrGroupRepository.save(ehrGroup);
            return new MethodOutcome().setResource(groupMapperR4.toFhir(ehrGroup));
        }
    }

    /**
     * Currently unusable as is, as request
     *
     * @param group
     * @param requestDetails
     * @return
     */
    @Update
    public MethodOutcome create(@ResourceParam Group group, ServletRequestDetails requestDetails) {
        ImmunizationRegistry immunizationRegistry = immunizationRegistryRepository.findByIdAndUserId(
                (String) requestDetails.getServletRequest().getAttribute(IMMUNIZATION_REGISTRY_ID),
                (Integer) requestDetails.getServletRequest().getAttribute(USER_ID)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "unknown source"));
        return create(group, requestDetails, immunizationRegistry);
    }

    public MethodOutcome create(@ResourceParam Group group, ServletRequestDetails requestDetails, ImmunizationRegistry immunizationRegistry) {
        Facility facility = facilityRepository.findById(requestDetails.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid facility id"));
        return create(group, facility, immunizationRegistry);
    }

    public MethodOutcome create(@ResourceParam Group group, Facility facility, ImmunizationRegistry immunizationRegistry) {
        EhrGroup ehrGroup = groupMapperR4.toEhrGroup(group, facility, immunizationRegistry);
        ehrGroupRepository.save(ehrGroup);
        return new MethodOutcome().setResource(group);
    }

    public Group getRemoteGroup(IGenericClient client, EhrGroup ehrGroup) {
        EhrIdentifier ehrIdentifier = ehrGroup.getIdentifiers().stream().findAny().orElseThrow(
                () -> new RuntimeException("Group Identifier required for R4 testing")
        );
        Bundle bundle = client.search().forResource("Group")
                .where(Group.IDENTIFIER.exactly().identifier(ehrIdentifier.getValue())) //TODO type and system
//                .where(Group.MANAGING_ENTITY.hasId("Organization/"+facilityId)) // TODO set criteria
                .returnBundle(Bundle.class)
                .execute();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.hasResource() && entry.getResource() instanceof Group
//                    && ((Group) entry.getResource()).getManagingEntity().getIdentifier().getValue().equals(String.valueOf(facilityId))
            ) {
                return (Group) entry.getResource();
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not remotely recorded");
    }

}
