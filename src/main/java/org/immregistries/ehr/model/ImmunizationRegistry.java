package org.immregistries.ehr.model;
import java.io.Serializable;

public class ImmunizationRegistry implements Serializable {

    private static final long serialVersionUID = 1L;

    private int immunizationRegistryId = 0;
    private User user = null;
    private String iisHL7Url = "";
    private String iisFHIRUrl = "";
    private String iisUsername = "";
    private String iisFacilityId = "";
    private String iisPassword = "";

    public int getImmunizationRegistryId() {
        return immunizationRegistryId;
    }

    public void setImmunizationRegistryId(int immunizationRegistryId) {
        this.immunizationRegistryId = immunizationRegistryId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIisHL7Url() {
        return iisHL7Url;
    }

    public void setIisHL7Url(String iisHL7Url) {
        this.iisHL7Url = iisHL7Url;
    }
    
    public String getIisFHIRUrl() {
      return iisFHIRUrl;
    }

    public void setIisFHIRUrl(String iisFHIRUrl) {
      this.iisFHIRUrl = iisFHIRUrl;
    }
  
    public String getIisUsername() {
        return iisUsername;
    }

    public void setIisUsername(String iisUsername) {
        this.iisUsername = iisUsername;
    }

    public String getIisFacilityId() {
        return iisFacilityId;
    }

    public void setIisFacilityId(String iisFacilityId) {
        this.iisFacilityId = iisFacilityId;
    }

    public String getIisPassword() {
        return iisPassword;
    }

    public void setIisPassword(String iisPassword) {
        this.iisPassword = iisPassword;
    }
}