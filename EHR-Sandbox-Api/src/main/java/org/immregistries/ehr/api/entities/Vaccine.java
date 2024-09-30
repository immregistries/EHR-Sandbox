package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "vaccine")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
@Audited
public class Vaccine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccine_id", nullable = false)
    private String id;

    @Column(name = "created_date", nullable = true)
    private Date createdDate;

    @Column(name = "updated_date", nullable = true)
    private Date updatedDate;

    @Column(name = "administered_date", nullable = true)
    private Date administeredDate;

    @Column(name = "vaccine_cvx_code", nullable = false, length = 250)
    private String vaccineCvxCode = "";

    @Column(name = "vaccine_ndc_code", length = 250)
    private String vaccineNdcCode = "";

    @Column(name = "vaccine_mvx_code", length = 250)
    private String vaccineMvxCode = "";

    @Column(name = "administered_amount", length = 250)
    private String administeredAmount = "";

    @Column(name = "information_source", length = 250)
    private String informationSource = "";

    @Column(name = "lot_number", length = 250)
    private String lotNumber = "";

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "completion_status", length = 250)
    private String completionStatus = "";

    @Column(name = "action_code", length = 250)
    private String actionCode = "";

    @Column(name = "refusal_reason_code", length = 250)
    private String refusalReasonCode = "";

    @Column(name = "body_site", length = 250)
    private String bodySite = "";

    @Column(name = "body_route", length = 250)
    private String bodyRoute = "";

    @Column(name = "funding_source", length = 250)
    private String fundingSource = "";

    @Column(name = "information_statement", length = 250)
    private String informationStatement = "";

    @Column(name = "information_statement_cvx", length = 250)
    private String informationStatementCvx = "";

    @Column(name = "information_statement_presented_date")
    private Date informationStatementPresentedDate;

    @Column(name = "information_statement_published_date")
    private Date informationStatementPublishedDate;

    @Column(name = "financial_status", length = 250)
    private String financialStatus = "";


    @OneToMany(mappedBy = "vaccine")
    private Set<VaccinationEvent> vaccinationEvents = new LinkedHashSet<>();

    public Set<VaccinationEvent> getVaccinationEvents() {
        return vaccinationEvents;
    }

    public void setVaccinationEvents(Set<VaccinationEvent> vaccinationEvents) {
        this.vaccinationEvents = vaccinationEvents;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getBodyRoute() {
        return bodyRoute;
    }

    public void setBodyRoute(String bodyRoute) {
        this.bodyRoute = bodyRoute;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getRefusalReasonCode() {
        return refusalReasonCode;
    }

    public void setRefusalReasonCode(String refusalReasonCode) {
        this.refusalReasonCode = refusalReasonCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getInformationSource() {
        return informationSource;
    }

    public void setInformationSource(String informationSource) {
        this.informationSource = informationSource;
    }

    public String getAdministeredAmount() {
        return administeredAmount;
    }

    public void setAdministeredAmount(String administeredAmount) {
        this.administeredAmount = administeredAmount;
    }

    public String getVaccineMvxCode() {
        return vaccineMvxCode;
    }

    public void setVaccineMvxCode(String vaccineMvxCode) {
        this.vaccineMvxCode = vaccineMvxCode;
    }

    public String getVaccineNdcCode() {
        return vaccineNdcCode;
    }

    public void setVaccineNdcCode(String vaccineNdcCode) {
        this.vaccineNdcCode = vaccineNdcCode;
    }

    public String getVaccineCvxCode() {
        return vaccineCvxCode;
    }

    public void setVaccineCvxCode(String vaccineCvxCode) {
        this.vaccineCvxCode = vaccineCvxCode;
    }

    public Date getAdministeredDate() {
        return administeredDate;
    }

    public void setAdministeredDate(Date administeredDate) {
        this.administeredDate = administeredDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInformationStatement() {
        return informationStatement;
    }

    public void setInformationStatement(String informationStatement) {
        this.informationStatement = informationStatement;
    }

    public Date getInformationStatementPresentedDate() {
        return informationStatementPresentedDate;
    }

    public void setInformationStatementPresentedDate(Date informationStatementPresentedDate) {
        this.informationStatementPresentedDate = informationStatementPresentedDate;
    }

    public String getFinancialStatus() {
        return financialStatus;
    }

    public void setFinancialStatus(String financialStatus) {
        this.financialStatus = financialStatus;
    }

    public String getInformationStatementCvx() {
        return informationStatementCvx;
    }

    public void setInformationStatementCvx(String informationStatementCvx) {
        this.informationStatementCvx = informationStatementCvx;
    }

    public Date getInformationStatementPublishedDate() {
        return informationStatementPublishedDate;
    }

    public void setInformationStatementPublishedDate(Date informationStatementPublishedDate) {
        this.informationStatementPublishedDate = informationStatementPublishedDate;
    }
}