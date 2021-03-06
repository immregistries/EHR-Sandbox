package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.immregistries.ehr.HL7printer;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.ImmunizationRegistry;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.User;
import org.immregistries.ehr.model.Vaccine;
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
    
    

    Connector connector;
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
        ServletHelper.doStandardHeader(out, req, "IIS messaging");
        User user = (User) session.getAttribute("user");
        Facility facility = (Facility) session.getAttribute("facility");
        Patient patient = (Patient) session.getAttribute("patient") ;
        ImmunizationRegistry IR = (ImmunizationRegistry) session.getAttribute("IR");
        
        Vaccine vaccine=(Vaccine) session.getAttribute("vaccine");
        HL7printer printerhl7 = new HL7printer();
        
        out.println("<form action=\"IIS_message\" method=\"POST\">");
        out.println("<div class=\"w3-margin\">");
        out.println("<div>");
        out.println("<div class=\"w3-left\" style=\"width:30%\">"
                + "<label class=\"w3-text-green\"><b>IIS UserID</b></label>"
                + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" hidden value=\""
                + IR.getIisUsername()
                + "\" style=\"width:75%\" name=\"USERID\"/></div>");
        out.println("<div class=\"w3-left\" style=\"width:30%\">"
                + " <label class=\"w3-text-green\"><b>IIS Password</b></label>"
                + "<input type=\"password\"  class=\"w3-input w3-margin w3-border\" hidden value=\""
                + IR.getIisPassword()
                + "\" style =\"width:75%\" name=\"PASSWORD\"/></div>");
        out.println("<div class=\"w3-left\" style=\"width:30%\">"
                + " <label class=\"w3-text-green\"><b>Facility ID</b></label>"
                + "<input type=\"text\"  class=\"w3-input w3-margin w3-border\" hidden value=\""
                + IR.getIisFacilityId()
                + "\" style =\"width:75%\" name=\"FACILITYID\"/></div>");
        out.println("</div>");

        out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\"name=\"MESSAGEDATA\" rows=\"15\" cols=\"200\">\r\n"
                + new HL7printer().buildVxu(vaccine,patient,facility) + "\r\n\r\n" + "</textarea><br/>");

        out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\">send to IIS</button>\r\n"
                +"</div>");
        out.println("</form>");

        out.println("<div class=\"w3-margin\">");
        if(req.getParameter("response").equals("1")) {
          out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\"\r\n"
                  + "rows=\"3\" cols=\"200\" readonly>");
          out.println(responseSoap==null ? "": responseSoap);
          out.println("</textarea><br/>");
          out.println("<p>");
          out.println("Message sent");
          out.println("</p>");
        }
        if(req.getParameter("response").equals("0")) {
          out.println("<p>");
          out.println("<textarea class =\"w3-border w3-border-red\" id=\"story\" style=\"width:75%\"\r\n"
                  + "rows=\"3\" cols=\"200\" readonly>");
          out.println(responseSoap==null ? "": responseSoap);
          out.println("</textarea><br/>");
          out.println( "<label class=\"w3-text-red w3-margin w3-margin-bottom\"><b class=\"w3-margin\">Message not sent</b></label><br/>");
          out.println("</p>");
        }
        out.println("</div>");
        
        String show = req.getParameter(PARAM_SHOW);
        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

}
