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
}
