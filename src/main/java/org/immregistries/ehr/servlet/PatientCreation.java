package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;

/**
 * Servlet implementation class patient_creation
 */
public class PatientCreation extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();

    Silo silo = new Silo();
    Facility facility = new Facility();
    Patient patient = new Patient();

    silo = (Silo) session.getAttribute("silo");
    facility = (Facility) session.getAttribute("facility");

    patient.setSilo(silo);
    patient.setFacility(facility);

    patient.setNameFirst(req.getParameter("first_name"));
    patient.setNameLast(req.getParameter("last_name"));
    patient.setNameMiddle(req.getParameter("middle_name"));
    patient.setAddressCity(req.getParameter("city"));
    patient.setAddressCountry(req.getParameter("country"));
    patient.setAddressCountyParish(req.getParameter("county"));
    patient.setAddressState(req.getParameter("state"));
    //patient.setBirthDate(req.getParameter("DoB"));
    patient.setBirthFlag(req.getParameter("birth_flag"));
    patient.setBirthOrder(req.getParameter("birth_order"));
    //patient.setDeathDate(null);
    patient.setDeathFlag(req.getParameter("death_flag"));
    patient.setEmail(req.getParameter("email"));
    patient.setEthnicity(req.getParameter("ethnicity"));
    patient.setGuardianFirst(req.getParameter("guardian_first"));
    patient.setGuardianLast(req.getParameter("guardian_last"));
    patient.setGuardianMiddle(req.getParameter("guardian_middle"));
    patient.setGuardianRelationship(req.getParameter("guardian_relation"));
    patient.setMotherMaiden(req.getParameter("mother_maiden"));
    patient.setPhone(req.getParameter("phone"));
    patient.setProtectionIndicator(req.getParameter("protection_indicator"));
    patient.setProtectionIndicatorDate(null);
    patient.setPublicityIndicator(req.getParameter("publicity_indicator"));
    patient.setPublicityIndicatorDate(null);
    patient.setRace(req.getParameter("race"));
    patient.setRegistryStatusIndicator(req.getParameter("registry_status_indicator"));
    patient.setRegistryStatusIndicatorDate(null);
    patient.setSex(req.getParameter("sex"));
    Date updatedDate = new Date();
    patient.setUpdatedDate(updatedDate);
    patient.setCreatedDate(updatedDate);
    patient.setBirthDate(updatedDate);
    Transaction transaction = dataSession.beginTransaction();
    dataSession.save(patient);
    transaction.commit();
    resp.sendRedirect("facility_patient_display");
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
        doHeader(out, session);
        String show = req.getParameter(PARAM_SHOW);

        out.println("<form method=\"post\" class=\"w3-container\" action=\"patient_creation\">\r\n"

            + "<label class=\"w3-text-green\"><b>Date of birth</b></label>"
            + "  						<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\" size=\"40\" maxlength=\"60\" name=\"DoB\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>First Name</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"first_name\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Last name</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"last_name\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Middle name</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"middle_name\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Mother maiden name</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"mother_maiden_name\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Sex (F or M) </b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"sex\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Race</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"race\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Address 1</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"address\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>City</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"city\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>State</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"state\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Country</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"  name=\"country\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>County/parish</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"county\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>phone</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"phone\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>E-mail</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"email\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Ethnicity</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"ethnicity\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Birth flag</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"birth_flag\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Birth order</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"birth_ order\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Death flag</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"death_flag\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Death date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"DoD\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>publicity indicator</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"publicity_indicator\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>publicity indicator date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"publicity_date\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>protection indicator</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"protection\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>protection indicator date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"protection_date\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Registry indicator date  </b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"registry_indicator_date\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>registry status indicator</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"registry_status_indicator\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>registry status indicator date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"registry_status_indicator_date\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Guardian last name</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"guardian_last_name\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Guardian first name</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"guardian_first_name\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Guardian middle name</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"guardian_middle_name\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Guardian relationship to patient</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"guardian_relation\" />\r\n"



            + "                <button onclick=\"location.href=\'patient_record\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
            + "                </form> " + "</div\r\n");


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
        + "</div>" + "    	</header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
