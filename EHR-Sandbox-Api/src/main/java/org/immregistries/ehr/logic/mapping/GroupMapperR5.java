package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r5.model.Group;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class GroupMapperR5 {

    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    @Autowired
    private EhrPatientRepository ehrPatientRepository;

    public EhrGroup toEhrGroup(Group group){
        EhrGroup ehrGroup = new EhrGroup();
        ehrGroup.setName(group.getName());
        ehrGroup.setCode(group.getCode().getText());
        ehrGroup.setDescription(group.getDescription());
        Set<EhrGroupCharacteristic> ehrGroupCharacteristicSet = new HashSet<>(group.getCharacteristic().size());
        ehrGroup.setEhrGroupCharacteristics(ehrGroupCharacteristicSet);
        for (Group.GroupCharacteristicComponent characteristicComponent: group.getCharacteristic()) {
            EhrGroupCharacteristic ehrGroupCharacteristic  = new EhrGroupCharacteristic();
            ehrGroupCharacteristic.setValue(characteristicComponent.getValue().toString());
            if (characteristicComponent.getCode().hasCoding()) {
                ehrGroupCharacteristic.setCodeSystem(characteristicComponent.getCode().getCodingFirstRep().getSystem());
                ehrGroupCharacteristic.setCodeValue(characteristicComponent.getCode().getCodingFirstRep().getCode());
            }
            ehrGroupCharacteristic.setExclude(characteristicComponent.getExclude());
            if (characteristicComponent.getPeriod() != null) {
                ehrGroupCharacteristic.setPeriodStart(characteristicComponent.getPeriod().getStart());
                ehrGroupCharacteristic.setPeriodEnd(characteristicComponent.getPeriod().getEnd());
            }
            ehrGroupCharacteristicSet.add(ehrGroupCharacteristic);
        }

        return ehrGroup;
    }

    public EhrGroup toEhrGroup(Group group, Facility facility, ImmunizationRegistry immunizationRegistry) {
        EhrGroup ehrGroup = toEhrGroup(group);
        ehrGroup.setFacility(facility);
        ehrGroup.setImmunizationRegistry(immunizationRegistry);
        Set<String> patientIds = new HashSet<>(group.getMember().size());
        for (Group.GroupMemberComponent g: group.getMember()) {
            String id = resourceIdentificationService.getPatientLocalId(g.getEntity(),immunizationRegistry,facility);
            patientIds.add(id);
        }
        if (!patientIds.isEmpty()) {
            ehrGroup.setPatientList(new HashSet<EhrPatient>((Collection<EhrPatient>) ehrPatientRepository.findAllById(patientIds)));
        }
        return  ehrGroup;
    }

}
