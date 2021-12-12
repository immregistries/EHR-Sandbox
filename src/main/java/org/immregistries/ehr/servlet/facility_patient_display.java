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
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;

/**
 * Servlet implementation class facility_patient_display
 */
public class facility_patient_display extends HttpServlet {
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
	    Session dataSession = PopServlet.getDataSession();
	    try {
	      {
	        doHeader(out, session);
	        Silo silo = new Silo();
	        List<Facility> facilityList = null;
            Query query = dataSession.createQuery(
                "from facility where silo=?");
            query.setParameter(0,silo);
            facilityList = query.list();
            
            List<Patient> patientList = null;
            query = dataSession.createQuery(
                "from patient where silo=?");
            query.setParameter(0,silo);
            patientList = query.list();

	        String show = req.getParameter(PARAM_SHOW);
	        out.println( "  <div class=\"w3-display-left w3-border-green w3-border w3-bar-block w3-margin\"style=\"width:30% ;height:100%;overflow:auto\">\r\n"
	        		+    "<h3>Facilities</h3>"
	        		+    "<a href=\'facility_patient_display'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
        				
	        		+"Test facility 1</a>"
        			+    "<a href=\'facility_patient_display'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
    				
        			+"Test facility 2</a>"
        			+"</div>"
        			+"  <div class=\"w3-display-middle w3-border-green w3-border w3-bar-block w3-margin\"style=\"width:30% ;height:100%;overflow:auto\">\r\n"
        			+"<h3>Patients</h3>"
	        		+    "<a href=\'patient_record'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
        				
	        		+"Test patient 1</a>"
        			+    "<a href=\'patient_record'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
    				
        			+"Test patient 2</a>"
        			+"</div>"
	        		+ "  <div class=\"w3-display-right w3-margin\"style=\"width:15%\">\r\n "
	        		+    "<button onclick=\"location.href=\'silo_creation'\" style=\"width:100%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new facility</button>"
	        		+"<button onclick=\"location.href=\'patient_creation'\" style=\"width:100%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new patient </button>"
	        			
	        		+"</div\r\n");
	        
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
	    out.println("<header >\r\n"
	    		+ "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
	    		+    	"<button onclick=\"location.href=\'silo_creation'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new facility</button>"
        		+		"<button onclick=\"location.href=\'patient_creation'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new patient </button>"
        		+		"<button onclick=\"location.href=\'entry'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new entry </button>"	
        		+ "</div>"
	    		+ "    	</header>");
	    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
	  }

	  public static void doFooter(PrintWriter out, HttpSession session) {
	    out.println("</div>\r\n"
	    		+ "    </body>\r\n"
	    		+ "</html>");
	  }


}
