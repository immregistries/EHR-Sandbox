package org.immregistries.ehr.api.entities;

import org.immregistries.ehr.api.entities.embedabbles.ImmunizationIdentifierKey;

import javax.persistence.*;

@Entity
@Table(name = "immunization_identifier", indexes = {
        @Index(name = "vaccination_event_id", columnList = "vaccination_event_id"),
        @Index(name = "immunization_registry_id", columnList = "immunization_registry_id")
})
@IdClass(ImmunizationIdentifierKey.class)
public class ImmunizationIdentifier {
    @Id
    @Column(name = "vaccination_event_id", nullable = false)
    private String vaccinationEventId;

    @Id
    @Column(name = "immunization_registry_id", nullable = true)
    private Integer immunizationRegistryId;

//    @Id
//    @Column(name = "system", nullable = false)
//    private String system;

    @Column(name = "identifier", nullable = false)
    private String identifier;


    public ImmunizationIdentifier() {
    }

    public ImmunizationIdentifier(String vaccinationEventId, Integer immunizationRegistryId, String identifier) {
        this.vaccinationEventId = vaccinationEventId;
        this.immunizationRegistryId = immunizationRegistryId;
        this.identifier = identifier;
    }

    public String getVaccinationEventId() {
        return vaccinationEventId;
    }

    public void setVaccinationEventId(String vaccinationEventId) {
        this.vaccinationEventId = vaccinationEventId;
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

