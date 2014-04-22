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
// ========================= WAITLIST SEARCH PAGE ===========================
// ==========================================================================

public class WW_WaitlistSearch extends HttpServlet {

  private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {

    res.setContentType("text/html; charset=UTF-8");
    PrintWriter out = res.getWriter();
    String selfUrl = res.encodeURL(req.getRequestURI());

    Connection con = null;
    try {
      printPageHeader(out);
      con = JoannaDSN.connect("jbi_db");
      printSearchField(req,out,con,selfUrl); //always print the search
      processForm(req,out,con,selfUrl); //this does all the work
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
  // CONTROL PANEL: PRINT ALL WAITLISTS OR PRINT SEARCH RESULTS
  // ========================================================================

  private void processForm(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    // Figure out if user is searching
    String button = req.getParameter("submit");

    // User is searching; process search
    if (button != null) {
      processSearch(req,out,con,selfUrl);
    }
    // User is not searching, print all waitlists
    else {
      printWaitlists(req,out,con,selfUrl);
    }
  }

  // ========================================================================
  // CONTROL PANEL: PROCESS THE SEARCH
  // ========================================================================

  // Processes the search form data and returns corresponding outputs
  private void processSearch(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    // Process the form data submitted for the search
    String field = req.getParameter("field");
    String input = req.getParameter("search_term");
    
    try {
      if(foundResults(req,con,out,field,input)) {
  // Do nothing; foundResults takes care of everything
      } else {
  out.println("Oops! It looks like we didn't find any results.");
      } 
    } catch (Exception e) {
      out.println("Error: "+e);
    }
  }

  // ========================================================================
  // CONTROL PANEL: FINDING AND PRINTING THE SEARCH RESULTS
  // ========================================================================

  // DETERMINE IF RESULTS IN SEARCH
  private boolean foundResults(HttpServletRequest req, Connection con, PrintWriter out,
             String field, String input)
    throws SQLException
  {
    try {
      PreparedStatement query = con.prepareStatement
  ("SELECT Course.crn, course_num, course_name, department, course_limit, kind, name, email "+
   "FROM Course, Created_Waitlist, Person "+
   "WHERE Course.crn=Created_Waitlist.crn and Person.bid=Created_Waitlist.bid "+
   "AND " + escape(field) + " LIKE ?"); //IS THIS OKAY. SQL INJECTION??? PROBABLY T__T [ASK]
      query.setString(1, "%"+escape(input)+"%"); //add wildcard
      ResultSet result = query.executeQuery();
      
      // Print the results and return boolean
      if(!result.wasNull()) {
  /**SOMETHING IS WRONG
     always returns true if use wasNull()
     but can't use next() because then can't get first result in resultset
     BUT next() correctly returns the boolean values
  */
  printSearchResults(req, out, con, result); //works ish; need to fix; subtle bug 
        return true; //yes results found
      } else {
  return false; //no results found
      }
    } catch (Exception e) { // something went wrong
      out.println("Error: "+e);
      return false;
    } 
  }

  // ========================================================================
  // HELPER METHODS: PRINT THE VARIOUS FORMS
  // ========================================================================

  // Print the search field form
  private void printSearchField(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    out.println("<form method='post' action='"+selfUrl+"'>");
    out.println("<p>Welcome to Walter Waitlist!</p><hr>");
    out.println("<p>Search the waitlist database by: ");
    out.println("<select required name='field'>");
    out.println("<option value=''>Choose one</option>");
    out.println("<option value='department'>Department");
    out.println("<option value='name'>Professor");
    out.println("<option value='course_name'>Course Name");
    out.println("<option value='course_num'>Course Number");
    out.println("<option value='crn'>CRN");
    out.println("<input required type='text' name='search_term'>");
    out.println("<input type='submit' name='submit' value='Go!'>");
    out.println("</form>");
  }

  // Print the search results
  private void printSearchResults(HttpServletRequest req, PrintWriter out, Connection con, ResultSet rs)
    throws SQLException
  {
    out.println("Here are your search results:<ul>");
    while(rs.next()) {
      out.println("<li>"+rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+
      " "+rs.getString(5)+" "+rs.getString(6)+" "+rs.getString(7)+" "+rs.getString(8)+"</li>");
    }
    out.println("</ul>");
  }

  // Print all Waitlists in the database
  private void  printWaitlists(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
  {
    out.println("List of courses with waitlists:<ul>");
    Statement waitlists = con.createStatement();
    ResultSet rs = waitlists.executeQuery
      ("SELECT Course.crn, course_num, course_name, department, course_limit, kind, name, email "+
       "FROM Course, Created_Waitlist, Person "+
       "WHERE Course.crn=Created_Waitlist.crn and Person.bid=Created_Waitlist.bid");
    while(rs.next()) {
      out.println("<li>"+rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+
      " "+rs.getString(5)+" "+rs.getString(6)+" "+rs.getString(7)+" "+rs.getString(8)+"</li>");
    }
    out.println("</ul>");
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