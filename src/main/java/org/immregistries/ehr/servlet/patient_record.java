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
	        out.println("<div class=\"container\">\r\n"
	        		+ "  <div class=\"leftpane\">\r\n"
	        		+    "<a href=\'http://localhost:9091/ehr-sandbox/entry'\"style=\"text-decoration:none;\" \">"
        			+ 		"<div class = clickable_silo>Entry 1</div>\r\n"
	        		+"</a>"
        			+    "<a href=\'http://localhost:9091/ehr-sandbox/entry'\"style=\"text-decoration:none;\" \">"
        			+ 		"<div class = clickable_silo>Entry 2</div>"
	        		+     "</div>\r\n"
	        		+"</a>"
	        		+ "  <div class=\"rightpane\">\r\n "
	        		+    "<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/silo_creation'\" class=\"boutton\">Historical</button>"
	        		+    "<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/silo_creation'\" class=\"boutton\">Add a new entry </button>"

	        		+ "		</div>\r\n"
	        		+ "</div>");  
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
	    out.println("<link rel=\"stylesheet\" href=\"inc/patient_record.css\" />");
	    out.println("  </head>");
	    out.println("  <body>");
	    out.println("<header>\r\n"
	    		+ "    		<h1>Name</h1>\r\n"
	    		+ "    	</header>");
	    out.println("<div id=\"formulaire\">");

	  }

	  public static void doFooter(PrintWriter out, HttpSession session) {
	    out.println("</div>\r\n"
	    		+ "    </body>\r\n"
	    		+ "</html>");
	  }

}
