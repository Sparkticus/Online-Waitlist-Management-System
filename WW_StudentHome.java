import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.lang.*;
import org.apache.commons.lang.StringEscapeUtils; //for the string escaping


public class WW_StudentHome extends HttpServlet {
    
    private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {
        
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        String selfUrl = res.encodeURL(req.getRequestURI());
        HttpSession session = req.getSession(true);
        String sessId = session.getId();
        printPageHeader(out);
        
        String session_bid = (String)session.getAttribute("session_bid");
        
        if (session_bid == null){
            out.println("Please log in or create an account.");
            out.println("<a href='/ltang/servlet/WW_Signin'>Click here to sign in</a>");
        } else {
            Connection con = null;
            try {
                
                con = ltang_DSN.connect("ltang_db");
                String remove_crn =req.getParameter("remove_crn");
                
                if (remove_crn != null){
        
                    processRemove(req, out, con,session_bid,remove_crn);
                }
                Enumeration keys = session.getAttributeNames();
                while (keys.hasMoreElements())
                {
                    String key = (String)keys.nextElement();
                    out.println(key + ": " + session.getValue(key) + "<br>");
                }
                
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
        out.println("</body>");
        out.println("</html>");
    }
    
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
    

    private void getStudentActivity(String student_bid, PrintWriter out, Connection con)
    throws SQLException
    {
        PreparedStatement query_student = con.prepareStatement
        ("select * from Waitlist where student_bid=?");
        query_student.setString(1, escape(student_bid));
        ResultSet result_student = query_student.executeQuery();
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
                out.println("waitlist_id: "+ waitlist_id);
                out.println(" rank: "+ rank);
                out.println(" course_name: "+ course_name);
                out.println(" course_num: "+ course_num);
                out.println(" department: "+ department);
                out.println(" course_limit: "+ course_limit);
                out.println(" kind: "+ kind);
                out.println("<button onclick=remove_student('"+waitlist_id+"')>Remove</button>");
                out.println("<br>");
                }
        }
    }

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
    
    private void printPageHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Walter Waitlist</title>");
        out.println("<h1><a href='/ltang/servlet/WW_Signin'>Walter Waitlist</a></h1>");
        out.println("<form method='post' action='/ltang/servlet/WW_Logout'><button  type='submit'>Log out</button></form>");
        out.println("<form action=/ltang/servlet/WW_WaitlistSearch><button type=submit> Browse </button></form>");
        out.println("<link rel='stylesheet' href='//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css'>");
        out.println("<script src='//code.jquery.com/jquery-1.10.2.js'></script>");
        out.println("<script src='//code.jquery.com/ui/1.10.4/jquery-ui.js'></script>");
        out.println("</head><hr>");
        out.println("<body>");
    }
    
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
