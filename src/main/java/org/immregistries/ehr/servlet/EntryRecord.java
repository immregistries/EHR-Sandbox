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
import org.immregistries.ehr.model.Silo;
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
        ServletHelper.doStandardHeader(out, session);
        Boolean creation = true;


        Patient patient = (Patient) session.getAttribute("patient");
        Facility facility = (Facility) session.getAttribute("facility");
        Silo silo = (Silo) session.getAttribute("silo");

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
        out.println("<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"
                + "<label class=\"w3-text-green w3-margin-right w3-margin-bottom\"><b>Current tenant : "
                + silo.getNameDisplay() + "</b></label>");

        out.println( "<label class=\"w3-text-green w3-margin-left w3-margin-bottom\"><b>Current Facility : "
                + facility.getNameDisplay() + "</b></label>");
        out.println(
                "<label class=\"w3-text-green w3-margin-left \"><b>     Current Patient : "
                        + patient.getNameFirst() + " " + patient.getNameLast() + "</b></label>" + "</div>"
        );
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

  private void printEntryForm(HttpServletRequest req,  PrintWriter out, Vaccine v, VaccinationEvent vaccinationEvent, Boolean preloaded) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    String administering = "";
    String  entering = "";
    String  ordering = "";

    String administeredDate = "";
    String expirationDate="";


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
      if (v.getAdministeredDate() != null){
        administeredDate = sdf.format(v.getAdministeredDate());
      }
      if (v.getExpirationDate() != null){
        expirationDate= sdf.format(v.getExpirationDate());
      }
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


    out.println("<form method=\"post\" class=\"w3-container\" action=\"entry_record\">\r\n");


    out.println("<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"width: 50% ;align-items:center\" "

            + " <label class=\"w3-text-green\"><b>Administered date</b></label>"
            + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
            + "    <input type=\"date\"   class = \" w3-margin w3-border\"  value=\"" + administeredDate + "\" style=\"width:75% \"  name=\"administered_date\" />\r\n"
            + "</div>"
            + "<div style =\"width: 50%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Administered amount</b></label>"
            + "    <input type=\"text\"  class = \" w3-margin w3-border\"  value=\"" + v.getAdministeredAmount() + "\" style=\"width:75% \"  name=\"administered_amount\"/>\r\n"
            + "</div>"

            + "</div>");

    out.println("<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">");
    printSelectForm(out, v.getVaccineCvxCode(), codeListCVX, "vacc_cvx", "Vaccine CVX code");
    printSelectForm(out, v.getVaccineNdcCode(), codeListNDC, "vacc_ndc", "Vaccine NDC code");
    printSelectForm(out, v.getVaccineMvxCode(), codeListMVX, "vacc_mvx", "Vaccine MVX code");
    out.println("</div>");

    out.println("<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;height:auto; display:flex\">");
    out.println("<div style =\"width: 50%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Lot number</b></label>"
            + "    <input type=\"text\"   style=\"width:75% \" class = \" w3-margin w3-border\"  value=\"" + v.getLotnumber() + "\" size=\"40\" maxlength=\"60\" name=\"lot_number\"/>"
            + "</div>");
    printSelectForm(out, v.getBodyRoute(), codeListBodyRoute, "body_route", "Body route");
    out.println("</div>");

    out.println("<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100%; height:auto; display:flex\">"
            + "<div style =\"width: 50%; align-items:center \" "
            + "    <label class=\"w3-text-green\"><b>Funding source</b></label>"
            + "    <input type=\"text\" style=\"width:75% \" class = \" w3-margin w3-border\"  value=\"" + v.getLotnumber() + "\" size=\"40\" maxlength=\"60\"name=\"funding_source\" />\r\n"
            + "</div>"
            + "<div style =\"width: 50%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Funding eligibility</b></label>"
            + "    <input type=\"text\" style=\"width:75% \" class = \" w3-margin w3-border\"  value=\"" + v.getFundingEligibility() + "\" size=\"40\" maxlength=\"60\" name=\"funding_eligibility\"/>\r\n"
            + "</div>"
            + "</div>"
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100%; display:flex\">"
            + "<div style =\"width: 30%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Entering clinician</b></label>"
            + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
            + "    <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\"" + entering + "\" size=\"40\" maxlength=\"60\" name=\"entering_cli\" />\r\n"
            + "</div>"
            + "<div style =\"width: 30%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Ordering clinician</b></label>"
            + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
            + "    <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\"" + ordering + "\" size=\"40\" maxlength=\"60\" name=\"ordering_cli\" />\r\n"
            + "</div>"
            + "<div style =\"width: 30%; align-items:center \" "
            + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
            + "<label class=\"w3-text-green\"><b>Administering clinician</b></label>"
            + "    <input type=\"text\" style=\"width: 75%\"class = \" w3-margin w3-border \"  value=\"" + administering + "\" size=\"40\" maxlength=\"60\" name=\"administering_cli\" />\r\n"
            + "</div>"
            + "</div>"


            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
            + "<div style =\"width: 50%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Vaccine ID</b></label>"
            + "    <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\"" + v.getVaccineId() + "\" size=\"40\" maxlength=\"60\" name=\"vacc_id\" />\r\n"
            + "</div>"

            + "<div style =\"width: 50%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Information source</b></label>"
            + "    <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\"" + v.getInformationSource() + "\" size=\"40\" maxlength=\"60\"  name=\"information_source\"/>\r\n"
            + "</div>"

            + "</div>"
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
            + "<div style =\"width: 50%; align-items:center \" "
            + " <label class=\"w3-text-green\"><b>Expiration_date</b></label>"
            + "    <input type=\"date\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\"" + expirationDate + "\" size=\"40\" maxlength=\"60\"name=\"expiration_date\" />\r\n"
            + "</div>");
    printSelectForm(out, v.getCompletionStatus(), codeListCompletionStatus, "completion_status", "Completion Status");
    out.println("</div>");


    out.println("<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">");
    printSelectForm(out, v.getActionCode(), codeListActionCode, "action_code", "Action code");
    printSelectForm(out, v.getRefusalReasonCode(), codeListRefusalReasonCode, "refusal_reason_code", "Refusal reason code");
    out.println("</div>");

    out.println("<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">");
    printSelectForm(out, v.getBodySite(), codeListBodySite, "body_site", "Body site");
    out.println("</div>");

    out.println("<input type=\"hidden\" id=\"paramEntryId\" name=\"paramEntryId\" value=" + req.getParameter("paramEntryId") + "></input>");
    out.println("<button type=\"submit\"  name=\"nextPage\" value=\"patient_record\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Save EntryRecord</button>\r\n");
    out.println("<button type=\"submit\" name=\"nextPage\" value=\"IIS_message\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >HL7v2 messaging</button>\r\n");
    out.println("<button type=\"submit\"  name=\"nextPage\" value=\"FHIR_messaging\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \">FHIR Messaging </button>\r\n");
    out.println("</form></div>");
  }

  private void printSelectForm(PrintWriter out, String value, Collection<Code> codeList, String fieldName, String label) {
    Code code = null;
    for(Code codeItem : codeList) {
      if (codeItem.getValue().equals(value)) {
        code=codeItem;
        break;
      }
    }
    printSelectCodeForm(out,code,codeList,fieldName,label);
  }

  private void printSelectCodeForm(PrintWriter out, Code code, Collection<Code> codeList, String fieldName, String label) {
    out.println("<div style =\"width: 30% ;align-items:center\""
            + "<label class=\"w3-text-green\"><b>" + label + "</b></label>"
            + "<p class=\"w3-margin\" style=\"width:40% height:5%\">"
            + "<SELECT style =\"width:100%\" name=\"" + fieldName + "\" size=\"1\">\r\n");
    if(code == null) {
      out.println( "<OPTION value=\"\">Select " + label + "</Option>\r\n");
    } else {
      out.println( "<OPTION value=\"" + code.getValue() + "\">" + code.getLabel() + "</Option>\r\n");
    }
    for(Code codeItem : codeList) {
      out.println("<OPTION value=\"" + codeItem.getValue() + "\">" + codeItem.getLabel() + "</Option>\r\n");
    }
    out.println( "</SELECT></p></div>");
  }


}