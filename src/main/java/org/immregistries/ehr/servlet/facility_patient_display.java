package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
	    try {
	      {
	        doHeader(out, session);
	        String show = req.getParameter(PARAM_SHOW);
	        out.println( "  <div class=\"leftpane\">\r\n"
	        		+ "<h4>patients</h4>"
	        		+		"<a href=\'http://localhost:9091/ehr-sandbox/patient_record'\"style=\"text-decoration:none;\" \">"
	        		        			+ 		"<div class = clickable_silo>patient 1</div>\r\n"
	        		        			+"</a>"
        			+    "<a href=\'http://localhost:9091/ehr-sandbox/patient_record'\"style=\"text-decoration:none;\" \">"
        			+ 		"<div class = clickable_silo>patient 2</div>"
        			+"</a>"
	        		+ "  	 </div>\r\n"
	        		+ "  <div class=\"middlepane\">\r\n"
	        		+ "<h4>facilities</h4>"
	        		+		"<a href=\'http://localhost:9091/ehr-sandbox/facility_patient_display'\"style=\"text-decoration:none;\" \">"
        			+ 		"<div class = clickable_silo>facility 1</div>\r\n"
        			+"</a>"
        			+    	"<a href=\'http://localhost:9091/ehr-sandbox/facility_patient_display'\"style=\"text-decoration:none;\" \">"
        			+ 		"<div class = clickable_silo>facility 2</div>"
        			+"</a>"
    	            +	"</div>\r\n"
	        		+ "  <div class=\"rightpane\">\r\n"
	        		+       "<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/patient_creation'\" class=\"boutton\">create new patient</button>\r\n"
	        		+      "<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/silo_creation'\"class=\"boutton\">create new silo</button>"
	        		+ "		</div>\r\n"
	        		);  
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
	    out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"inc/facility_patient_display.css\" />");
	    out.println("  </head>");
	    out.println("  <body>");
	    out.println("<header>\r\n"
	    		+ "    		<h1>patient facility</h1>\r\n"
	    		+ "    	</header>");
	    out.println("<div id=\"formulaire\">");

	  }

	  public static void doFooter(PrintWriter out, HttpSession session) {
	    out.println("</div>\r\n"
	    		+ "    </body>\r\n"
	    		+ "</html>");
	  }


}
