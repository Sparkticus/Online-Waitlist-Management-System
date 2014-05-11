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
// ========================= LOGIN & SIGNUP PAGE ============================
// ==========================================================================

public class WW_Signin extends HttpServlet {
    
    private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException
    {
    res.setContentType("text/html; charset=UTF-8");
    PrintWriter out = res.getWriter();
    String selfUrl = res.encodeURL(req.getRequestURI());
    HttpSession session = req.getSession(true);
    //String sessId = session.getId();
        
    printPageHeader(out,session,selfUrl);
    Connection con = null;
    try {
        con = WalterDSN.connect("walter_db");
        String submit = req.getParameter("submit");
        if (submit==null){
        //printLoginForm(req, out, con,selfUrl);
        } else {
        if (processLogin(session,req, out,con)>0){
            String type = (String)session.getAttribute("session_type");
            if (type.equals("student")) {
            redirect(out,"WW_StudentHome");
            } else {
            redirect(out,"WW_ProfHome");
            }
        } else {      
            out.println("<div class='alert alert-danger'>");
            out.println("<b>Oops!</b> It looks like we didn't recognize your email and/or password. Please try again!</div>");
            //printLoginForm(req, out, con,selfUrl);
        }
        }
    } catch (SQLException e) {
        out.println("Error: "+e);
    } catch (Exception e) {
        e.printStackTrace(out);
    } finally {
        close(con);
    }
    printSignup(out); //prints sign up portion
    out.println("<div class='footer'>");
    out.println("<p>&copy; Joanna Bi and Lindsey Tang 2014</p>");
    out.println("</div>");
    out.println("</body>");
    out.println("</html>");
    }

    /**Close the database connection. Should be called in a "finally"
       clause, so that it gets done no matter what.*/
    private void close(Connection con) {
    if( con != null ) {
        try {
        con.close();
        }
        catch( Exception e ) {
        e.printStackTrace();
        }
    }
    }
    
    // ========================================================================
    // CONTROL PANEL
    // ========================================================================

    // Redirect Page
    public void redirect(PrintWriter out, String url)
      throws IOException, ServletException
    {
    //out.println("<meta http-equiv='refresh' content='1;url='"+url+"'>");
    
    out.println("<script type='text/javascript'>");
    out.println("window.location.href = '"+url+"'");
    out.println("</script>");
    out.println("<title>Page Redirection</title>");
    out.println("</head>");
    out.println("<body>");
        out.println("If you are not redirected automatically, follow the <a href='"+url+"'>link</a><br>");
        out.println("</body>");
        out.println("</html>");
    }
    
    // Process login information
    private int processLogin(HttpSession session, HttpServletRequest req, PrintWriter out, Connection con)
    throws SQLException
    {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
    
        try {
            PreparedStatement query = con.prepareStatement
        ("Select * from Person where email=? and pass=?");
            query.setString(1, escape(email));
            query.setString(2, escape(password));
            ResultSet result = query.executeQuery();
            
            if (result.next()) {
                String type = result.getString("usertype");
                if (type.equals("s") ) {
                    String bid=result.getString("bid");
                    String name =result.getString("name");
                    session.setAttribute("session_type", "student");
                    session.setAttribute("session_bid", bid);
                    session.setAttribute("session_name", name);
                    session.setAttribute("session_email",email);
                    getStudent(session,req,out,con,bid);
                } else {
                    String bid=result.getString("bid");
                    String name =result.getString("name");
                    session.setAttribute("session_type", "professor");
                    session.setAttribute("session_bid", bid);
                    session.setAttribute("session_name", name);
                    session.setAttribute("session_email",email);
                    getProf(session,req,out,con,bid);
                }

            } else {
                return -1;
            }
        }
        catch (SQLException e) {
            out.println("<p>Error: "+e);
        }
        return 1;
    }

    // User is a student
    private void getStudent(HttpSession session,HttpServletRequest req,
                PrintWriter out,Connection con, String bid)
    throws SQLException
    {
        try {   
            PreparedStatement query = con.prepareStatement
        ("select * from Student where bid=?");
        query.setString(1, escape(bid));
        ResultSet result = query.executeQuery();
        if (result.next()) {
        String class_year=result.getString("class_year");
        String major_minor =result.getString("major_minor");
        session.setAttribute("session_class", class_year);
        session.setAttribute("session_major_minor",major_minor);
        }
        } catch (SQLException e) {
            out.println("<p>Error: "+e);
            out.print("No entry in Student table.");
        }
    }
    
    // User is a professor
    private void getProf(HttpSession session,HttpServletRequest req, PrintWriter out,
             Connection con, String bid)
    throws SQLException
    {
        try {       
            PreparedStatement query = con.prepareStatement
        ("select * from Professor where bid =?");
            query.setString(1, escape(bid));
            ResultSet result = query.executeQuery();
            if (result.next()) {
                String department=result.getString("department");
                session.setAttribute("session_department",department);
                session.setAttribute("session_bid",bid);
            }
        } catch (SQLException e) {
            out.println("<p>Error: "+e);
            out.print("No entry in Professor table.");
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    // Check is a user is already logged in
    private int isLoggedIn(HttpSession session){
        String session_bid = (String)session.getAttribute("session_bid");
        if (session_bid!=null){
            return 1;
        } else {
            return -1;
        }
    }
    
    // HTML Code
    private void printPageHeader(PrintWriter out, HttpSession session, String selfUrl) {
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
    // Print header and body
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<div class='header'>");
    out.println("<ul class='nav nav-pills pull-right'>");
        if (isLoggedIn(session)>0){
            String type = (String)session.getAttribute("session_type");
        //out.println("<ul class='nav nav-pills pull-right'>");
            if (type.equals("student")){
                out.println("<li><a href='/walter/servlet/WW_StudentHome'>Dashboard</a></li>");
            } else {
                out.println("<li><a href='/walter/servlet/WW_ProfHome'>Dashboard</a></li>");
                out.println("<li><a href='/walter/servlet/WW_CreateWaitlist'>Create Waitlist</a></li>");
            }
            out.println("<li><a href='WW_Logout'>Logout</a></li>");
        } else {
            //out.println("<li class='active'><a href="+selfUrl+">Sign in</a></li>");
        out.println("<form class='navbar-form navbar-right' role='form' method='post' action='/walter/servlet/WW_Signin'>"+
            "<div class='form-group'><input required type='email' placeholder='Email' name='email' class='form-control'></div> "+
            "<div class='form-group'><input required type='password' placeholder='Password' name='password' class='form-control'></div> "+
            "<input type='submit' name='submit' value='Sign in' class='btn btn-success'></form>");
        //out.println("<ul class='nav nav-pills pull-right'>");
        }
        //out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
    out.println("</ul>");
    out.println("<h3 class='text-muted'>Walter</h3>");
    out.println("</div>");
    }

    // Print out Sign up div
    private void printSignup(PrintWriter out) {
    out.println("<div class='jumbotron'>");
    out.println("<h1>Welcome!</h1>");
        out.println("<p class='lead'>Welcome to the alpha version of <b>Walter, the Online Waitlist Management System</b>.<br>By automating the process of creating and managing waitlists, we hope to make our application a convenient and easy to use alternative to manually processing data.</p><p>We hope you enjoy our app!</p><br>");
        out.println("<p><a class='btn btn-lg btn-success' href='WW_CreateAccount' role='button'>Sign up today!</a>&nbsp;&nbsp;&nbsp;");
    out.println("&nbsp;&nbsp;&nbsp;<a class='btn btn-lg btn-success' href='WW_WaitlistSearch' role='button'>Browse around!</a>");
        out.println("</div>");
    }

    /**
    // Print login form
    private void printLoginForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
    {
        out.println("<div class='jumbotron'>");
        out.println("<h1>Returning?</h1>");
    out.println("<p class='lead'>Returning user? Login below!");
        out.println("<form method='post' action='"+selfUrl+"'>");
        out.println("<center><table cols='2'>");
        out.println("<tr><td><p>Email <input required type='email' name='email'></tr></td>");
        out.println("<tr><td><p>Password <input required type='password' name='password'></tr></td>");
    out.println("<tr><td><p><input type='submit' name='submit' value='Log In'></form></tr></td>");
        //out.println("<tr><td><p><input type='submit' name='submit' value='Log In'></form>"+
        //            "<form action=/walter/servlet/WW_CreateAccount><button type=submit>Sign up!</button></form>"+
    //        "</tr></td>");
        out.println("</table></center>");
    out.println("</p></div>");
    }
    */
    // ========================================================================
    // HELPER METHOD: ESCAPING
    // =======================================================================

    // Function to prevent XSS attacks
    private static String escape(String raw) {
        return StringEscapeUtils.escapeHtml(raw);
    }
    
    // ========================================================================
    // These are the entry points for HttpServlets
    // ========================================================================
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    {
        try {
            doRequest(req,res);
        }
        catch (SQLException e) {
        }
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    {
        try {
            doRequest(req,res);
        }
        catch (SQLException e) {
        }
    }
    
}
