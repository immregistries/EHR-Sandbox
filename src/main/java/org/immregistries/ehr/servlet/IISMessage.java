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
import org.immregistries.ehr.model.ImmunizationRegistry;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;
import org.immregistries.iis.kernal.model.CodeMapManager;
import org.immregistries.smm.tester.connectors.Connector;
import org.immregistries.smm.tester.connectors.SoapConnector;
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
    
    

    Connector connector=null;
    try {
      connector = new SoapConnector("Test", ((ImmunizationRegistry) session.getAttribute("IR")).getIisHL7Url());
      connector.setUserid(req.getParameter("USERID"));
      connector.setPassword(req.getParameter("PASSWORD"));
      connector.setFacilityid(req.getParameter("FACILITYID"));
      session.setAttribute("SoapResponse", connector.submitMessage(req.getParameter("MESSAGEDATA"), false));
      resp.sendRedirect("IIS_message?response=1");
      
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      resp.sendRedirect("IIS_message?response=0");
    }
    
    
    //doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    String responseSoap=(String) session.getAttribute("SoapResponse");


    PrintWriter out = new PrintWriter(resp.getOutputStream());
    try {
      {
        doHeader(out, session, req);
        HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
        Tester tester = new Tester();
        Facility facility = new Facility();
        Patient patient = new Patient();
        ImmunizationRegistry IR = new ImmunizationRegistry();
        IR = (ImmunizationRegistry) session.getAttribute("IR");
        tester = (Tester) session.getAttribute("tester");
        facility = (Facility) session.getAttribute("facility");
        patient = (Patient) session.getAttribute("patient") ;
        
        Vaccine vaccine=(Vaccine) session.getAttribute("vaccine");
        HL7printer printerhl7 = new HL7printer();
        
        out.println("    <form action=\"IIS_message\" method=\"POST\" ");
        out.println(
            "<div class=\"w3-margin\">"
            + "<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\"name=\"MESSAGEDATA\"\r\n" + "     rows=\"20\" cols=\"200\">\r\n"

                + new HL7printer().buildVxu(vaccine,patient,facility).toString() + " \r\n"
                +" \r\n" + "</textarea><br/>"

                +" <label class=\"w3-text-green\"><b>IIS UserID</b></label>"
                + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" value=\""+ IR.getIisUsername()+"\" style =\"width:75%\" name=\"USERID\"/>\r\n"
                +" <label class=\"w3-text-green\"><b>IIS Password</b></label>"
                + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" value=\""+IR.getIisPassword()+"\" style =\"width:75%\" name=\"PASSWORD\"/>\r\n"
                +" <label class=\"w3-text-green\"><b>Facility ID</b></label>"
                + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" value=\""+IR.getIisFacilityId()+"\" style =\"width:75%\" name=\"FACILITYID\"/>\r\n"
                + "                <button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >send to IIS</button>\r\n"
                +"</div>");
                
        out.println("    </form>");
        out.println("<div id=\"formulaire\">");
        if(req.getParameter("response").equals("1")) {
          out.println("<p>");
          out.println("Message sent");
          out.println("</p>");
          out.println("<p>");
          out.println(responseSoap==null ? "": responseSoap);
          out.println("</p>");
        }
        if(req.getParameter("response").equals("0")) {
          out.println("<p>");
          out.println( "<label class=\"w3-text-red w3-margin w3-margin-bottom\"><b class=\"w3-margin\">Message not sent</b></label><br/>");
          out.println("</p>");
        }
        
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
    
  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
