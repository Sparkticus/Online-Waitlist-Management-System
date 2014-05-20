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
// ========================= CREATE ACCOUNT PAGE ============================
// ==========================================================================

public class WW_CreateAccount extends HttpServlet {
    
    private void doRequest(HttpServletRequest req, HttpServletResponse res)
  throws ServletException, IOException, SQLException
    {  
  res.setContentType("text/html; charset=UTF-8");
  PrintWriter out = res.getWriter();
  String selfUrl = res.encodeURL(req.getRequestURI());
  HttpSession session = req.getSession(true);
  String session_bid = (String)session.getAttribute("session_bid");
    
  printPageHeader(out,session);
        
  if (session_bid == null) {
      Connection con = null;
      try {
    con = WalterDSN.connect("walter_db");
    processForm(session,req, out, con, selfUrl); //this does the work
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
      out.println("<div class='alert alert-danger'><strong>Sorry!</strong> You must log out before creating a new account.</div>");
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

    // Check if a user is logged in
    private int isLoggedIn(HttpSession session){
        String session_bid = (String)session.getAttribute("session_bid");
        if (session_bid!=null){
            return 1;
        } else {
            return -1;
        }
    }

    // Print the page header
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
                out.println("<li><a href='/walter/servlet/WW_StudentHome'>Dashboard</a></li>");
    out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
            } else {
                out.println("<li><a href='/walter/servlet/WW_ProfHome'>Dashboard</a></li>");
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

    // Redirects the user
    public void redirect(PrintWriter out, String url)
  throws IOException, ServletException
    {
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
    
    // ========================================================================
    // CONTROL PANEL: WHICH BUTTON WAS PRESSED?
    // ========================================================================
    
    private void processForm(HttpSession session, HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
  throws SQLException
    {
  // Figure out which button was pressed
  String button = req.getParameter("submit");
  
  // Begin control panel
  if (button != null) {
      
      // STUDENT
      if (button.equals("STUDENT")) {
    printStudentForm(req, out, con, selfUrl);
      }
      // PROFESSOR
      if (button.equals("PROFESSOR")) {
    printProfessorForm(req, out, con, selfUrl);
      }
      // CREATE STUDENT ACCOUNT
      if (button.equals("Add Student")) {
    processStudent(session,req, out, con, selfUrl);
      }
      // CREATE PROFESSOR ACCOUNT
      if (button.equals("Add Professor")) {
    processProfessor(session,req, out, con, selfUrl);
      }
      
  } else {
      // No button was pressed
      printCreateAccount(req, out, con, selfUrl);
  }
  
    }
    
    // ========================================================================
    // PROCESS THE REQUEST DATA
    // ========================================================================
    
    // Process the form data submitted by a Student
    private void processStudent(HttpSession session, HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
  throws SQLException
    {
  // Process the form data submitted by a student
  String bid = req.getParameter("bid");
  String name = req.getParameter("name");
  String email = req.getParameter("email");
  String pass = req.getParameter("pass");
  String year = req.getParameter("year");
  String major_minor = req.getParameter("major_minor");
  
  try {
      if(updateStudent(con,out,bid,name,email,pass,year,major_minor)) {
    session.setAttribute("session_type", "student");
    session.setAttribute("session_bid", bid);
    session.setAttribute("session_name", name);
    session.setAttribute("session_class", year);
    session.setAttribute("session_major_minor", major_minor);
    printCreateAccount(req, out, con, selfUrl);
    redirect(out,"WW_StudentHome");
    
      } else {
    out.println("<div class='alert alert-danger'><b>Something went wrong!</b> It looks like you already have an account. Please sign in <a href='/walter/servlet/WW_Signin'>here</a>.</div>"); // Make sure account for duplicate accounts
      }
  } catch (Exception e) {
      printCreateAccount(req, out, con, selfUrl);
      out.println("<p>Something went wrong! Please try again.");
  }
    }
    
    // Process the form data submitted by a Professor
    private void processProfessor(HttpSession session, HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
  throws SQLException
    {
  // This is the professor data
  String bid = req.getParameter("bid");
  String name = req.getParameter("name");
  String email = req.getParameter("email");
  String pass = req.getParameter("pass");
  String department = req.getParameter("department");

  try {
      if(updateProfessor(con,out,bid,name,email,pass,department)) {
    printCreateAccount(req, out, con, selfUrl);
    session.setAttribute("session_bid", bid);
    session.setAttribute("session_name", name);
    session.setAttribute("session_email", email);
    session.setAttribute("session_department", department);
    session.setAttribute("session_type", "professor");
    redirect(out,"WW_ProfHome");
      } else {
    out.println("<div class='alert alert-danger'><b>Something went wrong!</b> It looks like you already have an account. Please sign in <a href='/walter/servlet/WW_Signin'>here</a>.</div>");
      }

  } catch (Exception e) {
      printCreateAccount(req, out, con, selfUrl);
      out.println("<p>Something went wrong! Please try again.");
  }
    }

    // ========================================================================
    // HELPER METHODS: UPDATE THE DATABASES
    // ========================================================================

    // Updates the Student database
    private boolean updateStudent(Connection con, PrintWriter out, String bid, String name,
          String email, String pass, String year,
          String major_minor)
  throws SQLException
    {
  int result = insertStudent(con,out,bid,name,email,pass,year,major_minor);
  if (result == 1) {
      return true;
  } else {
      return false;
  }
    }

    // Updates the Professor database
    private boolean updateProfessor(Connection con, PrintWriter out, String bid, String name,
            String email, String pass, String department)
  throws SQLException
    {
  int result = insertProfessor(con,out,bid,name,email,pass,department);
  if (result == 1) {
      return true;
  } else {
      return false;
  }
    }

    // ========================================================================
    // HELPER METHODS: ACTUAL INSERTING
    // ========================================================================

    // Insert new student into the database
    private int insertStudent(Connection con, PrintWriter out, String bid, String name,
            String email, String pass, String year,
            String major_minor)
  throws SQLException
    {
  try {
      PreparedStatement query1 = con.prepareStatement
    ("INSERT INTO Person (bid, name, email, usertype, pass) VALUES (?,?,?,?,?)");
      query1.setString(1, escape(bid));
      query1.setString(2, escape(name));
      query1.setString(3, escape(email));
      query1.setString(4, "s");
      query1.setString(5, escape(pass));
      int result1 = query1.executeUpdate();
      PreparedStatement query2 = con.prepareStatement
    ("INSERT INTO Student (bid, class_year, major_minor) VALUES (?,?,?)");
      query2.setString(1, escape(bid));
      query2.setString(2, escape(year));
      query2.setString(3, escape(major_minor));
      int result2 = query2.executeUpdate();
      return result2;
  }
  catch (SQLException e) {
      out.println("<p>Error: "+e);
      return -1; //-1 is fine because of the way updateStudent works
  }
    }

    // Insert new professor into the database
    private int insertProfessor(Connection con, PrintWriter out, String bid, String name,
        String email, String pass, String department)
  throws SQLException
    {
  try {
      PreparedStatement query1 = con.prepareStatement
    ("INSERT INTO Person (bid, name, email, usertype, pass) VALUES (?,?,?,?,?)");
      query1.setString(1, escape(bid));
      query1.setString(2, escape(name));
      query1.setString(3, escape(email));
      query1.setString(4, "p");
      query1.setString(5, escape(pass));
      int result1 = query1.executeUpdate();
      PreparedStatement query2 = con.prepareStatement
    ("INSERT INTO Professor (bid, department) VALUES (?,?)");
      query2.setString(1, escape(bid));
      query2.setString(2, escape(department));
      int result2 = query2.executeUpdate();
      return result2;
  }
  catch (SQLException e) {
      out.println("<p>Error: "+e);
      return -1;
  }
    }

    // ========================================================================
    // HELPER METHODS: PRINT THE VARIOUS FORMS
    // ========================================================================

    // Print the initial create account form
    private void printCreateAccount(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
  throws SQLException
    {
  out.println("<div class='jumbotron'>");
  out.println("<h3>To create your account, first let us know who you are!</h3><br>");
  out.println("<form method='post' action='"+selfUrl+"'>");
  out.println("<input type='submit' name='submit' value='STUDENT' class='btn btn-info'>&nbsp;&nbsp;&nbsp;");
  out.println("&nbsp;&nbsp;&nbsp;<input type='submit' name='submit' value='PROFESSOR' class='btn btn-info'>");
  out.println("</form>");
  out.println("</div>");
    }

    // Print the Student form
    private void printStudentForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
  throws SQLException
    {
  out.println("<form role='form' method='post' action='"+selfUrl+"'>");
  out.println("<div class='form-group'><label>Banner ID</label>");
  out.println("<input required type='text' name='bid' class='form-control'></div>");
  out.println("<div class='form-group'><label>Full name</label>");
  out.println("<input required type='text' name='name' class='form-control'></div>");
  out.println("<div class='form-group'><label>Email address</label>");
  out.println("<input required type='email' name='email' class='form-control'></div>");
  out.println("<div class='form-group'><label>Password</label>");
  out.println("<input required type='password' name='pass' class='form-control'></div>");
  out.println("<div class='form-group'><label>Class year</label>");
  out.println("<select required name='year' class='form-control'>");
  out.println("<option value=''></option>");
  out.println("<option value='2015'>2015");
  out.println("<option value='2016'>2016");
  out.println("<option value='2017'>2017");
  out.println("<option value='2018'>2018");
  out.println("</select></div>");
  out.println("<div class='form-group'><label>Major/Minor</label>");
  out.println("<input required type='text' name='major_minor' class='form-control'></div>");
  out.println("<input type='submit' name='submit' value='Add Student' class='btn btn-success'></form>");
    }

    // Print the Professor form
    private void printProfessorForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
  throws SQLException
    {
  out.println("<form role='form' method='post' action='"+selfUrl+"'>");
  out.println("<div class='form-group'><label>Banner ID</label>");
  out.println("<input required type='text' name='bid' class='form-control'></div>");
  out.println("<div class='form-group'><label>Full name</label>");
  out.println("<input required type='text' name='name' class='form-control'></div>");
  out.println("<div class='form-group'><label>Email address</label>");
  out.println("<input required type='email' name='email' class='form-control'></div>");
  out.println("<div class='form-group'><label>Password</label>");
  out.println("<input required type='password' name='pass' class='form-control'></div>");
  out.println("<div class='form-group'><label>Department</label>");
  out.println("<input required type='text' name='department' class='form-control'></div>");
  out.println("<input type='submit' name='submit' value='Add Professor' class='btn btn-success'></form>");
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
