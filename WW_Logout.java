/**Joanna Bi and Lindsey Tang
   CS304: Final Project
   Spring 2014 */

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.lang.*;
import org.apache.commons.lang.StringEscapeUtils; //for the string escaping

// ==========================================================================
// =========================== WALTER WAITLIST ==============================
// ============================= LOGOUT PAGE ================================
// ==========================================================================

public class WW_Logout extends HttpServlet {
    
    private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        String selfUrl = res.encodeURL(req.getRequestURI());
        HttpSession session = req.getSession(true);
        //String sessId = session.getId();
        
        printPageHeader(out);
        try { 
            String session_bid = (String)session.getAttribute("session_bid");
            String name = (String)session.getAttribute("session_name");
            req.getSession().invalidate();
            if (!name.equals(null)){
            // Print 'logged out' message
            out.println("<div class='jumbotron'>");
            out.println("<h2>Goodbye "+name+"</h1>");
            out.println("<p class='lead'>Please come back and visit soon!</p>");
            out.println("</div>");
            }
        }
        catch (Exception e) {
            e.printStackTrace(out);
        }
        out.println("<div class='footer'>");
        out.println("<p>&copy; Joanna Bi and Lindsey Tang 2014</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");     
    }

    private void printPageHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Walter</title>");
        // Here go the bootstrap links
        out.println("<!-- Bootstrap core CSS -->");
        out.println("<link href='../css/bootstrap.min.css' rel='stylesheet'>");
        out.println("<!-- Custom styles for this template -->");
        out.println("<link href='../css/jumbotron-narrow.css' rel='stylesheet'>");
        // Here go the jquery links
        out.println("<link rel='stylesheet' href='//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css'>");
        out.println("<script src='//code.jquery.com/jquery-1.10.2.js'></script>");
        out.println("<script src='//code.jquery.com/ui/1.10.4/jquery-ui.js'></script>");
        out.println("</head>");
        // Print header
        out.println("<body>");
        out.println("<div class='container'>"); //more bootstrap begins here
        out.println("<div class='header'>");
        out.println("<ul class='nav nav-pills pull-right'>");
        out.println("<li><a href='/walter/servlet/WW_Signin'>Home</a></li>");
        out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
        out.println("<li><a href='#'>About</a></li>");
        out.println("<li><a href='#'>Contact</a></li>");
        out.println("<li class='active'><a href='WW_Logout'>Logout</a></li>");
        out.println("</ul>");
        out.println("<h3 class='text-muted'>Walter</h3>");
        out.println("</div>");
    }

    // ========================================================================
    // These are the entry points for HttpServlets
    // ========================================================================
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    { doRequest(req,res);
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    { doRequest(req,res);
    }
    
}

