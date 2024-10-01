package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "immunization_registry", indexes = {
        @Index(name = "user_id", columnList = "user_id")
})
public class ImmunizationRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "immunization_registry_id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name = "";

    @Column(name = "iis_HL7_url", nullable = false, length = 250)
    private String iisHl7Url = "";

    @Column(name = "iis_FHIR_url", nullable = false, length = 250)
    private String iisFhirUrl = "";

    @Column(name = "iis_username", nullable = false, length = 250)
    private String iisUsername = "";

    @Column(name = "iis_facility_id", nullable = false, length = 250)
    private String iisFacilityId = "";

    @Column(name = "iis_password", nullable = false, length = 600)
    private String iisPassword = "";

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "description", nullable = false, length = 600)
    private String description = "";

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    @Column(name = "headers", nullable = false, length = 250)
    private String headers = "";

    public String getIisPassword() {
        return iisPassword;
    }

    public void setIisPassword(String iisPassword) {
        this.iisPassword = iisPassword;
    }

    public String getIisFacilityId() {
        return iisFacilityId;
    }

    public void setIisFacilityId(String iisFacilityId) {
        this.iisFacilityId = iisFacilityId;
    }

    public String getIisUsername() {
        return iisUsername;
    }

    public void setIisUsername(String iisUsername) {
        this.iisUsername = iisUsername;
    }

    public String getIisFhirUrl() {
        return iisFhirUrl;
    }

    public void setIisFhirUrl(String iisFhirUrl) {
        this.iisFhirUrl = iisFhirUrl;
    }

    public String getIisHl7Url() {
        return iisHl7Url;
    }

    public void setIisHl7Url(String iisHl7Url) {
        this.iisHl7Url = iisHl7Url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}