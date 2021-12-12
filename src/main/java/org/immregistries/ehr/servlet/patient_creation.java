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
	        out.println( "<form method=\"post\" class=\"w3-container\" action=\"authentication\">\r\n"
	        		
	        		+ 							"<label class=\"w3-text-green\"><b>Date of birth</b></label>"
	        		+ "  						<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>First Name</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Last name</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Middle name</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Mother maiden name</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Sex (F or M) </b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Race</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Adress 1</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Adress 2</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>City</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>State</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Country</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Country parish</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>phone</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>E-mail</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Ethnicity</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Birth flag</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Birth order</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Death flag</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Death date</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>publicity indicator</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>publicity indicator date</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>protection indicator</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>protection indicator date</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Registry indicator date  </b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>registry status indicator</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>registry status indicator date</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
	        		
	        		+						"	<label class=\"w3-text-green\"><b>Guardian last name</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
                    
					+						"	<label class=\"w3-text-green\"><b>Guardian first name</b></label>"	                	
					+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"

					+						"	<label class=\"w3-text-green\"><b>Guardian middle name</b></label>"	                	
					+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
					
					+						"	<label class=\"w3-text-green\"><b>Guardian relationship to patient</b></label>"	                	
	        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
                    

	        		
	        		+ "                <button onclick=\"location.href=\'patient_record\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
	        		+ "                </form> "	        			
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
	    		+ "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of silos </a>\r\n"
	    		+ "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
	    		+ "  <a href = \'silo_creation\' class=\"w3-bar-item w3-button\">Silo creation </a>\r\n"
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
