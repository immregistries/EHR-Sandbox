package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class patient_record
 */
public class patient_record extends HttpServlet {
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
	        out.println( "  <div class=\"w3-display-left w3-border-green w3-border w3-bar-block w3-margin\"style=\"width:40% ;height:100%;overflow:auto\">\r\n"
	        		+"<h3>Entries</h3>"
	        		+    "<a href=\'http://localhost:9091/ehr-sandbox/Entry'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
        				
	        		+"Entry 1</a>"
        			+    "<a href=\'http://localhost:9091/ehr-sandbox/Entry'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
    				
        			+"Entry 2</a>"
        			+"</div>"
	        		+ "  <div class=\"w3-display-right w3-margin\"style=\"width:15%\">\r\n "
	        		+    	"<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/silo_creation'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new facility</button>"
	        		+		"<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/patient_creation'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new patient </button>"
	        		+		"<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/entry'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new entry </button>"	
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
	    		+ "  <a href = \'http://localhost:9091/ehr-sandbox/silos \' class=\"w3-bar-item w3-button\">List of silos </a>\r\n"
	    		+ "  <a href = \'http://localhost:9091/ehr-sandbox/facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
	    		+ "  <a href = \'http://localhost:9091/ehr-sandbox/silo_creation\' class=\"w3-bar-item w3-button\">Silo creation </a>\r\n"
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
