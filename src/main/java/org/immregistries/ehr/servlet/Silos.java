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
    Session dataSession = PopServlet.getDataSession();
    try {
      {
        doHeader(out, session);
        session.setAttribute("facility", null);
        Tester tester = new Tester();
        tester = (Tester) session.getAttribute("tester");
        List<Silo> siloList = null;
        Query query = dataSession.createQuery("from Silo where tester=?");
        query.setParameter(0, tester);
        siloList = query.list();
        String show = req.getParameter(PARAM_SHOW);
        out.println(
            "  <div class=\"w3-display-left w3-border-green w3-border w3-bar-block w3-margin\"style=\"width:40% ;height:100%;overflow:auto\">\r\n");
        for (Silo siloDisplay : siloList) {
          String link = "paramSiloId=" + siloDisplay.getSiloId();
          out.println("<a href=\'facility_patient_display?" + link
              + "'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"
              + siloDisplay.getNameDisplay() + "</a>");
        }
        out.println("</div>" + "  <div class=\"w3-display-right\" style=\"width=15%\">\r\n "
            + "<button onclick=\"location.href=\'silo_creation\'\"  class=\"w3-button w3-round-large w3-green w3-hover-teal\">Create new silo</button>"
            //+ "		</div>\r\n" 	
            + "</div\r\n");
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
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">"
        + "<script type=\"text/javascript\" src=\"inc/Silos.js\"></script>");
    out.println("  </head>");
    out.println("  <body>");
    // out.println("<div class=\"w3-container \">");
    out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
        + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of silos </a>\r\n"
        + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        + "  <a href = \'silo_creation\' class=\"w3-bar-item w3-button\">Silo creation </a>\r\n"
        + "</div>" + "    	</header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}

