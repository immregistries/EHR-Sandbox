package org.immregistries.ehr.api.entities.embedabbles;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.io.Serializable;
import java.util.Date;

@Embeddable
public class EhrGroupCharacteristic implements Serializable {

    @Column(name = "characteristic_code_value")
    private String codeValue;

    @Column(name = "characteristic_code_system")
    private String codeSystem;

    @Column(name = "characteristic_value")
    private String value;

    @Column(name = "characteristic_exclude")
    private Boolean exclude;

    @Temporal(TemporalType.DATE)
    @Column(name = "period_start")
    private Date periodStart;

    @Temporal(TemporalType.DATE)
    @Column(name = "period_end")
    private Date periodEnd;

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Boolean getExclude() {
        return exclude;
    }

    public void setExclude(Boolean exclude) {
        this.exclude = exclude;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }
}