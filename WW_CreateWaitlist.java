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
// ========================= CREATE WAITLIST PAGE ===========================
// ==========================================================================

public class WW_CreateWaitlist extends HttpServlet {

    private void doRequest(HttpServletRequest req, HttpServletResponse res)
  throws ServletException, IOException, SQLException {
  
  res.setContentType("text/html; charset=UTF-8");
    
  PrintWriter out = res.getWriter();
  HttpSession session = req.getSession(true);
    
  printPageHeader(out,session);
  String user = (String)session.getAttribute("session_type");
  
  if (user.equals("professor")) {

      String selfUrl = res.encodeURL(req.getRequestURI());
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
      out.println("<div class='alert alert-danger'><strong>Sorry!</strong> You don't have permission to view this page.</div>");
  }
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
  
    // Check if user is logged in
    private int isLoggedIn(HttpSession session){
        String session_bid = (String)session.getAttribute("session_bid");
        if (session_bid!=null){
            return 1;
        } else {
            return -1;
        }
    }

    // Print header and nav bar
    private void printPageHeader(PrintWriter out, HttpSession session) {
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
                out.println("<li><a href='/walter/servlet/WW_StudentHome'>Dashboard</a></li>");
    out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
            } else {
                out.println("<li><a href='/walter/servlet/WW_ProfHome'>Dashboard</a></li>");
                out.println("<li class='active'><a href='/walter/servlet/WW_CreateWaitlist'>Create Waitlist</a></li>");
    out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
            }
            out.println("<li><a href='/walter/servlet/WW_Logout'>Logout</a></li>");
        } else {
            out.println("<li><a href='/walter/servlet/WW_Signin'>Sign in</a></li>");
        }
        out.println("</ul>");
        out.println("<h3 class='text-muted'>Walter</h3>");
        out.println("</div>");
    }
  
    // ========================================================================
    // PROCESS THE REQUEST DATA
    // ========================================================================

    private void processForm(HttpSession session, HttpServletRequest req, PrintWriter out, Connection con)
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
      if(updateDatabase(con,out,bid,crn,course_num,course_name,department,course_limit,type)) {
    out.println("<div class='alert alert-success'><strong>Congratulations!</strong> You've successfully created a waitlist.</div>");
      } else {
    out.println("<div class='alert alert-danger'><strong>Oops!</strong> It looks like that waitlist already exists!</div>");
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
    private void printForm(PrintWriter out, String selfUrl)
  throws SQLException
    {
  out.println("<form role='form' method='post' action='"+selfUrl+"'>");
  out.println("<div class='form-group'><label>Course CRN</label>");
  out.println("<input required type='text' name='crn' class='form-control'></div>");
  out.println("<div class='form-group'><label>Course number</label>");
  out.println("<input required type='text' name='course_num' class='form-control'></div>");
  out.println("<div class='form-group'><label>Course name</label>");
  out.println("<input required type='text' name='course_name' class='form-control'></div>");
  out.println("<div class='form-group'><label>Department</label>");
  out.println("<input required type='text' name='department' class='form-control'></div>");
  out.println("<div class='form-group'><label>Enrollment limit</label>");
  out.println("<input required type='text' name='course_limit' class='form-control'></div>");
  out.println("<div class='form-group'><label>Class type</label>");
  out.println("<select required name='type' class='form-control'>");
  out.println("<option value=''></option>");
  out.println("<option value='Lecture'>Lecture");
  out.println("<option value='Lab'>Lab");
  out.println("</select></div>");
  out.println("<input type='submit' name='submit' value='Create Waitlist' class='btn btn-success'></form>");
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
