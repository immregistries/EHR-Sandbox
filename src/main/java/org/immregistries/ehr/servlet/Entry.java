package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Entry
 */
public class Entry extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Entry() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
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
	        
	      }
	    } catch (Exception e) {
	      e.printStackTrace(System.err);
	    }
	    out.flush();
	    out.close();
	  }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	
	  public static void doHeader(PrintWriter out, HttpSession session) {
		    out.println("<html>");
		    out.println("  <head>");
		    out.println("    <title>EHR Sandbox</title>");
		    out.println("<link rel=\"stylesheet\" href=\"src/main/webapp/inc/authentication.css\" />");
		    out.println("  </head>");
		    out.println("  <body>");
		    out.println("    <header class=\"w3-container w3-light-grey\">");
		    out.println("<header>\r\n"
		    		+ "    		<h1>OUILLE</h1>\r\n"
		    		+ "    	</header>");
		    out.println("<div id=\"formulaire\">");

		  }

}
