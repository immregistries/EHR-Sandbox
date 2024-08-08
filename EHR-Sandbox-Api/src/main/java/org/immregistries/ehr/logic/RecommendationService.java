package org.immregistries.ehr.logic;

import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.hl7.fhir.r5.model.Reference;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

    Map<Integer, Map<String, Map<Integer, ImmunizationRecommendation>>> immunizationRecommendationsStore;

    RecommendationService() {
        immunizationRecommendationsStore = new HashMap<>(20);
    }

    public ImmunizationRecommendation saveInStore(ImmunizationRecommendation immunizationRecommendation, Facility facility, String patientId, ImmunizationRegistry immunizationRegistry) {
        immunizationRecommendation.setPatient(new Reference(patientId));
        immunizationRecommendationsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        immunizationRecommendationsStore.get(facility.getId()).putIfAbsent(patientId, new HashMap<>(1));
        immunizationRecommendationsStore.get(facility.getId()).get(patientId).put(immunizationRegistry.getId(), immunizationRecommendation);
        return immunizationRecommendation;
    }

    public Map<Integer, ImmunizationRecommendation> getPatientMap(String facilityId, String patientId) {
        return immunizationRecommendationsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(patientId, new HashMap<>(0));
    }
}
