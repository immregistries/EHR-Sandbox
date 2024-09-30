package org.immregistries.ehr.api.entities;

import jakarta.persistence.*;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.immregistries.ehr.api.AuditRevisionListener;

/**
 * Replaces Envers default Revinfo entity to add User information and more fields
 */
@Entity
@Table(name = "revinfo")
@AttributeOverrides({
        @AttributeOverride(name = "timestamp", column = @Column(name = "revtstmp")),
        @AttributeOverride(name = "id", column = @Column(name = "rev"))
})
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevisionEntity extends DefaultRevisionEntity {
    @Column(name = "user")
    private Integer user;

    @Column(name = "immunization_registry_id")
    private Integer immunizationRegistryId;

    @Column(name = "subscription_id")
    private String subscriptionId;

    @Column(name = "copied_entity_id")
    private String copiedEntityId;

    @Column(name = "copied_facility_id")
    private String copiedFacilityId;

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getImmunizationRegistryId() {
        return immunizationRegistryId;
    }

    public void setImmunizationRegistryId(Integer immunizationRegistry) {
        this.immunizationRegistryId = immunizationRegistry;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getCopiedEntityId() {
        return copiedEntityId;
    }

    public void setCopiedEntityId(String copiedEntityId) {
        this.copiedEntityId = copiedEntityId;
    }

    public String getCopiedFacilityId() {
        return copiedFacilityId;
    }

    public void setCopiedFacilityId(String copiedFacilityId) {
        this.copiedFacilityId = copiedFacilityId;
    }
}
