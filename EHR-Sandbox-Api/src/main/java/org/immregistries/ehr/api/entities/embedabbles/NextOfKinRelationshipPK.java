package org.immregistries.ehr.api.entities.embedabbles;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class NextOfKinRelationshipPK implements Serializable {

    @Column(name = "patient_id")
    Integer patientId;

    @Column(name = "next_of_kin_id")
    String nextOfKinId;

    public NextOfKinRelationshipPK() {
    }

    public NextOfKinRelationshipPK(Integer patientId, String nextOfKinId) {
        this.patientId = patientId;
        this.nextOfKinId = nextOfKinId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getNextOfKinId() {
        return nextOfKinId;
    }

    public void setNextOfKinId(String nextOfKinId) {
        this.nextOfKinId = nextOfKinId;
    }
}
