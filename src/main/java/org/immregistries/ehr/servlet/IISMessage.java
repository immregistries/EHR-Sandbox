package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ca.uhn.fhir.parser.IParser;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.HL7printer;
import org.immregistries.ehr.fhir.CustomClientBuilder;
import org.immregistries.ehr.model.Clinician;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.LogsOfModifications;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;
import org.immregistries.iis.kernal.model.CodeMapManager;
import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;

/**
 * Servlet implementation class IIS_message
 */
public class IISMessage extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();
    
    //Silo silo = new Silo();
    Facility facility = new Facility();
    Patient patient = new Patient();
    VaccinationEvent vacc_ev = new VaccinationEvent();
    Vaccine vaccine = new Vaccine();

    //silo = (Silo) session.getAttribute("silo");
    facility = (Facility) session.getAttribute("facility");
    patient = (Patient) session.getAttribute("patient") ;
    String nameAdmi = req.getParameter("administering_cli");  
    String nameOrder = req.getParameter("ordering_cli");  
    String nameEnter = req.getParameter("entering_cli");  
    Clinician admicli = new Clinician();
    
    admicli.setNameLast(nameAdmi.split(" ")[0]);    
    admicli.setNameFirst(nameAdmi.split(" ").length>1? nameAdmi.split(" ")[1]:"");
    admicli.setNameMiddle(nameAdmi.split(" ").length>2 ? nameAdmi.split(" ")[2]:"");
    
    Clinician ordercli = new Clinician();
    ordercli.setNameLast(nameOrder.split(" ")[0]);    
    ordercli.setNameFirst(nameOrder.split(" ").length>1 ? nameOrder.split(" ")[1]:"");
    ordercli.setNameMiddle(nameOrder.split(" ").length>2 ? nameOrder.split(" ")[2]:"");
    
    Clinician entercli = new Clinician();
    entercli.setNameLast(nameEnter.split(" ")[0]); 
    entercli.setNameFirst(nameEnter.split(" ").length>1 ? nameEnter.split(" ")[1]:"");
    entercli.setNameMiddle(nameEnter.split(" ").length>2 ? nameEnter.split(" ")[2]:"");
    System.out.println(req.getParameter("administered_date"));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Date administeredDate=new Date();
    Date updatedDate=new Date();
    Date expiredDate=new Date();
    try {
      administeredDate = sdf.parse(req.getParameter("administered_date"));
      expiredDate = sdf.parse(req.getParameter("expiration_date"));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    LogsOfModifications log = new LogsOfModifications();
    log.setModifDate(updatedDate);
    log.setModifType("modif");

    String vaccCode = req.getParameter("action_code");
    vaccine.setActionCode(vaccCode);
    vaccine.setAdministeredAmount(req.getParameter("administered_amount"));
    vaccine.setAdministeredDate(administeredDate);
    vaccine.setBodyRoute(req.getParameter("body_route"));
    vaccine.setBodySite(req.getParameter("body_site"));
    vaccine.setCompletionStatus(req.getParameter("completion_status"));
    vaccine.setCreatedDate(updatedDate);
    vaccine.setExpirationDate(updatedDate);
    vaccine.setFundingEligibility(req.getParameter("funding_eligibility"));
    vaccine.setFundingSource(req.getParameter("funding_source"));
    vaccine.setInformationSource(req.getParameter("information_source"));
    vaccine.setLotnumber(req.getParameter("lot_number"));
    vaccine.setManufacturer(req.getParameter("manufacturer"));
    vaccine.setRefusalReasonCode(req.getParameter("refusal_reason_code"));    
    vaccine.setUpdatedDate(expiredDate);
    vaccine.setVaccineCvxCode(req.getParameter("vacc_cvx"));
    vaccine.setVaccineMvxCode(req.getParameter("vacc_mvx"));
    vaccine.setVaccineNdcCode(req.getParameter("vacc_ndc"));
    
    Transaction transaction = dataSession.beginTransaction();
    dataSession.save(log);
    dataSession.save(admicli);
    dataSession.save(ordercli);
    dataSession.save(entercli);
    dataSession.save(vaccine);
    transaction.commit();

    System.out.print(entercli.getClinicianId());
    vacc_ev.setLog(log);
    vacc_ev.setAdministeringFacility(facility);
    vacc_ev.setPatient(patient);
    vacc_ev.setEnteringClinician(entercli);
    vacc_ev.setOrderingClinician(ordercli);
    vacc_ev.setAdministeringClinician(admicli);
    vacc_ev.setVaccine(vaccine);  
    
    Transaction transaction2 = dataSession.beginTransaction();
    dataSession.save(vacc_ev);
    transaction2.commit();

    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");



    PrintWriter out = new PrintWriter(resp.getOutputStream());
    try {
      {
        doHeader(out, session, req);
        String show = req.getParameter(PARAM_SHOW);
        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static void doHeader(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {

    
    HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
    Tester tester = new Tester();
    Facility facility = new Facility();
    Patient patient = new Patient();
    tester = (Tester) session.getAttribute("tester");
    facility = (Facility) session.getAttribute("facility");
    patient = (Patient) session.getAttribute("patient") ;
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateOfBirth = sdf.parse(req.getParameter("administered_date"));
    System.out.println(req.getParameter("administered_date")+" "+sdf.parse(req.getParameter("administered_date")));
    Vaccine vaccine=new Vaccine(0,sdf.parse(req.getParameter("administered_date")),req.getParameter("vacc_cvx"),req.getParameter("vacc_ndc"),req.getParameter("vacc_mvx"),req.getParameter("administered_amount"),req.getParameter("manufacturer"),req.getParameter("info_source"),req.getParameter("lot_number"),sdf.parse(req.getParameter("expiration_date")),req.getParameter("completion_status"),req.getParameter("action_code"),req.getParameter("refusal_reason_code"),req.getParameter("body_site"),req.getParameter("body_route"),req.getParameter("funding_source"),req.getParameter("funding_eligibility"));
    HL7printer printerhl7 = new HL7printer();
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>EHR Sandbox</title>");
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
    out.println("  </head>");
    out.println("  <body>");
    out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
        + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of silos </a>\r\n"
        + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        
        + "  <a href = \'Settings\' class=\"w3-bar-item w3-right w3-button\">Settings </a>\r\n"
        + "</div>" + "      </header>");
    out.println("    <form action=\"https://florence.immregistries.org/iis-sandbox/pop\" method=\"POST\" target=\"_blank\">");
    out.println(
        "<div class=\"w3-margin\">"
        + "<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\"name=\"MESSAGEDATA\"\r\n" + "     rows=\"20\" cols=\"200\">\r\n"
            /*+ new HL7printer().buildHL7(new Patient()).toString() + " \r\n"*/
            + new HL7printer().buildVxu(vaccine,patient,facility).toString() + " \r\n"
            /*+ req.getParameter("OrdPhy") + " \r\n" + req.getParameter("manufacturer") + " \r\n"
            + req.getParameter("AdmDate") + " \r\n" + req.getParameter("EHRuid") + " \r\n"
            + req.getParameter("Obs")*/ + " \r\n" + "</textarea><br/>"

            +" <label class=\"w3-text-green\"><b>IIS UserID</b></label>"
            + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" hidden value=\""+ tester.getLoginUsername()+"\" style =\"width:75%\" name=\"USERID\"/>\r\n"
            +" <label class=\"w3-text-green\"><b>IIS Password</b></label>"
            + "<input type=\"password\"  class = \"w3-input w3-margin w3-border\" hidden value=\""+tester.getLoginPassword()+"\" style =\"width:75%\" name=\"PASSWORD\"/>\r\n"
            +" <label class=\"w3-text-green\"><b>Facility ID</b></label>"
            + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" hidden value=\""+facility.getNameDisplay()+"\" style =\"width:75%\" name=\"FACILITYID\"/>\r\n"
            + "                <button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >send to IIS</button>\r\n"
            +"</div>");
            
    out.println("    </form>");
    out.println("<div id=\"formulaire\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
