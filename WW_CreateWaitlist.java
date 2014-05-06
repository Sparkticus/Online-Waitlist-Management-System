/**Joanna Bi and Lindsey Tang
   CS304: Final Project
   SPRING 2014 */

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.lang.*;
import org.apache.commons.lang.StringEscapeUtils; //for the string escaping

// ==========================================================================
// =========================== WALTER WAITLIST ==============================
// ========================= CREATE WAITLIST PAGE============================
// ==========================================================================

public class WW_CreateWaitlist extends HttpServlet {

  private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {
	
    res.setContentType("text/html; charset=UTF-8");
    
	PrintWriter out = res.getWriter();
	HttpSession session = req.getSession(true);
    
    
    String sessId = session.getId();
    
    printPageHeader(out,session);
	String user = (String)session.getAttribute("session_type");
	
	if (user.equals("professor")) {

	String selfUrl = res.encodeURL(req.getRequestURI());
        Enumeration keys = session.getAttributeNames();
        while (keys.hasMoreElements())
        {
        String key = (String)keys.nextElement();
        out.println(key + ": " + session.getValue(key) + "<br>");
        }
        
	
    Connection con = null;
    try {
      
      con = WalterDSN.connect("walter_db");
      String submit = escape(req.getParameter("submit"));
      if (submit!=null) {
          processForm(session,req, out, con);
      }
      printForm(out,selfUrl);
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
} else {
	out.println("You don't have permission to view this page");
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
        if (isLoggedIn(session)>0){
            String type = (String)session.getAttribute("session_type");
            if (type.equals("student")){
                out.println("<a href='/walter/servlet/WW_StudentHome'>Dashboard</a>");
            } else {
                out.println("<a href='/walter/servlet/WW_ProfHome'>Dashboard</a>");
                out.println("<a href='/walter/servlet/WW_CreateWaitlist'>Create Waitlist</a>");
            }
            out.println("<a href='/walter/servlet/WW_WaitlistSearch'>Browse</a>");
            out.println("<a href='/walter/servlet/WW_Logout'>Log out</a>");
        }
        out.println("<link rel='stylesheet' href='//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css'>");
        out.println("<script src='//code.jquery.com/jquery-1.10.2.js'></script>");
        out.println("<script src='//code.jquery.com/ui/1.10.4/jquery-ui.js'></script>");
        out.println("</head><hr>");
        out.println("<body>");
    }

  
  // ========================================================================
  // PROCESS THE REQUEST DATA
  // ========================================================================

  private void processForm(HttpSession session,HttpServletRequest req, PrintWriter out, Connection con)
    throws SQLException
  {
    // Get the request data
    String bid = (String)session.getAttribute("session_bid");
    String crn = req.getParameter("crn");
    String course_num = req.getParameter("course_num");
    String course_name = req.getParameter("course_name");
    String department = req.getParameter("department");
    String course_limit = req.getParameter("course_limit");
    String type = req.getParameter("type");
    
    try {
      if(updateDatabase(con,out,bid,crn,course_num,course_name, department, course_limit, type))
        {
    out.println("<p>Congratulations! You've successfully created a waitlist.");
  } else {
  out.println("<p>It looks like that waitlist already exists!");
      }
    } catch (Exception e) {
      out.println("<p>Error:"+e);
    }
    
  }
  
  // ========================================================================
  // UPDATE THE DATABASE
  // ========================================================================

  private boolean updateDatabase(Connection con, PrintWriter out, String bid,
         String crn, String course_num, String course_name,
         String department, String course_limit, String type)
    throws SQLException
  {
    int result = insert(con, out, bid, crn, course_num, course_name, department, course_limit, type);
    if (result == 1) {
      return true;
    } else {
      return false;
    }
  }
  
  // ========================================================================
  // HELPER METHOD: ACTUAL INSERTING
  // ========================================================================

  // Insert new waitlist into the database
  private int insert(Connection con, PrintWriter out, String bid, String crn,
         String course_num, String course_name, String department,
         String course_limit, String type)
    throws SQLException
  {
    try {
      PreparedStatement query1 = con.prepareStatement
        ("INSERT INTO Course ( crn, course_num,  department, course_limit , kind, course_name  ) VALUES (?,?,?,?,?,?)");
      query1.setString(1, escape(crn)); //wrap this into a for loop later?
      query1.setString(2, escape(course_num));
      query1.setString(3, escape(department));
      query1.setString(4, escape(course_limit));
      query1.setString(5, escape(type));
      query1.setString(6, escape(course_name));
      int result1 = query1.executeUpdate();
      
      PreparedStatement query2 = con.prepareStatement ("Insert INTO Created_Waitlist(bid,crn) values (?,?)");
      query2.setString(1, escape(bid));
      query2.setString(2, escape(crn));
      int result2 = query2.executeUpdate();
      return result1;
    }
    catch (SQLException e) {
      out.println("<p>Error: "+e);
      return -1; //error
    }
  }
  
  // ========================================================================
  // PRINT THE FORM
  // ========================================================================

  // Print the Waitlist form
  private void printForm(PrintWriter out,String selfUrl)
    throws SQLException
  {
    out.println("<html><head> <title>Walter Waitlist</title> </head> <body> <form method='post' action='"+selfUrl+"'> <table cols='2'> <tr><td>"+
"<tr><td><p>Course CRN: <input required type='text' name='crn'></tr></td> <tr><td><p>Course Number: <input required type='text' name='course_num'></tr></td> <tr><td><p>Course Name:<input required type='text' name='course_name'></td></tr><tr><td><p>Department: <input required type='text' name='department'></tr></td> <tr><td><p>Enrollment Limit: <input required type='text' name='course_limit'></tr></td> <tr><td><p>Type: <select required name='type'> <option value=''>Choose one</option> <option value='Lecture'>Lectue <option value='Lab'>Lab </select></td></tr> <tr><td><p><input type='submit' name='submit' value='Create Waitlist'></td></tr> </table> </form> </body></html");
  }
  
  // ========================================================================
  // HELPER METHOD: ESCAPING
  // ========================================================================
  
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
