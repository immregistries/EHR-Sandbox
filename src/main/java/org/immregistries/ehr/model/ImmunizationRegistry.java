package org.immregistries.ehr.model;
import java.io.Serializable;
import java.util.Date;

public class ImmunizationRegistry implements Serializable {

    private static final long serialVersionUID = 1L;

    private int immunizationRegistryId = 0;
    private Tester testerId = null;
    private String iisUrl = "";
    private String iisUsername = "";
    private String iisFacilityId = "";
    private String iisPassword = "";

    public int getImmunizationRegistryId() {
        return immunizationRegistryId;
    }

    public void setImmunizationRegistryId(int immunizationRegistryId) {
        this.immunizationRegistryId = immunizationRegistryId;
    }

    public Tester getTesterId() {
        return testerId;
    }

    public void setTesterId(Tester testerId) {
        this.testerId = testerId;
    }

    public String getIisUrl() {
        return iisUrl;
    }

    public void setIisUrl(String iisUrl) {
        this.iisUrl = iisUrl;
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