package org.immregistries.ehr.api.entities.embedabbles;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class NextOfKinRelationshipPK implements Serializable {

    @Column(name = "patient_id")
    String patientId;

    @Column(name = "next_of_kin_id")
    String nextOfKinId;

    public NextOfKinRelationshipPK() {
    }

    public NextOfKinRelationshipPK(String patientId, String nextOfKinId) {
        this.patientId = patientId;
        this.nextOfKinId = nextOfKinId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getNextOfKinId() {
        return nextOfKinId;
    }

    public void setNextOfKinId(String nextOfKinId) {
        System.out.println("TETTETETTETETET");
        this.nextOfKinId = nextOfKinId;
    }
}
