package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class patient_creation
 */
public class patient_creation extends HttpServlet {
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
	        out.println("<form method=\"post\" action=\"authentication\">\r\n"
	        		+ "                <fieldset>\r\n"
	        		+ "  						<input type=\"text\" placeholder=\"EHR Username\" id=\"EHRuid\" name=\"EHRuid\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		+ "	                	<br />\r\n"
	        		+ "	                    <br />\r\n"
	        		+ "	                    <br />\r\n"
	        		+ "	                    <input type=\"password\" placeholder=\"password\" id=\"motdepasse\" name=\"motdepasse\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		+ "	                    <br />\r\n"
	        		+ "                </fieldset>\r\n"
	        		+ "                <div class=\"ok\">\r\n"
	        		+ "	                <input type=\"submit\" value=\"Validate\" class=\"bouton\" />\r\n"
	        		+ "	                 <br />\r\n"
	        		+ "                </div>\r\n"
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
	    out.println("<link rel=\"stylesheet\" href=\"inc/patient_creation.css\" />");
	    out.println("  </head>");
	    out.println("  <body>");
	    out.println("    <header class=\"w3-container w3-light-grey\">");
	    out.println("<header>\r\n"
	    		+ "    		<h1>Authentication</h1>\r\n"
	    		+ "    	</header>");
	    out.println("<div id=\"formulaire\">");

	  }

	  public static void doFooter(PrintWriter out, HttpSession session) {
	    out.println("</div>\r\n"
	    		+ "    </body>\r\n"
	    		+ "</html>");
	  }

}