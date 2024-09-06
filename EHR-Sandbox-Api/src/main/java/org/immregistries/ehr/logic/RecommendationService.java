package org.immregistries.ehr.logic;

import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

    Map<Integer, Map<String, Map<String, IDomainResource>>> immunizationRecommendationsStore;

    RecommendationService() {
        immunizationRecommendationsStore = new HashMap<>(20);
    }

    public org.hl7.fhir.r5.model.ImmunizationRecommendation saveInStore(org.hl7.fhir.r5.model.ImmunizationRecommendation immunizationRecommendation, Facility facility, String patientId, ImmunizationRegistry immunizationRegistry) {
        immunizationRecommendation.setPatient(new org.hl7.fhir.r5.model.Reference(patientId));
        immunizationRecommendationsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        immunizationRecommendationsStore.get(facility.getId()).putIfAbsent(patientId, new HashMap<>(1));
        immunizationRecommendationsStore.get(facility.getId()).get(patientId).put(immunizationRegistry.getId(), immunizationRecommendation);
        return immunizationRecommendation;
    }

    public org.hl7.fhir.r4.model.ImmunizationRecommendation saveInStore(org.hl7.fhir.r4.model.ImmunizationRecommendation immunizationRecommendation, Facility facility, String patientId, ImmunizationRegistry immunizationRegistry) {
        immunizationRecommendation.setPatient(new org.hl7.fhir.r4.model.Reference(patientId));
        immunizationRecommendationsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        immunizationRecommendationsStore.get(facility.getId()).putIfAbsent(patientId, new HashMap<>(1));
        immunizationRecommendationsStore.get(facility.getId()).get(patientId).put(immunizationRegistry.getId(), immunizationRecommendation);
        return immunizationRecommendation;
    }

    public IDomainResource saveInStore(IDomainResource iDomainResource, Facility facility, String patientId, ImmunizationRegistry immunizationRegistry) {
//        immunizationRecommendation.setPatient(new org.hl7.fhir.r4.model.Reference(patientId)); // TODO
        immunizationRecommendationsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        immunizationRecommendationsStore.get(facility.getId()).putIfAbsent(patientId, new HashMap<>(1));
        immunizationRecommendationsStore.get(facility.getId()).get(patientId).put(immunizationRegistry.getId(), iDomainResource);
        return iDomainResource;
    }

    public Map<String, IDomainResource> getPatientMap(String facilityId, String patientId) {
        return immunizationRecommendationsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(patientId, new HashMap<>(0));
    }

//    public Map<String, org.hl7.fhir.r4.model.ImmunizationRecommendation> getPatientMap(String facilityId, String patientId) {
//        return immunizationRecommendationsStoreR4
//                .getOrDefault(facilityId, new HashMap<>(0))
//                .getOrDefault(patientId, new HashMap<>(0));
//    }
}
