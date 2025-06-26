package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Information used by client for both FHIR and Hl7V2 Messaging
 */
@Entity
@Table(name = "immunization_registry", indexes = {
        @Index(name = "user_id", columnList = "user_id")
})
public class ImmunizationRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "immunization_registry_id", nullable = false)
    private Integer id;

    /**
     * EHR user owning the logging information
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * Name of IIS, used essentially in UI, unique in user's Scope
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name = "";

    /**
     * HL7v2 SOAP endpoint URL
     */
    @Column(name = "iis_HL7_url", nullable = false, length = 250)
    private String iisHl7Url = "";

    /**
     * FHIR Restful endpoint URL
     */
    @Column(name = "iis_FHIR_url", nullable = false, length = 250)
    private String iisFhirUrl = "";

    /**
     * Optional, for experimental FHIR Messaging endpoint
     */
    @Column(name = "iis_FHIR_messaging_url", nullable = false, length = 250)
    private String iisFhirMessagingUrl = "";

    /**
     * IIS side username
     */
    @Column(name = "iis_username", nullable = false, length = 250)
    private String iisUsername = "";

    /**
     * IIS side tenant Id, used to complete URL in FHIR and as FacilityID in SOAP,
     * TODO improve support of diverse configuration
     */
    @Column(name = "iis_facility_id", nullable = false, length = 250)
    private String iisFacilityId = "";

    /**
     * IIS side password
     */
    @Column(name = "iis_password", nullable = false, length = 600)
    private String iisPassword = "";

    /**
     * Used for quick selection in UI
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * Optional description of the IIS endpoint
     */
    @Column(name = "description", nullable = false, length = 600)
    private String description = "";

    /**
     * Optional, MSH-6 segment for Receiving Facility, only used in UI
     */
    @Column(name = "receivingFacility", nullable = false, length = 600)
    private String receivingFacility = "";

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getIisFhirMessagingUrl() {
        return iisFhirMessagingUrl;
    }

    public void setIisFhirMessagingUrl(String iisFhirMessagingUrl) {
        this.iisFhirMessagingUrl = iisFhirMessagingUrl;
    }

    public String getReceivingFacility() {
        return receivingFacility;
    }

    public void setReceivingFacility(String receivingFacility) {
        this.receivingFacility = receivingFacility;
    }
}