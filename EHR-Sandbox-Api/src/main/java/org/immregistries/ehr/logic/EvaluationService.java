package org.immregistries.ehr.logic;

import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.entities.EhrUtils;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EvaluationService {

    Map<Integer, Map<Integer, Map<Integer, IDomainResource>>> immunizationEvaluationsStore;

    EvaluationService() {
        immunizationEvaluationsStore = new HashMap<>(20);
    }

    public org.hl7.fhir.r5.model.ImmunizationEvaluation saveInStore(org.hl7.fhir.r5.model.ImmunizationEvaluation immunizationEvaluation, Facility facility, Integer patientId, ImmunizationRegistry immunizationRegistry) {
        immunizationEvaluation.setPatient(new org.hl7.fhir.r5.model.Reference(EhrUtils.convert(patientId)));
        immunizationEvaluationsStore.putIfAbsent(facility.getId(), new HashMap<>(5));
        immunizationEvaluationsStore.get(facility.getId()).putIfAbsent(patientId, new HashMap<>(1));
        immunizationEvaluationsStore.get(facility.getId()).get(patientId).put(immunizationRegistry.getId(), immunizationEvaluation);
        return immunizationEvaluation;
    }

    public org.hl7.fhir.r4.model.ImmunizationEvaluation saveInStore(org.hl7.fhir.r4.model.ImmunizationEvaluation immunizationEvaluation, Facility facility, Integer patientId, ImmunizationRegistry immunizationRegistry) {
        immunizationEvaluation.setPatient(new org.hl7.fhir.r4.model.Reference(EhrUtils.convert(patientId)));
        immunizationEvaluationsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        immunizationEvaluationsStore.get(facility.getId()).putIfAbsent(patientId, new HashMap<>(1));
        immunizationEvaluationsStore.get(facility.getId()).get(patientId).put(immunizationRegistry.getId(), immunizationEvaluation);
        return immunizationEvaluation;
    }

    public IDomainResource saveInStore(IDomainResource iDomainResource, Integer facilityId, Integer patientId, ImmunizationRegistry immunizationRegistry) {
//        immunizationEvaluation.setPatient(new org.hl7.fhir.r4.model.Reference(patientId)); // TODO
        immunizationEvaluationsStore.putIfAbsent(Integer.valueOf(facilityId), new HashMap<>(5));
        immunizationEvaluationsStore.get(facilityId).putIfAbsent(patientId, new HashMap<>(1));
        immunizationEvaluationsStore.get(facilityId).get(patientId).put(immunizationRegistry.getId(), iDomainResource);
        return iDomainResource;
    }

    public Map<Integer, IDomainResource> getPatientMap(Integer facilityId, Integer patientId) {
        return immunizationEvaluationsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(patientId, new HashMap<>(0));
    }

//    public Map<String, org.hl7.fhir.r4.model.ImmunizationEvaluation> getPatientMap(Integer facilityId, Integer patientId) {
//        return immunizationEvaluationsStoreR4
//                .getOrDefault(facilityId, new HashMap<>(0))
//                .getOrDefault(patientId, new HashMap<>(0));
//    }
}
