
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
        Connection con = null;
        try {
            printPageHeader(out);
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
                    
                    out.println("Please enter a valid username and password<br>");
                    printLoginForm(req, out, con,selfUrl);
                }
                
                Enumeration keys = session.getAttributeNames();
                while (keys.hasMoreElements())
                {
                    String key = (String)keys.nextElement();
                    out.println(key + ": " + session.getValue(key) + "<br>");
                }
                 out.println("<p><form action=/walter/servlet/WW_WaitlistSearch><button type=submit> Search for a waitlist </button></form>");
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
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            PreparedStatement query = con.prepareStatement
            ("Select * from Person where username=? and pass=?");
            query.setString(1, escape(username));
            query.setString(2, escape(password));
            ResultSet result = query.executeQuery();
            String type = "Student"; //all logins are students for now, until update person table with field prof/student boolean
            if (result.next()) {
                //String type = result.getString(type);
                if (type == "Student") {
                    String bid=result.getString("bid");
                    String name =result.getString("name");
                    String email=result.getString("email");
                    session.setAttribute("session_type", "student");
                    session.setAttribute("session_bid", bid);
                    session.setAttribute("session_name", name);
                    session.setAttribute("session_email",email);
                    getStudent(session,req,out,con,bid);
                } else {
                    String bid=result.getString("bid");
                    String name =result.getString("name");
                    String email=result.getString("email");
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
            }
        }
        catch (SQLException e) {
            out.println("<p>Error: "+e);
            out.print("No entry in Professor table.");
        }
    }
    
    private void printPageHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Walter Waitlist</title>");
        out.println("<h1><a href='/walter/servlet/WW_Home'>Walter Waitlist</a></h1>");
        out.println("</head><hr>");
        out.println("<body>");
    }
    
    
    private void printLoginForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
    {
        out.println("<form method='post' action='"+selfUrl+"'>");
        out.println("<table cols='2'>");
        out.println("<tr><td><p>Username <input required type='text' name='username'></tr></td>");
        out.println("<tr><td><p>Password <input required type='text' name='password'></tr></td>");
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
