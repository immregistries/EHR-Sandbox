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

    VaccinationEvent vacc_ev;
    Vaccine vaccine;
    String nameAdmi = req.getParameter("administering_cli");
    String nameOrder = req.getParameter("ordering_cli");
    String nameEnter = req.getParameter("entering_cli");
    Clinician admicli = new Clinician();
    Clinician ordercli = new Clinician();
    Clinician entercli = new Clinician();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date updatedDate = new Date();
    Date administeredDate=new Date();
    Date expiredDate=new Date();

    if(req.getParameter("paramEntryId")!=null && !req.getParameter("paramEntryId").equals("null")){
      paramEntry =  Integer.parseInt(req.getParameter("paramEntryId"));
      creation = false;
      vacc_ev = (VaccinationEvent) dataSession.load(new VaccinationEvent().getClass(),paramEntry);
      vaccine = vacc_ev.getVaccine();

      admicli =  (Clinician) dataSession.load(admicli.getClass(), vacc_ev.getAdministeringClinician().getClinicianId());
      ordercli =  (Clinician) dataSession.load(ordercli.getClass(), vacc_ev.getOrderingClinician().getClinicianId());
      entercli =  (Clinician) dataSession.load(entercli.getClass(), vacc_ev.getEnteringClinician().getClinicianId());

    } else {
      creation = true;
      vaccine = new Vaccine();
      vacc_ev = new VaccinationEvent();

      try {
        administeredDate = sdf.parse(req.getParameter("administered_date"));
        expiredDate = sdf.parse(req.getParameter("expiration_date"));
      } catch (ParseException e) {
        System.err.println(req.getParameter("administered_date"));
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }


    admicli.setNameLast(nameAdmi.split("").length>0 ? nameAdmi.split(" ")[0]:" ");
    admicli.setNameFirst(nameAdmi.split(" ").length>1 ? nameAdmi.split(" ")[1]: " ");
    admicli.setNameMiddle(nameAdmi.split(" ").length>2 ? nameAdmi.split(" ")[2]:" ");

    ordercli.setNameLast(nameOrder.split(" ").length>0 ? nameOrder.split(" ")[0]:" ");
    ordercli.setNameFirst(nameOrder.split(" ").length>1 ? nameOrder.split(" ")[1]:" ");
    ordercli.setNameMiddle(nameOrder.split(" ").length>2 ? nameOrder.split(" ")[2]:" ");

    entercli.setNameLast(nameEnter.split(" ").length>0 ? nameEnter.split(" ")[0]:" ");
    entercli.setNameFirst(nameEnter.split(" ").length>1 ? nameEnter.split(" ")[1]:" ");
    entercli.setNameMiddle(nameEnter.split(" ").length>2 ? nameEnter.split(" ")[2]:" ");

    vaccine.setActionCode(req.getParameter("action_code"));
    vaccine.setAdministeredAmount(req.getParameter("administered_amount"));
    vaccine.setAdministeredDate(updatedDate);
    vaccine.setBodyRoute(req.getParameter("body_route"));
    vaccine.setBodySite(req.getParameter("body_site"));
    vaccine.setCompletionStatus(req.getParameter("completion_status"));
    vaccine.setCreatedDate(updatedDate);
    vaccine.setExpirationDate(updatedDate);
    vaccine.setFundingEligibility(req.getParameter("funding_eligibility"));
    vaccine.setFundingSource(req.getParameter("funding_source"));
    vaccine.setInformationSource(req.getParameter("information_source"));
    vaccine.setLotnumber(req.getParameter("lot_number"));
    vaccine.setRefusalReasonCode(req.getParameter("refusal_reason_code"));    
    vaccine.setUpdatedDate(updatedDate);
    vaccine.setVaccineCvxCode(req.getParameter("vacc_cvx"));
    vaccine.setVaccineMvxCode(req.getParameter("vacc_mvx"));
    vaccine.setVaccineNdcCode(req.getParameter("vacc_ndc"));


    vacc_ev.setAdministeringFacility(facility);
    vacc_ev.setPatient(patient);
    vacc_ev.setEnteringClinician(entercli);
    vacc_ev.setOrderingClinician(ordercli);
    vacc_ev.setAdministeringClinician(admicli);
    vacc_ev.setVaccine(vaccine);
    if(creation) {
      dataSession.save(admicli);
      dataSession.save(ordercli);
      dataSession.save(entercli);
      dataSession.save(vaccine);
      dataSession.save(vacc_ev);
    } else {
      dataSession.update(admicli);
      dataSession.update(ordercli);
      dataSession.update(entercli);
      dataSession.update(vaccine);
      dataSession.update(vacc_ev);
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
        resp.sendRedirect("FHIR_messaging?paramEntryId=" + vacc_ev.getVaccinationEventId());
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
        ServletHelper.doStandardHeader(out, session, req);
        Boolean creation = true;


        Patient patient = (Patient) session.getAttribute("patient");
        Facility facility = (Facility) session.getAttribute("facility");

        Vaccine v = new Vaccine();
        VaccinationEvent vaccinationEvent = new VaccinationEvent();
        vaccinationEvent.setVaccine(v);

        Boolean preloaded = false;

        if(req.getParameter("paramEntryId")!=null && !req.getParameter("paramEntryId").equals("null") && patient != null) {
          creation = false;
          Query queryVaccination = dataSession.createQuery("from VaccinationEvent where vaccination_event_Id=? and patient_id=?");
          queryVaccination.setParameter(0, Integer.parseInt(req.getParameter("paramEntryId")));
          queryVaccination.setParameter(1, patient.getPatientId());
          vaccinationEvent = (VaccinationEvent) queryVaccination.uniqueResult();
          v = vaccinationEvent.getVaccine();
          preloaded = true;
          session.setAttribute("vaccine", v);
          session.setAttribute("vacc_ev", vaccinationEvent);
        } else {
          creation = true;
          if(req.getParameter("testEntry")!=null) { // Generate random test vaccination
            preloaded = true;
            vaccinationEvent = VaccinationEvent.random(patient,facility);
            v = vaccinationEvent.getVaccine();
            session.setAttribute("vaccine", v);
            session.setAttribute("vacc_ev", vaccinationEvent);
          }
        }
        resp.setContentType("text/html");
        if (creation){
          out.println("<button onclick=\"location.href='entry_record?testEntry=1'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Fill with test informations</button><br/>");
        }


        printEntryForm(req, out, v, vaccinationEvent, preloaded);
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
              + vaccinationEvent.getAdministeringClinician().getNameLast() + " "
              + vaccinationEvent.getAdministeringClinician().getNameMiddle();
      entering = "" + vaccinationEvent.getEnteringClinician().getNameFirst() + " "
              + vaccinationEvent.getEnteringClinician().getNameLast() + " "
              + vaccinationEvent.getEnteringClinician().getNameMiddle();
      ordering = "" + vaccinationEvent.getOrderingClinician().getNameFirst() + " "
              + vaccinationEvent.getOrderingClinician().getNameLast() + " "
              + vaccinationEvent.getOrderingClinician().getNameMiddle();
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
            " justify-content : space-around ;" +
            " gap: 20px 20px ;" +
            "\">");

    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:47% ; display:flex\">");
    printDateInput(out, vaccine.getAdministeredDate(),"administered_date", "Administered date", 50 , true);
    printSimpleInput(out, vaccine.getAdministeredAmount(),"administered_amount", "Administered amount", 50 , false);
    out.println("</div>");

    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:47% ; display:flex\">");
    printSelectForm(out, vaccine.getVaccineCvxCode(), codeListCVX, "vacc_cvx", "Vaccine CVX code", 45);
    printSelectForm(out, vaccine.getVaccineNdcCode(), codeListNDC, "vacc_ndc", "Vaccine NDC code", 45);
    out.println("</div>");

    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:75% ; display:flex ; flex-direction: row\">");
    printSelectForm(out, vaccine.getVaccineMvxCode(), codeListMVX, "vacc_mvx", "Vaccine MVX code", 30);
    printSimpleInput(out, vaccine.getLotnumber(),"lot_number", "Lot number", 30 , false);
    printDateInput(out, vaccine.getExpirationDate(),"expiration_date", "Expiration date", 30 , false);
    out.println("</div>");

    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:75% ; display:flex\">");
    printSimpleInput(out,entering,"entering_cli", "Entering clinician", 30 , true);
    printSimpleInput(out,ordering,"ordering_cli", "Ordering clinician", 30 , true);
    printSimpleInput(out,administering,"administering_cli", "Administering clinician", 30 , true);
    out.println("</div>");

    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:47% ; display:flex ; flex-direction: row\">");
    printSelectForm(out, vaccine.getFundingSource(), codeListFundingSource, "funding_source", "Funding source", 45);
    printSimpleInput(out,vaccine.getFundingEligibility(),"funding_eligibility", "Funding eligibility", 50 , false);
    out.println("</div>");

    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:47% ; display:flex ; flex-direction: row\">");
    printSelectForm(out, vaccine.getBodyRoute(), codeListBodyRoute, "body_route", "Body route", 45);
    printSelectForm(out, vaccine.getBodySite(), codeListBodySite, "body_site", "Body site", 45);
    out.println("</div>");


    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:47% ; display:flex\">");
    printSelectForm(out, vaccine.getInformationSource(), codeListInfSource, "information_source", "Information source", 45);
    printSelectForm(out, vaccine.getActionCode(), codeListActionCode, "action_code", "Action code", 45);
    out.println("</div>");

    out.println("<div class=\"w3-container w3-light-gray\" style=\"width:47% ; display:flex\">");
    printSelectForm(out, vaccine.getCompletionStatus(), codeListCompletionStatus, "completion_status", "Completion status",45);
    printSelectForm(out, vaccine.getRefusalReasonCode(), codeListRefusalReasonCode, "refusal_reason_code", "Refusal reason code",45);
    out.println("</div>");


    out.println("<input type=\"hidden\" id=\"paramEntryId\" name=\"paramEntryId\" value=" + req.getParameter("paramEntryId") + "></input>");
    out.println("<button type=\"submit\" name=\"nextPage\" value=\"patient_record\" style=\"height:5%\" class=\"w3-button w3-round-large w3-green w3-hover-teal \">Save EntryRecord</button>");
    out.println("<button type=\"submit\" name=\"nextPage\" value=\"IIS_message\" style=\"height:5%\" class=\"w3-button w3-round-large w3-green w3-hover-teal \">HL7v2 messaging</button>");
    out.println("<button type=\"submit\" name=\"nextPage\" value=\"FHIR_messaging\" style=\"height:5%\" class=\"w3-button w3-round-large w3-green w3-hover-teal \">FHIR Messaging </button>");
    out.println("</form></div>");
  }

  private static void printSelectForm(PrintWriter out, String value, Collection<Code> codeList, String fieldName, String label, int width) {
    Code code = null;
    for(Code codeItem : codeList) {
      if (codeItem.getValue().equals(value)) {
        code=codeItem;
        break;
      }
    }
    out.println("<div style=\"width: " + width + "% ; align-items:center\" class=\"w3-margin\">"
            + "<label class=\"w3-text-green w3-left\"><b>"
              + label + "</b></label>"
            + "<SELECT style=\"width:95%\"  class=\"w3-select\" name=\"" + fieldName + "\" size=\"1\">");
    out.println( "<OPTION value=\"\" "
            + (code == null ? "selected" : "")
            + ">Select " + label + "</Option>");
    for(Code codeItem : codeList) {
      out.println("<OPTION value=\""
              + codeItem.getValue() + "\" "
              + (codeItem.equals(code) ? "selected" : "")
              + " >" /* + codeItem.getValue()*/
              + codeItem.getLabel() + "</Option>");
    }
    out.println( "</SELECT></div>");
  }

  public static void printSimpleInput(PrintWriter out, String input, String fieldName, String label, int width, boolean required){
    out.println("<div style=\"width: "+ width +"% ;align-items:center\" class=\"w3-margin\">"
            + "<label class=\"w3-text-green w3-left\"><b>"
            + label + (required? "<b class=\"w3-text-red\">*</b>" : "")
            + "</b></label>"
            + "<input type=\"text\""
            + " class=\"w3-input w3-border\" "
            + " value=\"" + input + "\""
            + " name=\"" + fieldName + "\""
            + "/>"
            + "</div>");
  }

  public static void printDateInput(PrintWriter out, Date dateInput, String fieldName, String label, int width, boolean required){
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String input = "";
    if (dateInput != null){
      input= sdf.format(dateInput);
    }
    out.println("<div style=\"width: "+ width +"% ; align-items:center\""
              + " class=\"w3-margin\">"
            + "<label class=\"w3-text-green w3-left\"><b>"
            + label + (required? "<b class=\"w3-text-red\">*</b>" : "")
            + "</b></label>"
            + "<input type=\"date\""
            + " class=\"w3-input w3-border\" "
            + " value=\"" + input + "\""
            + " name=\"" + fieldName + "\""
            + "/>"
            + "</div>");
  }
}