package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.ImmunizationRegistry;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;

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

    String username = req.getParameter("IIS Username");
    String password = req.getParameter("IIS Password");
    String facility = req.getParameter("IIS Facility");
    String URL = req.getParameter("IIS URL");
    ImmunizationRegistry IR = (ImmunizationRegistry) session.getAttribute("IR");
    IR.setIisUsername(username);
    IR.setIisPassword(password);
    IR.setIisFacilityId(facility);
    IR.setIisUrl(URL);
    
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
        doHeader(out, session);
        ImmunizationRegistry IR = (ImmunizationRegistry) session.getAttribute("IR");
        
        String show = req.getParameter(PARAM_SHOW);
        out.println("<form method=\"post\" class=\"w3-container\" action=\"Settings\">\r\n"
            + "<label class=\"w3-text-green\"><b>IIS credentials</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisUsername()+"\" size=\"40\" maxlength=\"60\" name=\"IIS Username\"/>\r\n"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisPassword()+"\" size=\"40\" maxlength=\"60\" name=\"IIS Password\"/>\r\n"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisFacilityId()+"\" size=\"40\" maxlength=\"60\" name=\"IIS Facility\"/>\r\n"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+IR.getIisUrl()+"\" size=\"40\" maxlength=\"60\" name=\"IIS URL\"/>\r\n"
            + "                <button onclick=\"location.href=\'silos\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
            + "                </form> " + "            </div>");

        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static void doHeader(PrintWriter out, HttpSession session) {
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>EHR Sandbox</title>");
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
    out.println("  </head>");
    out.println("  <body>");
    // out.println("<div class=\"w3-container \">");
    out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
        + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of silos </a>\r\n"
        + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        + "  <a href = \'silo_creation\' class=\"w3-bar-item w3-button\">Silo creation </a>\r\n"
        + "  <a href = \'Settings\' class=\"w3-bar-item w3-button\">Settings </a>\r\n"
        + "</div>" + "      </header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }


}