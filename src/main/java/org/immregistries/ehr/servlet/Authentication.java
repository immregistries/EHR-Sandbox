package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.ehr.SoftwareVersion;
import org.immregistries.ehr.model.ImmunizationRegistry;
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
    List<Tester> testerList;
    List<ImmunizationRegistry> IRList;
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
      newIR.setIisFacilityId(username);
      newIR.setIisHL7Url("https://florence.immregistries.org/iis-sandbox/soap");
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

      //setting session to expiry in 30 mins
      session.setMaxInactiveInterval(30*60);

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
        out.println("<div class=\"w3-container w3-half w3-margin-top\">");
        if (show == null) {


          out.println("<h2>EHR Sandbox</h2>");
          out.println(" The EHR Sandbox is a testing tool developed by AIRA and NIST along with the IIS Sandbox tool. " +
                  "It's purpose is to simulate the behaviour of a Electronic health record (EHR).");

          out.println("<h6 class=\"w3-margin-top\"><a href=\"https://github.com/immregistries/EHR-Sandbox\">Github Repository</a></h6>");


          out.println("<h4 class=\"w3-margin-top\">Some functionalities :</h4>");

          out.println("<ul class=\"w3-ul \">");
          out.println("<li>Input of patient and immunization data</li>");
          out.println("<li>Generation of testing data</li>");
          out.println("<li>Send data to IIS through Hl7v2 messages</li>");
          out.println("<li>Send data to IIS through FHIR messages</li>");
          out.println("</ul>");

          out.println( "<div class=\"w3-panel w3-yellow\"><p class=\"w3-left-align\">"
                  + "This system is for test purposes only. "
                  + "Do not submit production data. This system is not secured for safely holding personally identifiable health information."
                  + "</p></div>");
        }
        out.println(" </div>");


        out.println("<div class=\"w3-container w3-right w3-margin-top\">");
        out.println("<h3>Sign in</h3>");
        out.println("To sign up : sign in with a new username");
        out.println("<form method=\"post\" class=\"w3-container w3-margin-top\" action=\"authentication\">");
        if(req.getParameter("wrongId")!=null) {
          out.println( "<label class=\"w3-text-red w3-margin w3-margin-bottom\"><b class=\"w3-margin\">Username and password don't match</b></label><br/>");
        }
        out.println( "<label class=\"w3-text-green\"><b>Username</b></label>"
                + "<input type=\"text\" class=\"w3-input w3-margin w3-border\" required id=\"username\" name=\"username\"" +
                "style=\"width:90%\"/>"

                + "<label class=\"w3-text-green\"><b>Password</b></label>"
                + "<input type=\"password\" class = \"w3-input w3-margin w3-border\" required id=\"pwd\" name=\"pwd\"" +
                "style=\"width:90%\"/>"

                + "<button type=\"submit\"  class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin w3-right\" style=\"margin:auto\" name=\"validate_button\" >Sign in</button>"
                + "</form> ");

        out.println("</div>");


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
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
    out.println("<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <header>");
    out.println("      <div class=\"w3-bar w3-light-grey\">");
    out.println( "<a href=\"authentication\" class=\"w3-bar-item w3-button w3-green\">EHR Sandbox</a>");

    out.println("      </div>");
    out.println("    </header>");
    out.println("    <div class=\"w3-container\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("  </div>");
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    out.println("  <div class=\"w3-container w3-green w3-bottom\">");
    out.println("<p> EHR Sandbox v" + SoftwareVersion.VERSION + " - Current Time "
            + sdf.format(System.currentTimeMillis()) + "</p>");
    out.println("  </div>");
    out.println("  </body>");
    out.println("</html>");
  }


}
