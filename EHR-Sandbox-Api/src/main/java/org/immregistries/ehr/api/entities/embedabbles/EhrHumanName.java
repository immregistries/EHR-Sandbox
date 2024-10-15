package org.immregistries.ehr.api.entities.embedabbles;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EhrHumanName {

    @Column(name = "name_prefix", length = 250)
    private String namePrefix = "";
    @Column(name = "name_last", length = 250)
    private String nameLast = "";
    @Column(name = "name_first", length = 250)
    private String nameFirst = "";
    @Column(name = "name_middle", length = 250)
    private String nameMiddle = "";
    @Column(name = "name_suffix", length = 250)
    private String nameSuffix = "";
    @Column(name = "name_type", length = 250)
    private String nameType = "";

    public String getNameLast() {
        return nameLast;
    }

    public void setNameLast(String nameLast) {
        this.nameLast = nameLast;
    }

    public String getNameFirst() {
        return nameFirst;
    }

    public void setNameFirst(String nameFirst) {
        this.nameFirst = nameFirst;
    }

    public String getNameMiddle() {
        return nameMiddle;
    }

    public void setNameMiddle(String nameMiddle) {
        this.nameMiddle = nameMiddle;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNameType() {
        return nameType;
    }

    public void setNameType(String nameType) {
        this.nameType = nameType;
    }
}
