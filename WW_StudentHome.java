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
// =========================== STUDENT HOMEPAGE =============================
// ==========================================================================

public class WW_StudentHome extends HttpServlet {
    
    private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException
    {   
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        String selfUrl = res.encodeURL(req.getRequestURI());
        HttpSession session = req.getSession(true);
        //String sessId = session.getId();
        printPageHeader(out,session);
        
        String session_bid = (String)session.getAttribute("session_bid");
        
        if (session_bid == null){
            //out.println("Please log in or create an account.");
            //out.println("<a href='/walter/servlet/WW_Signin'>Click here to sign in</a>");
        out.println("<div class='alert alert-danger'><strong>Oh no!</strong> To view this page you must log in or create an account. <a href='/walter/servlet/WW_Signin'>Click here to sign in</a>.</div>");
        } else {
            Connection con = null;
            try {
                
                con = WalterDSN.connect("walter_db");
                String remove_crn =req.getParameter("remove_crn");
                
                if (remove_crn != null){
                    processRemove(req, out, con,session_bid,remove_crn);
                }
        out.println("<div class='panel panel-primary'>"+
                "<div class='panel-heading'><h3 class='panel-title'>Student Stats</h3></div>"+
                "<div class='panel-body'>");
                Enumeration keys = session.getAttributeNames();
                while (keys.hasMoreElements()) {
                    String key = (String)keys.nextElement();
                    out.println(key + ": " + session.getValue(key) + "<br>");
                }
        out.println("</div></div>");
                getStudentActivity(session_bid, out, con);
                printScript(out,selfUrl);
            }
            catch (SQLException e) {
                out.println("Error: "+e);
            }
            catch (Exception e) {
                e.printStackTrace(out);
            }
            finally {
                close(con);
            }
        }
    out.println("<div class='footer'>");
    out.println("<p>&copy; Joanna Bi and Lindsey Tang 2014</p>");
    out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    // ========================================================================
    // CONTROL PANEL
    // ========================================================================

    // Remove student from waitlist
    private void processRemove(HttpServletRequest req, PrintWriter out, Connection con, String student_bid, String remove_crn)
    throws SQLException
    {
        PreparedStatement query = con.prepareStatement
        ("DELETE from Waitlist where waitlist_id=? and student_bid=?");
        query.setString(1, escape(remove_crn) );
        query.setString(2, escape(student_bid) );
        int result = query.executeUpdate();
        out.println("Student Removed<br>");
    }
    
    // Gets students' waitlist information
    private void getStudentActivity(String student_bid, PrintWriter out, Connection con)
    throws SQLException
    {
        PreparedStatement query_student = con.prepareStatement
        ("select * from Waitlist where student_bid=?");
        query_student.setString(1, escape(student_bid));
        ResultSet result_student = query_student.executeQuery();
    out.println("<div class='panel panel-primary'>"+
            "<div class='panel-heading'><h3 class='panel-title'>Currently waitlisted on:</h3></div>"+
            "<div class='panel-body'>");
    out.println("<table class='table table-hover'><thead><tr><th>Course #</th><th>Course Name</th><th>Department</th><th>Type</th><th>Course Limit</th><th>Rank</th><th>Remove</th></thead><tbody>");
        while (result_student.next()) {
            String waitlist_id=result_student.getString("waitlist_id");
            String rank = result_student.getString("rank");
            PreparedStatement query_waitlist = con.prepareStatement
        ("select * from Course where crn=?");
            query_waitlist.setString(1, escape(waitlist_id));
            ResultSet result_waitlist = query_waitlist.executeQuery();
            if (result_waitlist.next()) {
                String course_name =result_waitlist.getString("course_name");
                String course_num =result_waitlist.getString("course_num");
                String department =result_waitlist.getString("department");
                String course_limit = result_waitlist.getString("course_limit");
                String kind = result_waitlist.getString("kind");
        // Print out student activity (waitlists they are on)
                //out.println("<tr><td>"+waitlist_id+"</td>");
                out.println("<tr><td>"+course_num+"</td>");
                out.println("<td>"+course_name+"</td>");
        out.println("<td>"+department+"</td>");
                out.println("<td>"+kind+"</td>");
        out.println("<td>"+course_limit+"</td>");
                out.println("<td>"+rank+"</td>");
                out.println("<td><button class='btn btn-xs btn-danger' onclick=remove_student('"+waitlist_id+"')>Remove</button></td>");
                out.println("</tr>");
        }
        }
    out.println("</tbody></table>"); //close table
    out.println("</div></div>"); // close panel div
    }
    
    // Javascript code
    private void printScript(PrintWriter out, String selfUrl) {
        out.println("<script>"+
                    "function post_to_url(path, params, method) {"+
                    "method = method || 'post';"+
                    "var form = document.createElement('form');"+
                    "form.setAttribute('method', method);"+
                    "form.setAttribute('action', path);"+
                    "for(var key in params) {"+
                    "     if(params.hasOwnProperty(key)) {"+
                    "        var hiddenField = document.createElement('input');"+
                    "        hiddenField.setAttribute('type', 'hidden');"+
                    "        hiddenField.setAttribute('name', key);"+
                    "        hiddenField.setAttribute('value', params[key]);"+
                    "        form.appendChild(hiddenField);"+
                    "    }"+
                    "}"+
                    "document.body.appendChild(form);"+
                    "form.submit();"+
                    "}");
        out.println("function remove_student(waitlist_id) {"+
                    "if (confirm('Are you sure you want to be removed from waitlist?')) {"+
                    "post_to_url('"+selfUrl+"', {remove_crn:waitlist_id.toString()});"+
                    "} else {"+
                    "}"+
                    "};</script>");
        
    }

    // ========================================================================
    // HELPER FUNCTIONS
    // ========================================================================

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
    
    // Check if the student is logged in
    private int isLoggedIn(HttpSession session){
        String session_bid = (String)session.getAttribute("session_bid");
        if (session_bid!=null){
            return 1;
        } else {
            return -1;
        }
    }

    // Print HTML
    private void printPageHeader(PrintWriter out,HttpSession session) {
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
            if (type.equals("student")){
                out.println("<li class='active'><a href='/walter/servlet/WW_StudentHome'>Dashboard</a></li>");
        out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
            } else {
                out.println("<li class='active'><a href='/walter/servlet/WW_ProfHome'>Dashboard</a></li>");
                out.println("<li><a href='/walter/servlet/WW_CreateWaitlist'>Create Waitlist</a></li>");
        out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
            }
            out.println("<li><a href='/walter/servlet/WW_Logout'>Logout</a></li>");
        } else {
        out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
            out.println("<li><a href='/walter/servlet/WW_Signin'>Sign in</a></li>");
        }
        out.println("</ul>");
        out.println("<h3 class='text-muted'>Walter</h3>");
        out.println("</div>");
    }
    /**
    // Print this if user not logged in
    private void printPageHeader2(PrintWriter out) {
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
    out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
        out.println("<li><a href='/walter/servlet/WW_Signin'>Home</a></li>");
    out.println("<li class='active'><a href='WW_Logout'>Logout</a></li>");
    out.println("</ul>");
    out.println("<h3 class='text-muted'>Walter</h3>");
    out.println("</div>");
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
        try{
            doRequest(req,res);
        }
        catch (SQLException e) {
        }
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    {
        try{
            doRequest(req,res);
        }
        catch (SQLException e) {
        }
    }
    
}
