
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.lang.*;
import org.apache.commons.lang.StringEscapeUtils; //for the string escaping


public class WW_Signin extends HttpServlet {
    
    private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {
    
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        String selfUrl = res.encodeURL(req.getRequestURI());
        HttpSession session = req.getSession(true);
        String sessId = session.getId();
        
        printPageHeader(out,session);
        Connection con = null;
        try {
            
            con = WalterDSN.connect("walter_db");
            String submit = req.getParameter("submit");
            if (submit==null){
                printLoginForm(req, out, con,selfUrl);
             
            } else {
                if (processLogin(session,req, out,con)>0){
                    String type = (String)session.getAttribute("session_type");
                    if (type.equals("student")) {
                        redirect(out,"WW_StudentHome");
                    } else {
                         redirect(out,"WW_ProfHome");
                    }
                }else{
                    
                    out.println("Please enter a valid email and password<br>");
                    printLoginForm(req, out, con,selfUrl);
                }
            }
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

    public void redirect(PrintWriter out,
                        String url)
    throws IOException, ServletException
    {
        //out.println("<meta http-equiv='refresh' content='1;url='"+url+"'>");
        out.println("<script type='text/javascript'>");
        out.println(" window.location.href = '"+url+"'");
        out.println(" </script>");
        out.println("<title>Page Redirection</title>");
        out.println("</head>");
        out.println(" <body>");
        out.println("If you are not redirected automatically, follow the <a href='"+url+"'>link</a><br>");
        out.println(" </body>");
        out.println(" </html>");
    }

    
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
            //String type = "Student"; //all logins are students for now, until update person table with field prof/student boolean
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
    
    private void getStudent(HttpSession session,HttpServletRequest req, PrintWriter out,Connection con, String bid)
    throws SQLException
    {
        try {
            
            PreparedStatement query = con.prepareStatement
            ("select * from Student where bid =?");
             query.setString(1, escape(bid));
             ResultSet result = query.executeQuery();
            if (result.next()) {
                    String class_year=result.getString("class_year");
                    String major_minor =result.getString("major_minor");
                    session.setAttribute("session_class", class_year);
                    session.setAttribute("session_major_minor",major_minor);
                }
        }
        catch (SQLException e) {
            out.println("<p>Error: "+e);
            out.print("No entry in Student table.");
        }
    }

    private void getProf(HttpSession session,HttpServletRequest req, PrintWriter out,Connection con, String bid)
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
                session.setAttribute("type","professor");
            }
        }
        catch (SQLException e) {
            out.println("<p>Error: "+e);
            out.print("No entry in Professor table.");
        }
    }
    
    private int isLoggedIn(HttpSession session){
        String session_bid = (String)session.getAttribute("session_bid");
        if (session_bid!=null){
            return 1;
        } else {
            return -1;
        }
    }
    private void printPageHeader(PrintWriter out,HttpSession session) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Walter Waitlist</title>");
        out.println("<h1><a href='/walter/servlet/WW_Signin'>Walter Waitlist</a></h1>");
        out.println("<a href='/walter/servlet/WW_WaitlistSearch'>Browse</a>");
        if (isLoggedIn(session)>0){
            String type = (String)session.getAttribute("session_type");
            if (type.equals("student")){
                out.println("<a href='/walter/servlet/WW_StudentHome'>Dashboard</a>");
            } else {
                out.println("<a href='/walter/servlet/WW_ProfHome'>Dashboard</a>");
            }
            out.println("<a href='/walter/servlet/WW_Logout'>Log out</a>");
        }
        out.println("<link rel='stylesheet' href='//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css'>");
        out.println("<script src='//code.jquery.com/jquery-1.10.2.js'></script>");
        out.println("<script src='//code.jquery.com/ui/1.10.4/jquery-ui.js'></script>");
        out.println("</head><hr>");
        out.println("<body>");
    }
    
    private void printLoginForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
    {
        out.println("<form method='post' action='"+selfUrl+"'>");
        out.println("<table cols='2'>");
        out.println("<tr><td><p>Email <input required type='email' name='email'></tr></td>");
        out.println("<tr><td><p>Password <input required type='password' name='password'></tr></td>");
        out.println("<tr><td><p><input type='submit' name='submit' value='Log In'></form>"+
                    "<form action=/walter/servlet/WW_CreateAccount><button type=submit>Sign up!</button></form></tr></td>");
        out.println("</table>");
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
