package org.immregistries.ehr.api.entities.embedabbles;

import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable
public class EhrAddress {
    @Size(max = 300)
    private String addressLine1 = "";
    @Size(max = 300)
    private String addressLine2 = "";
    @Size(max = 300)
    private String addressCity = "";
    @Size(max = 300)
    private String addressState = "";
    @Size(max = 300)
    private String addressZip = "";
    @Size(max = 300)
    private String addressCountry = "";
    @Size(max = 300)
    private String addressCountyParish = "";

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressCountyParish() {
        return addressCountyParish;
    }

    public void setAddressCountyParish(String addressCountyParish) {
        this.addressCountyParish = addressCountyParish;
    }
}
