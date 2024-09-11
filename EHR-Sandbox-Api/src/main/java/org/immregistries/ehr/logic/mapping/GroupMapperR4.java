package org.immregistries.ehr.logic.mapping;

import org.hl7.fhir.r4.model.*;
import org.immregistries.ehr.api.entities.EhrGroup;
import org.immregistries.ehr.api.entities.EhrPatient;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.immregistries.ehr.api.entities.embedabbles.EhrGroupCharacteristic;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.EhrPatientRepository;
import org.immregistries.ehr.logic.ResourceIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service("groupMapperR4")
public class GroupMapperR4 implements IGroupMapper<Group> {

    @Autowired
    private ResourceIdentificationService resourceIdentificationService;

    @Autowired
    private EhrPatientRepository ehrPatientRepository;

    public EhrGroup toEhrGroup(Group group) {
        EhrGroup ehrGroup = new EhrGroup();
        ehrGroup.setName(group.getName());
        ehrGroup.setCode(group.getCode().getText());
//        ehrGroup.setDescription(group.get.getDescription());
        Set<EhrGroupCharacteristic> ehrGroupCharacteristicSet = new HashSet<>(group.getCharacteristic().size());
        ehrGroup.setEhrGroupCharacteristics(ehrGroupCharacteristicSet);
        for (Group.GroupCharacteristicComponent characteristicComponent : group.getCharacteristic()) {
            EhrGroupCharacteristic ehrGroupCharacteristic = new EhrGroupCharacteristic();
            ehrGroupCharacteristic.setValue(((CodeableConcept) characteristicComponent.getValue()).getCodingFirstRep().getCode());
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
        for (Group.GroupMemberComponent g : group.getMember()) {
            String id = resourceIdentificationService.getLocalPatientId(g.getEntity(), immunizationRegistry, facility);
            patientIds.add(id);
        }
        if (!patientIds.isEmpty()) {
            ehrGroup.setPatientList(new HashSet<EhrPatient>((Collection<EhrPatient>) ehrPatientRepository.findAllById(patientIds)));
        }
        for (Identifier identifier : group.getIdentifier()) {
            ehrGroup.getIdentifiers().add(new EhrIdentifier(identifier));
        }
        return ehrGroup;
    }

    public Group toFhir(EhrGroup ehrGroup) {
        Group group = new Group();
        group.setType(Group.GroupType.PERSON);
        group.setActive(true);

        group.setName(ehrGroup.getName());
        group.setCode(new CodeableConcept().setText(ehrGroup.getCode()));

        for (EhrIdentifier ehrIdentifier : ehrGroup.getIdentifiers()
        ) {
            group.addIdentifier(ehrIdentifier.toR4());
        }


//        group.setDescription(ehrGroup.getDescription());
        group.setManagingEntity(new Reference().setType("Organization").setIdentifier(IOrganizationMapper.facilityIdToEhrIdentifier(ehrGroup.getFacility()).toR4()));
//        Hibernate.initialize(ehrGroup.getEhrGroupCharacteristics());
        if (ehrGroup.getEhrGroupCharacteristics() != null) {
            for (EhrGroupCharacteristic ehrGroupCharacteristic : ehrGroup.getEhrGroupCharacteristics()) {
                Group.GroupCharacteristicComponent groupCharacteristicComponent = group.addCharacteristic();
                groupCharacteristicComponent.setValue(new CodeableConcept(new Coding(null, ehrGroupCharacteristic.getValue(), null)).setText(ehrGroupCharacteristic.getValue()))
                        .setCode(new CodeableConcept(new Coding().setSystem(ehrGroupCharacteristic.getCodeSystem()).setCode(ehrGroupCharacteristic.getCodeValue())))
                        .setPeriod(new Period().setEnd(ehrGroupCharacteristic.getPeriodEnd()).setStart(ehrGroupCharacteristic.getPeriodStart()));
                if (Objects.nonNull(ehrGroupCharacteristic.getExclude())) {
                    groupCharacteristicComponent.setExclude(ehrGroupCharacteristic.getExclude());
                }
            }
        }
        if (ehrGroup.getPatientList() != null) {
            for (EhrPatient patient : ehrGroup.getPatientList()) {
                EhrIdentifier ehrIdentifier = patient.getMrnEhrIdentifier();
                group.addMember().setEntity(new Reference()
                        .setIdentifier(ehrIdentifier.toR4()));
            }
        }

        return group;
    }

}
