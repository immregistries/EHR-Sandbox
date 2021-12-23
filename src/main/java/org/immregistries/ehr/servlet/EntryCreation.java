package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.ehr.HL7printer;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Clinician;

/**
 * Servlet implementation class Entry
 */
public class EntryCreation extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Clinician cli = new Clinician();
    cli.setClinicianId(0);
    cli.setNameLast(req.getParameter("Clinician"));
    VaccinationEvent vacc_ev = new VaccinationEvent();
    vacc_ev.setAdministeringClinician(cli);
    //vacc_ev.set
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

            + "<label class=\"w3-text-green\"><b>Administering clinician</b></label>"
            + "  						<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\" size=\"40\" maxlength=\"60\" name=\"administering_cli\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Entering clinician</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"entering_clinician\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Ordering clinician</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"ordering_clinician\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Administered date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"administered_date\" />\r\n"


            + "	<label class=\"w3-text-green\"><b>Vaccine ID</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_id\" />\r\n"


            + "	<label class=\"w3-text-green\"><b>Vaccine CVX code </b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_cvx\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Vaccine NDC code</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_ndc\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Vaccine MVX code</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_mvx\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Administered amount</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"administered_amount\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Manufacturer</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"manufacturer\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Information source</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"  name=\"info_source\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Lot number</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"lot_number\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Expiration_date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"expiration_date\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>E-mail</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"email\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Completion status</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"completion_status\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Action code</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"action_code\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Refusal reason code</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"refusal_reason_code\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Body site</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"body_site\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Body route</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"body_route\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Funding source</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"funding_source\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Funding eligbility</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"funding_eligibility\"/>\r\n"

            + "                <button onclick=\"location.href=\'patient_record\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
            +"                  <button onclick=\"location.href=\'IIS_message\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >See message</button>\r\n"
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
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\" />");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <header class=\"w3-container w3-light-grey\">"
        + "    		<h1>Entry creation</h1>\r\n" + "    	</header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }


}
