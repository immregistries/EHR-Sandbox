package org.immregistries.ehr.api.entities;

import jakarta.persistence.*;
import org.immregistries.ehr.api.entities.embedabbles.PatientExternalIdentifierKey;

@Entity
@Table(name = "patient_identifier", indexes = {
        @Index(name = "patient_id", columnList = "patient_id"),
//        @Index(name = "immunization_registry_id", columnList = "immunization_registry_id")
})
@IdClass(PatientExternalIdentifierKey.class)
public class PatientExternalIdentifier {
    @Id
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Id
    @Column(name = "immunization_registry_id", nullable = false)
    private Integer immunizationRegistryId;


    @Column(name = "identifier", nullable = true)
    private String identifier;

    public PatientExternalIdentifier() {
    }

    public PatientExternalIdentifier(Integer patientId, Integer immunizationRegistryId, String identifier) {
        this.patientId = patientId;
        this.immunizationRegistryId = immunizationRegistryId;
        this.identifier = identifier;
    }


    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getImmunizationRegistryId() {
        return immunizationRegistryId;
    }

    public void setImmunizationRegistryId(Integer immunizationRegistryId) {
        this.immunizationRegistryId = immunizationRegistryId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}

