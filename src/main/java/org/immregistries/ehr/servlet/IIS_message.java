package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class IIS_message
 */
public class IIS_message extends HttpServlet {
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
	        doHeader(out, session,req);
	        String show = req.getParameter(PARAM_SHOW);
	        out.println("<button>send to IIS</button> ");  
	        doFooter(out, session);
	      }
	    } catch (Exception e) {
	      e.printStackTrace(System.err);
	    }
	    out.flush();
	    out.close();
	  }

	  public static void doHeader(PrintWriter out, HttpSession session,HttpServletRequest req) {
	    out.println("<html>");
	    out.println("  <head>");
	    out.println("    <title>EHR Sandbox</title>");
	    out.println("<link rel=\"stylesheet\" href=\"inc/IIS_message.css\" />");
	    out.println("  </head>");
	    out.println("  <body>");
	    out.println("    <header class=\"w3-container w3-light-grey\">");
	    out.println("<header>\r\n"
	    		+ "    		<h1>Message sent to IIS</h1>\r\n"
	    		+ "    	</header>");
	    out.println("<textarea id=\"story\" name=\"story\"\r\n"
	        + "          rows=\"5\" cols=\"33\">\r\n"
	        + req.getParameter("OrdPhy") +" "
	        + req.getParameter("manufacturer")+" "
	        + req.getParameter("AdmDate")+" "
	        + req.getParameter("EHRuid")
	        + "</textarea>");
	    out.println("<div id=\"formulaire\">");

	  }

	  public static void doFooter(PrintWriter out, HttpSession session) {
	    out.println("</div>\r\n"
	    		+ "    </body>\r\n"
	    		+ "</html>");
	  }

}
