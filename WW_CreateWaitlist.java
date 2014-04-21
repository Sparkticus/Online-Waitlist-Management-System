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
// ==========================================================================

public class WW_CreateWaitlist extends HttpServlet {

  private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {

    res.setContentType("text/html; charset=UTF-8");
    PrintWriter out = res.getWriter();
    String selfUrl = res.encodeURL(req.getRequestURI());

    Connection con = null;
    try {
      printPageHeader(out);
      con = ltang_DSN.connect("ltang_db");
       String submit = escape(req.getParameter("submit"));
	if (submit!=null) {
	processForm(req, out, con);
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
  // PROCESS THE REQUEST DATA
  // ========================================================================

  private void processForm(HttpServletRequest req, PrintWriter out, Connection con)
    throws SQLException
  {
    // IDEA: Have a big control method where you first determine which submit button
    // was pressed. Depending on that, you enter if/else statements to find out what
    // data to grab and then call the appropriate upate method
    // Perhaps we could create a helper method 'update person' to call within our
    // update student and update professor? Just some thoughts.
 
    String bid = req.getParameter("bid");
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
  // UPDATE THE DATABASES
  // ========================================================================

  // STRATEGY: control panel to choose which helper update table to execute
  // For now just this to make sure it works:
  private boolean updateDatabase(Connection con, PrintWriter out, String bid, String crn, String course_name,String course_num, String department, String course_limit, String type)
    throws SQLException
  {
    int result = insert(con,out,bid,crn,course_num,course_name, department, course_limit, type);
    if (result == 1) {
      return true;
    } else {
      return false;
    }
  }

  // ========================================================================
  // HELPER METHODS: CONTROL PANEL
  // ========================================================================

  // Insert new student into the database
  private int insert(Connection con, PrintWriter out, String bid, String crn, String course_num, String course_name, String department, String course_limit, String type)
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
        /*
        if (type.equals("class")) {
            PreparedStatement query3 = con.prepareStatement ("Insert INTO Class(crn) values (?)");
            query3.setString(1, escape(crn));
            int result3 = query3.executeUpdate();
            
        } else
       {     PreparedStatement query4 = con.prepareStatement ("Insert Into Lab (crn) values (?)");
            query4.setString(1, escape(crn));
            int result4 = query4.executeUpdate();
        }
*/
      return result1;
      }
    catch (SQLException e) {
      out.println("<p>Error: "+e);
      return -1; //error
    }
  }


  // ========================================================================
  // PRINT THE VARIOUS FORMS
  // ========================================================================

  // Print the Student form
  private void printForm(PrintWriter out,String selfUrl)
    throws SQLException
  {
out.println("<html><head> <title>Walter Waitlist</title> </head> <body> <form method='post' action='"+selfUrl+"'> <table cols='2'> <tr><td><p>Professor Banner ID: <input required type='text' name='bid'></tr></td> <tr><td><p>Course CRN: <input required type='text' name='crn'></tr></td> <tr><td><p>Course Number: <input required type='text' name='course_num'></tr></td> <tr><td><p>Course Name:<input required type='text' name='course_name'></td></tr><tr><td><p>Department: <input required type='text' name='department'></tr></td> <tr><td><p>Enrollment Limit: <input required type='text' name='course_limit'></tr></td> <tr><td><p>Type: <select required name='type'> <option value=''>Choose one</option> <option value='Lecture'>Lectue <option value='Lab'>Lab </select></td></tr> <tr><td><p><input type='submit' name='submit' value='Create Waitlist'></td></tr> </table> </form> </body></html");
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
