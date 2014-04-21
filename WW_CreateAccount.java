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
    throws ServletException, IOException, SQLException {

    res.setContentType("text/html; charset=UTF-8");
    PrintWriter out = res.getWriter();
    String selfUrl = res.encodeURL(req.getRequestURI());

    Connection con = null;
    try {
      printPageHeader(out);
      con = JoannaDSN.connect("jbi_db");
      processForm(req, out, con, selfUrl); //this does the work
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
  
  private void printPageHeader(PrintWriter out) {
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Walter Waitlist</title>");
    out.println("</head>");
    out.println("<body>");
  }
  
  // ========================================================================
  // CONTROL PANEL: WHICH BUTTON WAS PRESSED?
  // ========================================================================

  private void processForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
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
	processStudent(req, out, con, selfUrl);
      }
      // CREATE PROFESSOR ACCOUNT
      if (button.equals("Add Professor")) {
	processProfessor(req, out, con, selfUrl);
      }

    } else {
      // No button was pressed
      printCreateAccount(req, out, con, selfUrl);
    }

  }

  // ========================================================================
  // PROCESS THE REQUEST DATA
  // ========================================================================

  // 
  private void processStudent(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    // Process the form data submitted by a student
    String bid = req.getParameter("bid");
    String name = req.getParameter("name");
    String email = req.getParameter("email");
    String usrname = req.getParameter("usrname");
    String pass = req.getParameter("pass");
    String year = req.getParameter("year");
    String major_minor = req.getParameter("major_minor");
    
    try {
      if(updateStudent(con,out,bid,name,email,usrname,pass,year,major_minor)) {
	printCreateAccount(req, out, con, selfUrl);
	out.println("<p>Congratulations! You've successfully created your account.");
	out.println("<p>To add yourself to a waitlist, click here."); //FIX LINK LATER
      } else {
	printCreateAccount(req, out, con, selfUrl);
	out.println("<p>It looks like you already have an account!"); // I NEED TO ADD A CASE FOR THIS ie: SQL QUERY IN UPDATE STUDENT TO CHECK IF DUPLICATE BEFORE INSERTING
      }
    } catch (Exception e) {
      printCreateAccount(req, out, con, selfUrl);
      out.println("<p>Something went wrong! Please try again.");
    }
  }

  // Process the form data submitted by a Professor
  private void processProfessor(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    // This is the professor data
    String bid = req.getParameter("bid");
    String name = req.getParameter("name");
    String email = req.getParameter("email");
    String usrname = req.getParameter("usrname");
    String pass = req.getParameter("pass");
    String department = req.getParameter("department");
    
    try {
      if(updateProfessor(con,out,bid,name,email,usrname,pass,department)) {
	printCreateAccount(req, out, con, selfUrl);
	out.println("<p>Congratulations! You've successfully created your account.");
	out.println("<p>To create a waitlist for your course, click here.");
      } else {
	printCreateAccount(req, out, con, selfUrl);
	out.println("<p>It looks like you already have an account!");
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
				String email, String usrname, String pass, String year,
				String major_minor)
    throws SQLException
  {
    int result = insertStudent(con,out,bid,name,email,usrname,pass,year,major_minor);
    if (result == 1) {
      return true;
    } else {
      return false;
    }
  }

  // Updates the Professor database
  private boolean updateProfessor(Connection con, PrintWriter out, String bid, String name,
				  String email, String usrname, String pass, String department)
    throws SQLException
  {
    int result = insertProfessor(con,out,bid,name,email,usrname,pass,department);
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
			     String email, String usrname, String pass, String year,
			     String major_minor)
    throws SQLException
  {
    try {
      PreparedStatement query1 = con.prepareStatement
	("INSERT INTO Person (bid, name, email, username, pass) VALUES (?,?,?,?,?)");
      query1.setString(1, escape(bid)); //wrap this into a for loop later?
      query1.setString(2, escape(name));
      query1.setString(3, escape(email));
      query1.setString(4, escape(usrname));
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
			       String email, String usrname, String pass, String department)
    throws SQLException
  {
    try {
      PreparedStatement query1 = con.prepareStatement
	("INSERT INTO Person (bid, name, email, username, pass) VALUES (?,?,?,?,?)");
      query1.setString(1, escape(bid)); //wrap this into a for loop later?
      query1.setString(2, escape(name));
      query1.setString(3, escape(email));
      query1.setString(4, escape(usrname));
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
    out.println("<form method='post' action='"+selfUrl+"'>");
    out.println("<p>Welcome to Walter Waitlist!</p><hr>");
    out.println("<p>Who are you?");
    out.println("<input type='submit' name='submit' value='STUDENT'>");
    out.println("<input type='submit' name='submit' value='PROFESSOR'>");
    out.println("</form>");
  }

  // Print the Student form
  private void printStudentForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    out.println("<form method='post' action='"+selfUrl+"'>");
    out.println("<p>Welcome to Walter Waitlist!</p><hr>");
    out.println("<table cols='2'>");
    out.println("<tr><td><p>Banner ID: <input required type='text' name='bid'></tr></td>");
    out.println("<tr><td><p>Full Name: <input required type='text' name='name'></tr></td>");
    out.println("<tr><td><p>Email Address: <input required type='text' name='email'></tr></td>");
    out.println("<tr><td><p>Username: <input required type='text' name='usrname'></tr></td>");
    out.println("<tr><td><p>Password: <input required type='text' name='pass'></tr></td>");
    out.println("<tr><td><p>Class Year:");
    out.println("<select required name='year'>");
    out.println("<option value=''>Choose one</option>");
    out.println("<option value='2015'>2015"); //ideally we can maybe dynamically generate these
    out.println("<option value='2016'>2016");
    out.println("<option value='2017'>2017");
    out.println("<option value='2018'>2018");
    out.println("</select></tr></td>");
    //fix so that it's a drop down menu in the future
    out.println("<tr><td><p>Major/Minor: <input required type='text' name='major_minor'></tr></td>");
    out.println("<tr><td><p><input type='submit' name='submit' value='Add Student'></td></tr>");
    out.println("</table>");
    out.println("</form>");
  }

  // Print the Professor form
  private void printProfessorForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    out.println("<form method='post' action='"+selfUrl+"'>");
    out.println("<p>Welcome to Walter Waitlist!</p><hr>");
    out.println("<table cols='2'>");
    out.println("<tr><td><p>Banner ID: <input required type='text' name='bid'></tr></td>");
    out.println("<tr><td><p>Full Name: <input required type='text' name='name'></tr></td>");
    out.println("<tr><td><p>Email Address: <input required type='text' name='email'></tr></td>");
    out.println("<tr><td><p>Username: <input required type='text' name='usrname'></tr></td>");
    out.println("<tr><td><p>Password: <input required type='text' name='pass'></tr></td>");
    //fix so that it's a drop down menu in the future
    out.println("<tr><td><p>Department: <input required type='text' name='department'></tr></td>");
    out.println("<tr><td><p><input type='submit' name='submit' value='Add Professor'></tr></td>");
    out.println("</table>");
    out.println("</form>");
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