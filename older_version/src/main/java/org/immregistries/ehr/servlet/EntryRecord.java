package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;
import org.immregistries.iis.kernal.model.CodeMapManager;
import org.immregistries.ehr.model.Clinician;
import org.immregistries.ehr.model.Facility;

/**
 * Servlet implementation class EntryRecord
 */
public class EntryRecord extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @SuppressWarnings("UnusedAssignment")
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Boolean creation = true;
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();


    Transaction transaction = dataSession.beginTransaction();
    Facility facility = (Facility) session.getAttribute("facility");
    Patient patient = (Patient) session.getAttribute("patient");
    int paramEntry;

    VaccinationEvent vaccinationEvent;
    Vaccine vaccine;
    String nameAdmi = req.getParameter("administeringClinician");
    String nameOrder = req.getParameter("orderingClinician");
    String nameEnter = req.getParameter("enteringClinician");
    Clinician administrating = new Clinician();
    Clinician ordering = new Clinician();
    Clinician entering = new Clinician();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date updatedDate = new Date();
    Date administeredDate=new Date();
    Date expiredDate=new Date();

    if(req.getParameter("paramEntryId")!=null && !req.getParameter("paramEntryId").equals("null")){
      paramEntry =  Integer.parseInt(req.getParameter("paramEntryId"));
      creation = false;
      vaccinationEvent = (VaccinationEvent) dataSession.load(VaccinationEvent.class,paramEntry);
      vaccine = vaccinationEvent.getVaccine();

      administrating =  (Clinician) dataSession.load(administrating.getClass(), vaccinationEvent.getAdministeringClinician().getClinicianId());
      ordering =  (Clinician) dataSession.load(ordering.getClass(), vaccinationEvent.getOrderingClinician().getClinicianId());
      entering =  (Clinician) dataSession.load(entering.getClass(), vaccinationEvent.getEnteringClinician().getClinicianId());

    } else {
      creation = true;
      vaccine = new Vaccine();
      vaccinationEvent = new VaccinationEvent();

      try {
        administeredDate = sdf.parse(req.getParameter("administeredDate"));
        expiredDate = sdf.parse(req.getParameter("expirationDate"));
      } catch (ParseException e) {
        System.err.println(req.getParameter("administeredDate"));
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    administrating.fillFromFullname(nameAdmi);
    ordering.fillFromFullname(nameOrder);
    entering.fillFromFullname(nameEnter);

    vaccine.setActionCode(req.getParameter("actionCode"));
    vaccine.setAdministeredAmount(req.getParameter("administeredAmount"));
    vaccine.setAdministeredDate(updatedDate);
    vaccine.setBodyRoute(req.getParameter("bodyRoute"));
    vaccine.setBodySite(req.getParameter("bodySite"));
    vaccine.setCompletionStatus(req.getParameter("completionStatus"));
    vaccine.setCreatedDate(updatedDate);
    vaccine.setExpirationDate(updatedDate);
    vaccine.setFundingEligibility(req.getParameter("fundingEligibility"));
    vaccine.setFundingSource(req.getParameter("fundingSource"));
    vaccine.setInformationSource(req.getParameter("informationSource"));
    vaccine.setLotnumber(req.getParameter("lotNumber"));
    vaccine.setRefusalReasonCode(req.getParameter("refusalReasonCode"));
    vaccine.setUpdatedDate(updatedDate);
    vaccine.setVaccineCvxCode(req.getParameter("vaccineCvxCode"));
    vaccine.setVaccineMvxCode(req.getParameter("vaccineMvxCode"));
    vaccine.setVaccineNdcCode(req.getParameter("vaccineNdcCode"));


    vaccinationEvent.setAdministeringFacility(facility);
    vaccinationEvent.setPatient(patient);
    vaccinationEvent.setEnteringClinician(entering);
    vaccinationEvent.setOrderingClinician(ordering);
    vaccinationEvent.setAdministeringClinician(administrating);
    vaccinationEvent.setVaccine(vaccine);
    if(creation) {
      dataSession.save(administrating);
      dataSession.save(ordering);
      dataSession.save(entering);
      dataSession.save(vaccine);
      dataSession.save(vaccinationEvent);
    } else {
      dataSession.update(administrating);
      dataSession.update(ordering);
      dataSession.update(entering);
      dataSession.update(vaccine);
      dataSession.update(vaccinationEvent);
    }
    transaction.commit();

    session.setAttribute("vaccine", vaccine);
    switch(req.getParameter("nextPage")) {
      case "patient_record":
        resp.sendRedirect("patient_record");
        break;
      case "IIS_message":
        resp.sendRedirect("IIS_message");
        break;
      case "FHIR_messaging":
        resp.sendRedirect("FHIR_messaging?paramEntryId=" + vaccinationEvent.getVaccinationEventId());
        break;
    }
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    Session dataSession = PopServlet.getDataSession();
    try {
      {
        ServletHelper.doStandardHeader(out, req, "Vaccination form");
        Boolean creation = true;


        Patient patient = (Patient) session.getAttribute("patient");
        Facility facility = (Facility) session.getAttribute("facility");

        Vaccine vaccine = new Vaccine();
        VaccinationEvent vaccinationEvent = new VaccinationEvent();
        vaccinationEvent.setVaccine(vaccine);

        Boolean preloaded = false;

        if(req.getParameter("paramEntryId")!=null && !req.getParameter("paramEntryId").equals("null") && patient != null) {
          creation = false;
          Query queryVaccination = dataSession.createQuery("from VaccinationEvent where vaccination_event_Id=? and patient_id=?");
          queryVaccination.setParameter(0, Integer.parseInt(req.getParameter("paramEntryId")));
          queryVaccination.setParameter(1, patient.getPatientId());
          vaccinationEvent = (VaccinationEvent) queryVaccination.uniqueResult();
          vaccine = vaccinationEvent.getVaccine();
          preloaded = true;
          session.setAttribute("vaccine", vaccine);
          session.setAttribute("vaccinationEvent", vaccinationEvent);
        } else {
          creation = true;
          if(req.getParameter("testEntry")!=null) { // Generate random test vaccination
            preloaded = true;
            vaccinationEvent = VaccinationEvent.random(patient,facility);
            vaccine = vaccinationEvent.getVaccine();
            session.setAttribute("vaccine", vaccine);
            session.setAttribute("vaccinationEvent", vaccinationEvent);
          }
        }
        resp.setContentType("text/html");
        if (creation){
          out.println("<button onclick=\"location.href='entry_record?testEntry=1'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Fill with test informations</button><br/>");
        }


        printEntryForm(req, out, vaccine, vaccinationEvent, preloaded);
        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static void printEntryForm(HttpServletRequest req,  PrintWriter out, Vaccine vaccine, VaccinationEvent vaccinationEvent, Boolean preloaded) {

    String administering = "";
    String  entering = "";
    String  ordering = "";

    if (preloaded){ // Load Vaccination info in form
      administering = "" + vaccinationEvent.getAdministeringClinician().getNameFirst() + " "
              + vaccinationEvent.getAdministeringClinician().getNameMiddle() + " "
            + vaccinationEvent.getAdministeringClinician().getNameLast();
      entering = "" + vaccinationEvent.getEnteringClinician().getNameFirst() + " "
              + vaccinationEvent.getEnteringClinician().getNameMiddle() + " "
              + vaccinationEvent.getEnteringClinician().getNameLast();
      ordering = "" + vaccinationEvent.getOrderingClinician().getNameFirst() + " "
              + vaccinationEvent.getOrderingClinician().getNameMiddle() + " "
              + vaccinationEvent.getOrderingClinician().getNameLast();
    }

    CodeMap codeMap = CodeMapManager.getCodeMap();
    Collection<Code> codeListCVX=codeMap.getCodesForTable(CodesetType.VACCINATION_CVX_CODE);
    Collection<Code>codeListMVX=codeMap.getCodesForTable(CodesetType.VACCINATION_MANUFACTURER_CODE);
    Collection<Code>codeListNDC=codeMap.getCodesForTable(CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE);
    Collection<Code>codeListInfSource=codeMap.getCodesForTable(CodesetType.VACCINATION_INFORMATION_SOURCE);
    Collection<Code>codeListBodyRoute=codeMap.getCodesForTable(CodesetType.BODY_ROUTE);
    Collection<Code>codeListBodySite=codeMap.getCodesForTable(CodesetType.BODY_SITE);
    Collection<Code>codeListActionCode=codeMap.getCodesForTable(CodesetType.VACCINATION_ACTION_CODE);
    Collection<Code>codeListCompletionStatus=codeMap.getCodesForTable(CodesetType.VACCINATION_COMPLETION);
    Collection<Code>codeListRefusalReasonCode=codeMap.getCodesForTable(CodesetType.VACCINATION_REFUSAL);
    Collection<Code>codeListFundingSource=codeMap.getCodesForTable(CodesetType.VACCINATION_FUNDING_SOURCE);

    out.println("<form method=\"post\"  action=\"entry_record\" " +
            "class=\"\"" +
            "style=\"" +
            " display:flex ;" +
            " flex-flow: wrap ;" +
            " justify-content: space-around ;" +
            " gap: 20px 20px ;" +
            "\">");
    ServletHelper.printOpenContainer(out, 40, "row");
    ServletHelper.printDateInput(out, vaccine.getAdministeredDate(),"administeredDate", "Administered date", true);
    ServletHelper.printSimpleInput(out, vaccine.getAdministeredAmount(),"administeredAmount", "Administered amount", false, 4);
    ServletHelper.printCloseContainer(out);

    ServletHelper.printOpenContainer(out, 47, "row");
    ServletHelper.printSelectForm(out, vaccine.getVaccineCvxCode(), codeListCVX, "vaccineCvxCode", "Vaccine CVX code", 240);
    ServletHelper.printSelectForm(out, vaccine.getVaccineNdcCode(), codeListNDC, "vaccineNdcCode", "Vaccine NDC code", 240);
    ServletHelper.printCloseContainer(out);

    ServletHelper.printOpenContainer(out, 50, "row");
    ServletHelper.printSelectForm(out, vaccine.getVaccineMvxCode(), codeListMVX, "vaccineMvxCode", "Vaccine MVX code", 240);
    ServletHelper.printSimpleInput(out, vaccine.getLotnumber(),"lotNumber", "Lot number", false, 5);
    ServletHelper.printDateInput(out, vaccine.getExpirationDate(),"expirationDate", "Expiration date", false);
    ServletHelper.printCloseContainer(out);

    ServletHelper.printOpenContainer(out, 80, "row");
    ServletHelper.printSimpleInput(out,entering,"enteringClinician", "Entering clinician", true, 35);
    ServletHelper.printSimpleInput(out,ordering,"orderingClinician", "Ordering clinician", true, 35);
    ServletHelper.printSimpleInput(out,administering,"administeringClinician", "Administering clinician", true, 35);
    ServletHelper.printCloseContainer(out);

    ServletHelper.printOpenContainer(out, 47, "row");
    ServletHelper.printSelectForm(out, vaccine.getFundingSource(), codeListFundingSource, "fundingSource", "Funding source", 240);
    ServletHelper.printSimpleInput(out,vaccine.getFundingEligibility(),"fundingEligibility", "Funding eligibility", false, 10);
    ServletHelper.printCloseContainer(out);

    ServletHelper.printOpenContainer(out, 47, "row");
    ServletHelper.printSelectForm(out, vaccine.getBodyRoute(), codeListBodyRoute, "bodyRoute", "Body route", 240);
    ServletHelper.printSelectForm(out, vaccine.getBodySite(), codeListBodySite, "bodySite", "Body site", 240);
    ServletHelper.printCloseContainer(out);

    ServletHelper.printOpenContainer(out, 47, "row");
    ServletHelper.printSelectForm(out, vaccine.getInformationSource(), codeListInfSource, "informationSource", "Information source", 240);
    ServletHelper.printSelectForm(out, vaccine.getActionCode(), codeListActionCode, "actionCode", "Action code", 140);
    ServletHelper.printCloseContainer(out);

    ServletHelper.printOpenContainer(out, 47, "row");
    ServletHelper.printSelectForm(out, vaccine.getCompletionStatus(), codeListCompletionStatus, "completionStatus", "Completion status",240);
    ServletHelper.printSelectForm(out, vaccine.getRefusalReasonCode(), codeListRefusalReasonCode, "refusalReasonCode", "Refusal reason",240);
    ServletHelper.printCloseContainer(out);

    out.println("<input type=\"hidden\" id=\"paramEntryId\" name=\"paramEntryId\" value=" + req.getParameter("paramEntryId") + "></input>");
    out.println("<button type=\"submit\" name=\"nextPage\" value=\"patient_record\" style=\"height:5%\" class=\"w3-button w3-round-large w3-green w3-hover-teal \">Save</button>");
    out.println("<button type=\"submit\" name=\"nextPage\" value=\"IIS_message\" style=\"height:5%\" class=\"w3-button w3-round-large w3-green w3-hover-teal \">HL7v2 messaging</button>");
    out.println("<button type=\"submit\" name=\"nextPage\" value=\"FHIR_messaging\" style=\"height:5%\" class=\"w3-button w3-round-large w3-green w3-hover-teal \">FHIR Messaging </button>");
    out.println("</form></div>");
  }


}