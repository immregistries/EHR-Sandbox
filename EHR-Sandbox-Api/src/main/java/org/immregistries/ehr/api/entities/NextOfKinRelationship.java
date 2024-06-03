package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.immregistries.ehr.api.entities.embedabbles.NextOfKinRelationshipPK;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "next_of_kin_patient_association")
//@IdClass(NextOfKinRelationshipPK.class)
public class NextOfKinRelationship implements Serializable {

    @EmbeddedId
    NextOfKinRelationshipPK nextOfKinRelationshipPK = new NextOfKinRelationshipPK();

    //    @Id
    @ManyToOne
    @MapsId("patientId")
    @JoinColumn(name = "patient_id")
    @JsonBackReference("patient_next_of_kin_relationship")
    private EhrPatient ehrPatient;

    //    @Id
    @ManyToOne(cascade = {CascadeType.ALL, CascadeType.MERGE})
    @MapsId("nextOfKinId")
    @JoinColumn(name = "next_of_kin_id")
    private NextOfKin nextOfKin;

    private String relationshipKind;

    public NextOfKinRelationship() {
    }

    public NextOfKinRelationship(EhrPatient ehrPatient, NextOfKin nextOfKin) {
        this.ehrPatient = ehrPatient;
        this.nextOfKin = nextOfKin;
    }

    public EhrPatient getEhrPatient() {
        return ehrPatient;
    }

    public void setEhrPatient(EhrPatient ehrPatient) {
        this.ehrPatient = ehrPatient;
    }

    public NextOfKin getNextOfKin() {
        return nextOfKin;
    }

    public void setNextOfKin(NextOfKin nextOfKin) {
        this.nextOfKin = nextOfKin;
    }

    public String getRelationshipKind() {
        return relationshipKind;
    }

    public void setRelationshipKind(String relationshipKind) {
        this.relationshipKind = relationshipKind;
    }

    public NextOfKinRelationshipPK getNextOfKinRelationshipPK() {
        return nextOfKinRelationshipPK;
    }

    public void setNextOfKinRelationshipPK(NextOfKinRelationshipPK nextOfKinRelationshipPK) {
        this.nextOfKinRelationshipPK = nextOfKinRelationshipPK;
    }
}
