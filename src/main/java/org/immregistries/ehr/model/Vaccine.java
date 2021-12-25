package org.immregistries.ehr.model;
import java.io.Serializable;
import java.util.Date;

public class Vaccine implements Serializable {

    private static final long serialVersionUID = 1L;

    private int vaccineId = 0;
    private Date createdDate = null;
    private Date updatedDate = null;
    private Date administeredDate = null;
    private String vaccineCvxCode = ""; //il faut que ce soit un nbr
    private String vaccineNdcCode = "";
    private String vaccineMvxCode = "";
    private String administeredAmount = "";
    private String manufacturer = "";
    private String informationSource = "";
    private String lotnumber = "";
    private Date expirationDate = null;
    private String completionStatus = "";
    private String actionCode = "";// A, D or U
    private String refusalReasonCode = "";
    private String bodySite = "";
    private String bodyRoute = "";
    private String fundingSource = ""; //obx5
    private String fundingEligibility = ""; //obx5
    
    public Vaccine(int vaccineId, Date administeredDate, String vaccineCvxCode, String vaccineNdcCode, String vaccineMvxCode, String administeredAmount, String manufacturer, String informationSource, String lotnumber, Date expirationDate, String completionStatus, String actionCode, String refusalReasonCode, String bodySite, String bodyRoute, String fundingSource, String fundingEligibility) {
      this.vaccineId = vaccineId;
      this.administeredDate = administeredDate;
      this.vaccineCvxCode = vaccineCvxCode;
      this.vaccineNdcCode = vaccineNdcCode;
      this.vaccineMvxCode = vaccineMvxCode;
      this.administeredAmount =administeredAmount;
      this.manufacturer = manufacturer;
      this.informationSource = informationSource;
      this.lotnumber = lotnumber;
      this.expirationDate = expirationDate;
      this.completionStatus = completionStatus;
      this.actionCode = actionCode;
      this.refusalReasonCode = refusalReasonCode;
      this.bodySite = bodySite;
      this.bodyRoute = bodyRoute;
      this.fundingSource = fundingSource; //obx5
      this.fundingEligibility = fundingEligibility; //obx5
    }
    
    public Vaccine() {
      
    }
    public int getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(int vaccineId) {
        this.vaccineId = vaccineId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Date getAdministeredDate() {
        return administeredDate;
    }

    public void setAdministeredDate(Date administeredDate) {
        this.administeredDate = administeredDate;
    }

    public String getVaccineCvxCode() {
        return vaccineCvxCode;
    }

    public void setVaccineCvxCode(String vaccineCvxCode) {
        this.vaccineCvxCode = vaccineCvxCode;
    }

    public String getVaccineNdcCode() {
        return vaccineNdcCode;
    }

    public void setVaccineNdcCode(String vaccineNdcCode) {
        this.vaccineNdcCode = vaccineNdcCode;
    }

    public String getVaccineMvxCode() {
        return vaccineMvxCode;
    }

    public void setVaccineMvxCode(String vaccineMvxCode) {
        this.vaccineMvxCode = vaccineMvxCode;
    }

    public String getAdministeredAmount() {
        return administeredAmount;
    }

    public void setAdministeredAmount(String administeredAmount) {
        this.administeredAmount = administeredAmount;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getInformationSource() {
        return informationSource;
    }

    public void setInformationSource(String informationSource) {
        this.informationSource = informationSource;
    }

    public String getLotnumber() {
        return lotnumber;
    }

    public void setLotnumber(String lotnumber) {
        this.lotnumber = lotnumber;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getRefusalReasonCode() {
        return refusalReasonCode;
    }

    public void setRefusalReasonCode(String refusalReasonCode) {
        this.refusalReasonCode = refusalReasonCode;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getBodyRoute() {
        return bodyRoute;
    }

    public void setBodyRoute(String bodyRoute) {
        this.bodyRoute = bodyRoute;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getFundingEligibility() {
        return fundingEligibility;
    }

    public void setFundingEligibility(String fundingEligibility) {
        this.fundingEligibility = fundingEligibility;
    }
}