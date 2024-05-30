package org.immregistries.ehr.api.entities.embedabbles;

import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable()
public class EhrPhoneNumber {
    @Size(max = 300)
    private String number;
    @Size(max = 300)
    private String type;

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
}
