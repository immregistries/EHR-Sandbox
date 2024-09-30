package org.immregistries.ehr.api.entities.embedabbles;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

@Embeddable()
public class EhrPhoneNumber {
    @Size(max = 300)
    private String number;
    @Size(max = 300)
    private String type;
    @Size(max = 300)
    private String use;

    public EhrPhoneNumber() {
    }

    public EhrPhoneNumber(String number, String type) {
        this.number = number;
        this.type = type;
    }

    public EhrPhoneNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }
}
