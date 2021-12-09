package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.ehr.HL7printer;
import org.immregistries.ehr.model.Patient;

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
	    System.out.print("wow");
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
	    HL7printer printerhl7=new HL7printer();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    out.println("<html>");
	    out.println("  <head>");
	    out.println("    <title>EHR Sandbox</title>");
	    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
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
	        + req.getParameter("EHRuid")+" "
	        
	        + "</textarea>");
	    System.out.println(printerhl7.printQueryPID(new Patient(), new StringBuilder(), new Patient(), sdf, 1).toString()+" "
            +printerhl7.printQueryNK1(new Patient(), new StringBuilder(), new CodeMap()).toString()+" ");
	    out.println("<div id=\"formulaire\">");

	  }

	  public static void doFooter(PrintWriter out, HttpSession session) {
	    out.println("</div>\r\n"
	    		+ "    </body>\r\n"
	    		+ "</html>");
	  }

}
