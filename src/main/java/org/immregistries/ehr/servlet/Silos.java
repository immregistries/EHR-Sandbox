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
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;

public class Silos extends HttpServlet {
	
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
	        Tester tester = new Tester();
	        Session dataSession = PopServlet.getDataSession();
	        List<Silo> siloList = null;
            Query query = dataSession.createQuery(
                "from Silo where tester=?");
            query.setParameter(0,tester);
            siloList = query.list();
	        int count = 0;
	        String show = req.getParameter(PARAM_SHOW);
	        out.println( "  <div class=\"w3-display-left w3-border-green w3-border w3-bar-block w3-margin\"style=\"width:40% ;height:100%;overflow:auto\">\r\n"
	        		+    "<a href=\'http://localhost:9091/ehr-sandbox/facility_patient_display'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
        				
	        		+"Test silo 1</a>"
        			+    "<a href=\'http://localhost:9091/ehr-sandbox/facility_patient_display'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
    				
        			+"Test silo 2</a>"
        			+"</div>"
	        		+ "  <div class=\"w3-display-right\" style=\"width=15%\">\r\n "
	        		+    "<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/silo_creation'\" class=\"w3-button w3-round-large w3-green w3-hover-teal\">Create new silo</button>"
	        		//+ "		</div>\r\n" 	
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
	    		+ "  <div class=\"w3-bar-item\">London</div>\r\n"
	    		+ "  <div class=\"w3-bar-item\">Paris</div>\r\n"
	    		+ "  <div class=\"w3-bar-item\">Tokyo</div>\r\n"
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

