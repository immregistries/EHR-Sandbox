package org.immregistries.ehr.model;
import java.io.Serializable;
import java.util.Date;

public class ImmunizationRegistry implements Serializable {

    private static final long serialVersionUID = 1L;

    private int immunizationRegistryId = 0;
    private Tester tester = null;
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

    public Tester getTester() {
        return tester;
    }

    public void setTester(Tester tester) {
        this.tester = tester;
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