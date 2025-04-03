package org.immregistries.ehr.logic;

import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregistries.ehr.api.entities.EhrUtils;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.ImmunizationRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

    Map<Integer, Map<Integer, Map<Integer, IDomainResource>>> immunizationRecommendationsStore;

    RecommendationService() {
        immunizationRecommendationsStore = new HashMap<>(20);
    }

    public org.hl7.fhir.r5.model.ImmunizationRecommendation saveInStore(org.hl7.fhir.r5.model.ImmunizationRecommendation immunizationRecommendation, Facility facility, Integer vaccinationId, ImmunizationRegistry immunizationRegistry) {
        immunizationRecommendation.setPatient(new org.hl7.fhir.r5.model.Reference(EhrUtils.convert(vaccinationId)));
        immunizationRecommendationsStore.putIfAbsent(facility.getId(), new HashMap<>(5));
        immunizationRecommendationsStore.get(facility.getId()).putIfAbsent(vaccinationId, new HashMap<>(1));
        immunizationRecommendationsStore.get(facility.getId()).get(vaccinationId).put(immunizationRegistry.getId(), immunizationRecommendation);
        return immunizationRecommendation;
    }

    public org.hl7.fhir.r4.model.ImmunizationRecommendation saveInStore(org.hl7.fhir.r4.model.ImmunizationRecommendation immunizationRecommendation, Facility facility, Integer vaccinationId, ImmunizationRegistry immunizationRegistry) {
        immunizationRecommendation.setPatient(new org.hl7.fhir.r4.model.Reference(EhrUtils.convert(vaccinationId)));
        immunizationRecommendationsStore.putIfAbsent(Integer.valueOf(facility.getId()), new HashMap<>(5));
        immunizationRecommendationsStore.get(facility.getId()).putIfAbsent(vaccinationId, new HashMap<>(1));
        immunizationRecommendationsStore.get(facility.getId()).get(vaccinationId).put(immunizationRegistry.getId(), immunizationRecommendation);
        return immunizationRecommendation;
    }

    public IDomainResource saveInStore(IDomainResource iDomainResource, Integer facilityId, Integer vaccinationId, ImmunizationRegistry immunizationRegistry) {
//        immunizationRecommendation.setPatient(new org.hl7.fhir.r4.model.Reference(vaccinationId)); // TODO
        immunizationRecommendationsStore.putIfAbsent(Integer.valueOf(facilityId), new HashMap<>(5));
        immunizationRecommendationsStore.get(facilityId).putIfAbsent(vaccinationId, new HashMap<>(1));
        immunizationRecommendationsStore.get(facilityId).get(vaccinationId).put(immunizationRegistry.getId(), iDomainResource);
        return iDomainResource;
    }

    public Map<Integer, IDomainResource> getPatientMap(Integer facilityId, Integer vaccinationId) {
        return immunizationRecommendationsStore
                .getOrDefault(facilityId, new HashMap<>(0))
                .getOrDefault(vaccinationId, new HashMap<>(0));
    }

//    public Map<String, org.hl7.fhir.r4.model.ImmunizationRecommendation> getPatientMap(Integer facilityId, Integer vaccinationId) {
//        return immunizationRecommendationsStoreR4
//                .getOrDefault(facilityId, new HashMap<>(0))
//                .getOrDefault(vaccinationId, new HashMap<>(0));
//    }
}
