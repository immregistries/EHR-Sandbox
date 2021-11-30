package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Silo_creation
 */
public class Silo_creation extends HttpServlet {
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
	        out.println("<form method=\"post\" action=\"authentication\">\r\n"
	        		+ "                <fieldset>\r\n"
	        		+ "  						<input type=\"text\" placeholder=\"Silo name\" id=\"EHRuid\" name=\"EHRuid\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		+ "	                	<br />\r\n"
	        		+ "	                    <br />\r\n"
	        		+ "	                    <br />\r\n"
	        		+ "	                    <input type=\"password\" placeholder=\"Location\" id=\"motdepasse\" name=\"motdepasse\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		+ "	                    <br />\r\n"
	        		+ "                </fieldset>\r\n"
	        		+"<button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/silos\'\" class=\"boutton\"  >Validate</button>\r\n"
	        		+ "            </form>");  
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
	    out.println("<link rel=\"stylesheet\" href=\"inc/Silo_creation.css\" />");
	    out.println("  </head>");
	    out.println("  <body>");
	    out.println("    <header class=\"w3-container w3-light-grey\">");
	    out.println("<header>\r\n"
	    		+ "    		<h1>Silo creation</h1>\r\n"
	    		+ "    	</header>");
	    out.println("<div id=\"formulaire\">");

	  }

	  public static void doFooter(PrintWriter out, HttpSession session) {
	    out.println("</div>\r\n"
	    		+ "    </body>\r\n"
	    		+ "</html>");
	  }

}