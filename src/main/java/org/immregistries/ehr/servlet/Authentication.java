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
import org.hibernate.Transaction;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;

@SuppressWarnings("serial")
public class Authentication extends HttpServlet {

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
        Session dataSession = PopServlet.getDataSession();
        
        String Username ="";
        String Password ="";
        doHeader(out, session);
        String show = req.getParameter(PARAM_SHOW);
        out.println("<form method=\"post\" class=\"w3-container\" action=\"authentication\">\r\n"
        		+ 							"<label class=\"w3-text-green\"><b>EHR username</b></label>"
        		+ "  						<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
        		+						"	<label class=\"w3-text-green\"><b>password</b></label>"	                	
        		+ "	                    	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" />\r\n"
        		
        		
        		+ "                <button onclick=\"location.href=\'http://localhost:9091/ehr-sandbox/silos\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
        		+ "                </form> "
        		+ "            </div>");
        
        List<Tester> testerList = null;
        Query query = dataSession.createQuery(
                "from Tester where loginUsername= ?");
        query.setParameter(0,Username);
        testerList = query.list();
        if(!testerList.isEmpty()) {
        if(testerList.get(0).getLoginPassword()==Password) {
          //on se connecte
          out.println("connected");  
        }
        else {
          //wrong password
          out.println("wrong password"); 
          
          
        }
        }
        else {
          //on crée le nouveau tester
          Tester newTester = new Tester();
          newTester.setLoginUsername(Username);
          newTester.setLoginPassword(Password);
          Transaction transaction = dataSession.beginTransaction();
          dataSession.save(newTester);
          transaction.commit();
          
        }
        
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
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\" />");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <header class=\"w3-container w3-light-grey\">");
    out.println( "    		<h1>Authentication</h1>\r\n"
    		+ "    	</header>");
    
   out.println("<div class=\"w3-display-container w3-margin \" style=\"height:200px;\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n"
    		+ "    </body>\r\n"
    		+ "</html>");
  }

}
