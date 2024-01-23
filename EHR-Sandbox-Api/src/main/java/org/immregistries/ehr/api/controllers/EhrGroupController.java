package org.immregistries.ehr.api.controllers;

import org.hl7.fhir.r5.model.Group;
import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.repositories.EhrGroupRepository;
import org.immregistries.ehr.fhir.Client.CustomClientFactory;
import org.immregistries.ehr.fhir.ServerR5.GroupProviderR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping({"/tenants/{tenantId}/facilities/{facilityId}/groups"})
public class EhrGroupController {
    Logger logger = LoggerFactory.getLogger(RemoteGroupController.class);
    @Autowired
    CustomClientFactory customClientFactory;
    @Autowired
    Map<Integer, Map<Integer, Map<String, Group>>> remoteGroupsStore;
    @Autowired
    private ImmunizationRegistryController immunizationRegistryController;
    @Autowired
    private EhrGroupRepository ehrGroupRepository;
    @Autowired
    private GroupProviderR5 groupProviderR5;
    @Autowired
    private FhirClientController fhirClientController;

    /**
     * hollow method for url mapping
     * query is actually executed in authorization filter
     * @param ehrGroup
     * @return
     */
    @GetMapping("/{groupId}")
    public EhrGroup get(@RequestAttribute EhrGroup ehrGroup) {
        return ehrGroup;
    }

    @PostMapping()
    public ResponseEntity<EhrGroup> post(@RequestAttribute Facility facility,
                                         @RequestBody EhrGroup ehrGroup) {
        if (ehrGroupRepository.existsByFacilityIdAndName(facility.getId(), ehrGroup.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Name already used for this facility");
        } else {
            EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
            return new ResponseEntity<>(newEntity, HttpStatus.CREATED);
        }
    }


    @PutMapping("{groupId}")
    public ResponseEntity<EhrGroup> put(@RequestAttribute Facility facility, @RequestBody EhrGroup ehrGroup) {
        Optional<EhrGroup> oldEntity = ehrGroupRepository.findByFacilityIdAndId(facility.getId(), ehrGroup.getId());
        if (oldEntity.isEmpty()) {
            return post(facility, ehrGroup);
        } else {
            Optional<EhrGroup> groupUsingNewName = ehrGroupRepository.findByFacilityIdAndId(facility.getId(), ehrGroup.getId());
            if (groupUsingNewName.isPresent() && !groupUsingNewName.get().getId().equals(oldEntity.get().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_ACCEPTABLE, "Name already used by another group");
            } else {
                EhrGroup newEntity = ehrGroupRepository.save(ehrGroup);
                return new ResponseEntity<>(newEntity, HttpStatus.ACCEPTED);
            }
        }


    }


}
