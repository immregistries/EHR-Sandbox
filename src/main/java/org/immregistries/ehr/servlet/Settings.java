package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.ehr.model.ImmunizationRegistry;

/**
 * Servlet implementation class Settings
 */
public class Settings extends HttpServlet {

  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {    
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();
    Transaction transaction = dataSession.beginTransaction();
    String username = req.getParameter("IIS Username");
    String password = req.getParameter("IIS Password");
    String facility = req.getParameter("IIS Facility");
    String HL7URL = req.getParameter("HL7 URL");
    String FHIRURL = req.getParameter("FHIR URL");
    ImmunizationRegistry IR = (ImmunizationRegistry) session.getAttribute("IR");
    IR.setIisUsername(username);
    IR.setIisPassword(password);
    IR.setIisFacilityId(facility);
    IR.setIisHL7Url(HL7URL);
    IR.setIisFHIRUrl(FHIRURL);
    
    dataSession.update(IR);
    transaction.commit();
    resp.sendRedirect(req.getParameter("previousPage").split("/")[req.getParameter("previousPage").split("/").length-1]);
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
        ServletHelper.doStandardHeader(out, req, "Settings");
        ImmunizationRegistry IR = (ImmunizationRegistry) session.getAttribute("IR");
        
        String show = req.getParameter(PARAM_SHOW);
        out.println("<form method=\"post\" class=\"w3-container\" action=\"Settings\">\r\n"
            + " <label class=\"w3-text-green\"><b>IIS username</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisUsername()+"\" size=\"40\" maxlength=\"60\" name=\"IIS Username\"/>\r\n"
            + " <label class=\"w3-text-green\"><b>IIS password</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisPassword()+"\" size=\"40\" maxlength=\"60\" name=\"IIS Password\"/>\r\n"
            + " <label class=\"w3-text-green\"><b>IIS facility id</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisFacilityId()+"\" size=\"40\" maxlength=\"60\" name=\"IIS Facility\"/>\r\n"
                + " <label class=\"w3-text-green\"><b>HL7v2 URL</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisHL7Url()+"\" size=\"40\" maxlength=\"60\" name=\"HL7 URL\"/>\r\n"
            + " <label class=\"w3-text-green\"><b>FHIR URL</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisFHIRUrl()+"\" size=\"40\" maxlength=\"60\" name=\"FHIR URL\"/>\r\n"
            + "                <button onclick=\"location.href='settings'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Save</button>\r\n"
            +" <input type=\"hidden\" id=\"previousPage\" name=\"previousPage\" value="+req.getHeader("referer")+">" 
            + "                </form> " + "            </div>");

        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

}