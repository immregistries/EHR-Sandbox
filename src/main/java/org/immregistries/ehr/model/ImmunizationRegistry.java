package org.immregistries.ehr.model;
import java.io.Serializable;

public class ImmunizationRegistry implements Serializable {

    private static final long serialVersionUID = 1L;

    private int immunizationRegistryId = 0;
    private Tester tester = null;
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

    public Tester getTester() {
        return tester;
    }

    public void setTester(Tester tester) {
        this.tester = tester;
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