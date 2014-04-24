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
// ========================= VIEW WAITLIST PAGE ============================
// ==========================================================================

public class WW_ViewWaitlist extends HttpServlet {

  private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {

    res.setContentType("text/html; charset=UTF-8");
    PrintWriter out = res.getWriter();
    String selfUrl = res.encodeURL(req.getRequestURI());

    Connection con = null;
    try {
      printPageHeader(out);
      con = JoannaDSN.connect("jbi_db");
      String submit = escape(req.getParameter("submit"));
      if (submit!=null) {
    printForm(out,selfUrl);
    processForm(req, out, con);
      }
      else {
    printForm(out,selfUrl);
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
  
  private void printPageHeader(PrintWriter out) {
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Walter Waitlist</title>");
    out.println("<link rel='stylesheet' href='//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css'><script src='//code.jquery.com/jquery-1.10.2.js'></script><script src='//code.jquery.com/ui/1.10.4/jquery-ui.js'></script>");
    out.println("</head>");
    out.println("<body>");
  }
  
  // ========================================================================
  // PROCESS THE REQUEST DATA
  // ========================================================================
   
  private void processForm(HttpServletRequest req, PrintWriter out, Connection con)
    throws SQLException
  { 
    //Insert Into Waitlist (waitlist_id, student_bid, student_name, major_minor, student_class, rank, explanation) 
    String waitlist_id = req.getParameter("waitlist_id");
    
    try {
      printList( con,out,waitlist_id); 
      printEmail( con, out,waitlist_id);
    } catch (Exception e) {
      out.println("<p>Error:"+e);
    }    
  }
  
  // ========================================================================
  // HELPER METHOD: PRINTING THE WAITLISTS AND EMAILS
  // ========================================================================

  // Prints list of students on searched for waitlist
  private void printList(Connection con, PrintWriter out, String waitlist_id)
    throws SQLException
  {
    try {
      Statement query2 = con.createStatement();
      ResultSet result2 = query2.executeQuery("select * from Course where crn = "+waitlist_id);
      
      if (result2.next()) {
  out.println("<p>"+result2.getString("course_num")+" "+result2.getString("course_name")+"<br>"+
        result2.getString("kind")+" Limit: "+result2.getString("course_limit")+"<br>");
      }
      
      Statement query = con.createStatement();
      ResultSet result = query.executeQuery
  ("select * from Waitlist where waitlist_id = '"+waitlist_id+"' order by submitted_on asc");  
      out.println("<ul id='students'>");
      while (result.next()) {
  String student_bid = result.getString("student_bid");
  String student_name = result.getString("student_name");
  String major_minor  = result.getString("major_minor");
  String student_class = result.getString("student_class");
  String submitted_on  = result.getString("submitted_on");
  String rank = result.getString("rank");
  String explanation = result.getString("explanation");
  if(!result.wasNull()) {
    out.println("<li onclick=console.log('"+student_bid+"'); value='"+student_bid+"'>"+rank+
          " "+student_name+" "+major_minor+" "+student_class+" "+explanation+"</li>");
  } else {
    out.println("result was not null"); //when there's no result, print error statement
  }
      }
      out.println("</ul>");
      out.println("<script>$('#students').sortable();</script>");
    } catch (SQLException e) {
      out.println("<p>Error: "+e);
    }
  }

  // Prints emails of all students on waitlist
  private void printEmail(Connection con, PrintWriter out, String waitlist_id)
    throws SQLException
  {
    try {
      Statement query = con.createStatement();
      ResultSet result = query.executeQuery
  ("select email from Person,Waitlist where Person.bid=Waitlist.student_bid and Waitlist.waitlist_id ="+waitlist_id);
      out.println("Student Emails:<br>");
      while (result.next()) {
    out.println(result.getString("email")+";");
      } 
      out.println("<br>");
    }
    catch (SQLException e) {
      out.println("<p>Error: "+e);
    }
  }
  
  // ========================================================================
  // PRINT THE FORM
  // ========================================================================
  
  // Print the View Waitlists form
  private void printForm(PrintWriter out,String selfUrl)
    throws SQLException
  {
    out.println("<form method='post' action='"+selfUrl+"'><table cols='2'> <tr><td><p>Waitlist ID: <input required type='text' name='waitlist_id'></tr></td> <tr><td><input type='submit' name='submit' value='View Waitlist'></td></tr> </table> </form>");    
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
