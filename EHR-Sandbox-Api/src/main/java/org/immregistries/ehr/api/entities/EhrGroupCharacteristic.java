package org.immregistries.ehr.api.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "group_characteristic")
@IdClass(EhrGroupCharacteristicId.class)
public class EhrGroupCharacteristic implements Serializable {

    @Id
    @Column(name = "group_id")
    private String groupId;

    @Id
    @Column(name = "code_value")
    private String codeValue;

    @Id
    @Column(name = "code_system")
    private String codeSystem;

    @Column(name = "value")
    private String value;

    @Column(name = "exclude")
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }
}