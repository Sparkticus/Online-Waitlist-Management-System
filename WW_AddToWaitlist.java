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
// ========================= ADD TO WAITLIST PAGE ===========================
// ==========================================================================

public class WW_AddToWaitlist extends HttpServlet {

  private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {

    res.setContentType("text/html; charset=UTF-8");
    PrintWriter out = res.getWriter();
    String selfUrl = res.encodeURL(req.getRequestURI());
    HttpSession session = req.getSession(true);
    printPageHeader(out);
    
    // Control access to add
    String session_bid = (String)session.getAttribute("session_bid"); 
    if (session_bid == null){
      out.println("Please log in or create an account to add to waitlist.");
      out.println("<a href='/walter/servlet/WW_Home'>Click here to go to home page</a>");
    } else {
      Connection con = null;
    try {
      con = WalterDSN.connect("walter_db");
      String submit = escape(req.getParameter("crn_submit"));
    
      out.println("submit crn: "+submit);
        
      if (submit!=null) {
        processForm(session,req, out, con);
      } else {
        String previous_crn = (String)session.getAttribute("session_crn");
        String current_crn = req.getParameter("crn");
        if (!current_crn.equals(previous_crn)) {
            session.setAttribute("session_crn",current_crn);
        }
        String crn = (String)session.getAttribute("session_crn");
        
        // Print out session values
        Enumeration keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
          String key = (String)keys.nextElement();
          out.println(key + ": " + session.getValue(key) + "<br>");
        }
        printForm(out,selfUrl,crn);
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
    out.println("<h1><a href='/walter/servlet/WW_Home'>Walter Waitlist</a></h1>");
    out.println("<form method='post' action='/walter/servlet/WW_Logout'><button  type='submit'>Log out</button></form>");
    out.println("</head><hr>");
    out.println("<body>");
  }
  
  // ========================================================================
  // PROCESS THE REQUEST DATA
  // ========================================================================
   
  private void processForm(HttpSession session,HttpServletRequest req, PrintWriter out, Connection con)
    throws SQLException
  { 
    //Insert Into Waitlist (waitlist_id, student_bid, student_name, major_minor, student_class, rank, explanation) 
    String waitlist_id = (String)session.getAttribute("session_crn");
    String student_bid = (String)session.getAttribute("session_bid");
    String student_name =  (String)session.getAttribute("session_name");
    String major_minor =  (String)session.getAttribute("session_major_minor");
    String student_class =  (String)session.getAttribute("session_class");
      
    String explanation = req.getParameter("explanation");
    
    try {
      if(updateDatabase(con,out,waitlist_id,student_bid,student_name,major_minor,student_class,explanation)) {
        out.println("<p>Congratulations! You've successfully added yourself to a waitlist.");
      } else {
        out.println("<p>It looks like you're already on the waitlist!");
      }
    } catch (Exception e) {
      out.println("<p>Error:"+e);
    }
    
  }

  // ========================================================================
  // HELPER METHOD: UPDATE THE DATABASE
  // ========================================================================

  private boolean updateDatabase(Connection con, PrintWriter out, String waitlist_id,
                                 String student_bid, String student_name, String major_minor,
                                 String student_class, String explanation)
    throws SQLException
  {
    int result = insert(con, out, waitlist_id, student_bid, student_name, major_minor, student_class, explanation);
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
  private int insert(Connection con, PrintWriter out, String waitlist_id,
                     String student_bid, String student_name, String major_minor,
                     String student_class, String explanation)
    throws SQLException
  {
    try {
      PreparedStatement query1 = con.prepareStatement
        ("Insert into Waitlist (waitlist_id, student_bid, student_name, major_minor, student_class, rank, explanation) VALUES (?,?,?,?,?,0,?)");
      query1.setString(1, escape(waitlist_id));
      query1.setString(2, escape(student_bid));
      query1.setString(3, escape(student_name));
      query1.setString(4, escape(major_minor));
      query1.setString(5, escape(student_class));
      query1.setString(6, escape(explanation));
      int result1 = query1.executeUpdate();
      PreparedStatement query2 = con.prepareStatement
        ("Insert into On_Waitlist (bid,waitlist_id) Values (?,?)");
      query2.setString(1, escape(student_bid));
      query2.setString(2, escape(waitlist_id));
      int result2 = query2.executeUpdate();
      return result1;
    }
    catch (SQLException e) {
      if (e instanceof SQLIntegrityConstraintViolationException) {
        out.println("check line 177- lindsey");
      }  
      out.println("<p>Error: "+e);
      return -1; //error
    }
  }
  
  // ========================================================================
  // PRINT THE FORM
  // ========================================================================

  // Print the Waitlist form
  private void printForm(PrintWriter out,String selfUrl,String crn)
    throws SQLException
  {
    out.println("<html><head> <title>Walter Waitlist</title> </head> <body>"+
                "<form method='post' action='"+selfUrl+"'> <table cols='2'>"+
                "<tr><td><p> <textarea name='explanation' rows='3' cols='20'>Enter explanation here... </textarea> </tr></td> "+
                "<tr><td><p><button  type='submit' name='crn_submit' value="+crn+">Add to Waitlist</button></td></tr></table> </form> </body></html");
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
