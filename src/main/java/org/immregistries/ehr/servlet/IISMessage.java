package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;

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

  public static void doHeader(PrintWriter out, HttpSession session, HttpServletRequest req) {
    Patient patientTest = new Patient();
    patientTest.setAddressCity(PARAM_SHOW);
    patientTest.setAddressCountry(PARAM_SHOW);
    patientTest.setAddressCountyParish(PARAM_SHOW);
    patientTest.setAddressLine1(PARAM_SHOW);
    patientTest.setAddressLine2(PARAM_SHOW);
    patientTest.setAddressState(PARAM_SHOW);
    patientTest.setAddressZip(PARAM_SHOW);
    patientTest.setBirthDate(null);
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
    patientTest.setUpdatedDate(null);

    List<Patient> patientList = null;
    Session dataSession = PopServlet.getDataSession();
    Silo silo = new Silo();
    silo = (Silo) session.getAttribute("silo");
    Query query = dataSession.createQuery("from Facility where silo=?");
    query = dataSession.createQuery("from Patient where silo=?");
    query.setParameter(0, silo);
    patientList = query.list();
    HL7printer printerhl7 = new HL7printer();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
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
            + new HL7printer().buildHL7(new Patient()).toString() + " \r\n"
            + req.getParameter("OrdPhy") + " \r\n" + req.getParameter("manufacturer") + " \r\n"
            + req.getParameter("AdmDate") + " \r\n" + req.getParameter("EHRuid") + " \r\n"
            + req.getParameter("Obs") + " \r\n" + "</textarea>");
    out.println("<div id=\"formulaire\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
