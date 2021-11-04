package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.immregistries.ehr.SoftwareVersion;

@SuppressWarnings("serial")
public class HomeServlet extends HttpServlet {

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
        out.println("    <div class=\"w3-container w3-half w3-margin-top\">");
        if (show == null) {
          out.println(
              "    <div class=\"w3-panel w3-yellow\"><p class=\"w3-left-align\">This system is for test purposes only. "
                  + "Do not submit production data. This system is not secured for safely holding personally identifiable heath information.  </p></div>");
          out.println("    <h2>Primary Functions Supported</h2>");
          out.println("    <ul class=\"w3-ul w3-hoverable\">");
          out.println("      <li><a href=\"home\">Home</a>: Home Page.</li>");
          out.println("    </ul>");
        }
        out.println("  </div>");
        out.println(
            "  <img src=\"images/markus-spiske-Ds8gp_7-5AM-unsplash.jpg\" class=\"w3-round\" alt=\"Sandbox\" width=\"400\">");
        out.println(
            "<a style=\"background-color:black;color:white;text-decoration:none;padding:4px 6px;font-family:-apple-system, BlinkMacSystemFont, &quot;San Francisco&quot;, &quot;Helvetica Neue&quot;, Helvetica, Ubuntu, Roboto, Noto, &quot;Segoe UI&quot;, Arial, sans-serif;font-size:12px;font-weight:bold;line-height:1.2;display:inline-block;border-radius:3px\" href=\"https://unsplash.com/@markusspiske?utm_medium=referral&amp;utm_campaign=photographer-credit&amp;utm_content=creditBadge\" target=\"_blank\" rel=\"noopener noreferrer\" title=\"Download free do whatever you want high-resolution photos from Markus Spiske\"><span style=\"display:inline-block;padding:2px 3px\"><svg xmlns=\"http://www.w3.org/2000/svg\" style=\"height:12px;width:auto;position:relative;vertical-align:middle;top:-2px;fill:white\" viewBox=\"0 0 32 32\"><title>unsplash-logo</title><path d=\"M10 9V0h12v9H10zm12 5h10v18H0V14h10v9h12v-9z\"></path></svg></span><span style=\"display:inline-block;padding:2px 3px\">Markus Spiske</span></a>");
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
    out.println("    <link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\"/>");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <header class=\"w3-container w3-light-grey\">");
    out.println("      <div class=\"w3-bar w3-light-grey\">");
    out.println(
        "        <a href=\"home\" class=\"w3-bar-item w3-button w3-green\">EHR Sandbox</a>");
    out.println("        <a href=\"home\" class=\"w3-bar-item w3-button\">Item 2</a>");
    out.println("        <a href=\"home\" class=\"w3-bar-item w3-button\">Item 3</a>");
    out.println("        <a href=\"home\" class=\"w3-bar-item w3-button\">Item 4</a>");
    out.println("        <a href=\"home\" class=\"w3-bar-item w3-button\">Item 5</a>");
    out.println("      </div>");
    out.println("    </header>");
    out.println("    <div class=\"w3-container\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("  </div>");
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    out.println("  <div class=\"w3-container w3-green\">");
    out.println("    <p>IIS Sandbox v" + SoftwareVersion.VERSION + " - Current Time "
        + sdf.format(System.currentTimeMillis()) + "</p>");
    out.println("  </div>");
    out.println("  </body>");
    out.println("</html>");
  }

}
