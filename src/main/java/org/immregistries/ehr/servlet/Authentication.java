package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.ehr.model.ImmunizationRegistry;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;
import org.mindrot.jbcrypt.BCrypt;

@SuppressWarnings("serial")
public class Authentication extends HttpServlet {

  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session;

    Session dataSession = PopServlet.getDataSession();
    Tester newTester = new Tester();
    ImmunizationRegistry newIR = new ImmunizationRegistry();
    List<Tester> testerList = null;
    List<ImmunizationRegistry> IRList = null;
    Query query = dataSession.createQuery("from Tester where loginUsername=?");
    String username = req.getParameter("username");
    String password = req.getParameter("pwd");
    String passwordIR = password;
    query.setParameter(0, username);
    testerList = query.list();
    int dontRedirect = 0;
    if (!testerList.isEmpty()) {
      // Checking if password matches hash
      if (BCrypt.checkpw(password, testerList.get(0).getLoginPassword())) {
        newTester = testerList.get(0);
        query = dataSession.createQuery("from ImmunizationRegistry where tester=?");
        query.setParameter(0,newTester);
        IRList = query.list();
        newIR = IRList.get(0);
      } else {
        //wrong password 
        resp.sendRedirect("authentication?wrongId=1");
        dontRedirect = 1;

      }
    } else {

      // hashing new password
      password = BCrypt.hashpw(password, BCrypt.gensalt(5));
      // Creating new tester/user
      newIR.setIisUsername(username);
      newIR.setIisPassword(passwordIR);
      newTester.setLoginUsername(username);
      newTester.setLoginPassword(password);
      Transaction transaction = dataSession.beginTransaction();
      dataSession.save(newTester);
      transaction.commit();
      newIR.setIisFacilityId("Mercy Healthcare");
      newIR.setIisHL7Url("https://florence.immregistries.org/iis-sandbox/pop");
      newIR.setIisFHIRUrl("https://florence.immregistries.org/iis-sandbox/fhir");
      newIR.setTester(newTester);
      Transaction transaction2 = dataSession.beginTransaction();
      dataSession.save(newIR);
      transaction2.commit();
    }
    if(dontRedirect==0) {
      //get the old session and invalidate
      HttpSession oldSession = req.getSession(false);
      if (oldSession != null) {
          oldSession.invalidate();
      }
      //generate a new session
      session = req.getSession(true);

      //setting session to expiry in 15 mins
      session.setMaxInactiveInterval(15*60);

      // Cookie message = new Cookie("message", "Welcome");
      // resp.addCookie(message);

      session.setAttribute("tester", newTester);
      session.setAttribute("IR", newIR);

      resp.sendRedirect("silos");
    }
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
        session.setAttribute("silo", null);
        String show = req.getParameter(PARAM_SHOW);
        out.println("<form method=\"post\" class=\"w3-container\" action=\"authentication\">\r\n");
            if(req.getParameter("wrongId")!=null) {
              out.println( "<label class=\"w3-text-red w3-margin w3-margin-bottom\"><b class=\"w3-margin\">Username and password don't match</b></label><br/>");
            }
        out.println( "<label class=\"w3-text-green\"><b>EHR username</b></label>"
            + "  					<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\" size=\"40\" maxlength=\"60\" id =\"username\" name=\"username\" />\r\n"
            + "	<label class=\"w3-text-green\"><b>Password</b></label>"
            + "	                   	<input type=\"password\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" id = \"pwd\" name=\"pwd\"/>\r\n"


            + "                <button onclick=\"location.href=\'silos\'\"  class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \" style=\"margin:auto\"name=\"validate_button\" >Validate</button>\r\n"
            + "                </form> "
            /*onclick=\"validateOnClick()\"*/
            + "            </div>"
            + "</div>");


        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    //resp.sendRedirect("/silos");
    out.flush();
    out.close();
  }

  public static void doHeader(PrintWriter out, HttpSession session) {
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>EHR Sandbox</title>");
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\" />"
    //+ "<script src =\"inc/Authentication.js\"></script>");
    );
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <header class=\"w3-container w3-light-grey\">");
    out.println("    		<h1>Authentication</h1>\r\n" + "    	</header>"
        + "<div style =\"margin:auto \">");

    out.println("<div class=\"w3-display-container \" style=\"height:20%;width:75%;margin:auto; margin-top:10%;align-items:center \">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }


}
