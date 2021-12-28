package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.ehr.HL7printer;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;
import org.immregistries.ehr.model.Vaccine;

/**
 * Servlet implementation class IIS_message
 */
public class IISMessage extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
        out.println("<button>send to IIS</button> ");
        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static void doHeader(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    /*Patient patientTest = new Patient();
    patientTest.setAddressCity("Nancy");
    patientTest.setAddressCountry("France");
    patientTest.setAddressCountyParish("County");
    patientTest.setAddressLine1("43 rue de la commanderie");
    patientTest.setAddressLine2("adresseline2");
    patientTest.setAddressState("state");
    patientTest.setAddressZip("adresseZip");
    patientTest.setBirthDate(new Date());
    patientTest.setBirthFlag(PARAM_SHOW);
    patientTest.setBirthOrder(PARAM_SHOW);
    patientTest.setCreatedDate(null);
    patientTest.setDeathDate(null);
    patientTest.setDeathFlag(PARAM_SHOW);
    patientTest.setEmail(PARAM_SHOW);
    patientTest.setEthnicity(PARAM_SHOW);
    patientTest.setFacility(null);
    patientTest.setGuardianFirst(PARAM_SHOW);
    patientTest.setGuardianLast(PARAM_SHOW);
    patientTest.setGuardianMiddle(PARAM_SHOW);
    patientTest.setGuardianRelationship(PARAM_SHOW);
    patientTest.setMotherMaiden(PARAM_SHOW);
    patientTest.setNameFirst(PARAM_SHOW);
    patientTest.setNameLast(PARAM_SHOW);
    patientTest.setNameMiddle(PARAM_SHOW);
    patientTest.setPatientId(0);
    patientTest.setPhone(PARAM_SHOW);
    patientTest.setProtectionIndicator(PARAM_SHOW);
    patientTest.setProtectionIndicatorDate(null);
    patientTest.setPublicityIndicator(PARAM_SHOW);
    patientTest.setPublicityIndicatorDate(null);
    patientTest.setRace(PARAM_SHOW);
    patientTest.setRegistryStatusIndicator(PARAM_SHOW);
    patientTest.setRegistryStatusIndicatorDate(null);
    patientTest.setSex(PARAM_SHOW);
    patientTest.setSilo(null);
    patientTest.setUpdatedDate(null);*/
    
    /*Session dataSession = PopServlet.getDataSession();
    Silo silo = new Silo();
    List<Silo> siloList = null;
    String siloId = req.getParameter("paramSiloId");
    if (siloId != null) {
      Query query = dataSession.createQuery("from Silo where siloId=?");
      query.setParameter(0, Integer.parseInt(siloId));
      siloList = query.list();
      silo = siloList.get(0);
      session.setAttribute("silo", silo);
    } else {
      silo = (Silo) session.getAttribute("silo");
      Tester tester = (Tester) session.getAttribute("tester");
      if (silo != null) {
        if (!silo.getTester().getLoginUsername().equals(tester.getLoginUsername())) {
          silo = null;
        }
      }
    }
    List<Facility> facilityList = null;
    Query query = dataSession.createQuery("from Facility where silo=?");
    query.setParameter(0, silo);
    facilityList = query.list();

    List<Patient> patientList = null;
    query = dataSession.createQuery("from Patient where silo=?");
    query.setParameter(0, silo);
    patientList = query.list();
    String showFacility = null;
    if (req.getParameter("paramFacilityId") != null) {
      showFacility = req.getParameter("paramFacilityId");
    }*/
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Date dateOfBirth = sdf.parse(req.getParameter("administered_date"));
    Vaccine vaccine=new Vaccine(0,sdf.parse(req.getParameter("administered_date")),req.getParameter("vacc_cvx"),req.getParameter("vacc_ndc"),req.getParameter("vacc_mvx"),req.getParameter("administered_amount"),req.getParameter("manufacturer"),req.getParameter("info_source"),req.getParameter("lot_number"),sdf.parse(req.getParameter("expiration_date")),req.getParameter("completion_status"),req.getParameter("action_code"),req.getParameter("refusal_reason_code"),req.getParameter("body_site"),req.getParameter("body_route"),req.getParameter("funding_source"),req.getParameter("funding_eligibility"));
    HL7printer printerhl7 = new HL7printer();
    System.out.println(req.getParameter("administered_date"));
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>EHR Sandbox</title>");
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <header class=\"w3-container w3-light-grey\">");
    out.println("<header>\r\n" + "    		<h1>Message sent to IIS</h1>\r\n" + "    	</header>");
    out.println(
        "<textarea id=\"story\" name=\"story\"\r\n" + "          rows=\"20\" cols=\"200\">\r\n"
            /*+ new HL7printer().buildHL7(new Patient()).toString() + " \r\n"*/
            + new HL7printer().buildVxu(vaccine,new Patient()).toString() + " \r\n"
            /*+ req.getParameter("OrdPhy") + " \r\n" + req.getParameter("manufacturer") + " \r\n"
            + req.getParameter("AdmDate") + " \r\n" + req.getParameter("EHRuid") + " \r\n"
            + req.getParameter("Obs")*/ + " \r\n" + "</textarea>");
    out.println("<div id=\"formulaire\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
