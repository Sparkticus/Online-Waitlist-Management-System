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
  throws ServletException, IOException, SQLException
    {
  res.setContentType("text/html; charset=UTF-8");
  PrintWriter out = res.getWriter();
  String selfUrl = res.encodeURL(req.getRequestURI());
  HttpSession session = req.getSession(true);

  printPageHeader(out,session);
  Connection con = null;

  try { 
      con = WalterDSN.connect("walter_db");
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

    // Check if the user is logged in
    private int isLoggedIn(HttpSession session){
        String session_bid = (String)session.getAttribute("session_bid");
        if (session_bid!=null){
            return 1;
        } else {
            return -1;
        }
    }
    
    // Print the page header
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
    out.println("<li class='active'><a href='WW_WaitlistSearch'>Browse</a></li>");
            } else {
                out.println("<li><a href='/walter/servlet/WW_ProfHome'>Dashboard</a></li>");
                out.println("<li><a href='/walter/servlet/WW_CreateWaitlist'>Create Waitlist</a></li>");
    out.println("<li class='active'><a href='WW_WaitlistSearch'>Browse</a></li>");
            }
            out.println("<li><a href='WW_Logout'>Logout</a></li>");
        } else {
      out.println("<li class='active'><a href='WW_WaitlistSearch'>Browse</a></li>");
            out.println("<li><a href='/walter/servlet/WW_Signin'>Sign in</a></li>");
        }
        out.println("</ul>");
        out.println("<h3 class='text-muted'>Walter</h3>");
        out.println("</div>");
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
      if(foundResults(req,con,out,selfUrl,field,input)) {
    // Do nothing; foundResults takes care of everything
      } else {
    out.println("<div class='alert alert-info'><strong>Sorry!</strong> It looks like we didn't find any results.</div>");
      } 
  } catch (Exception e) {
      out.println("Error: "+e);
  }
    }

    // ========================================================================
    // CONTROL PANEL: FINDING AND PRINTING THE SEARCH RESULTS
    // ========================================================================

    // DETERMINE IF RESULTS IN SEARCH
    private boolean foundResults(HttpServletRequest req, Connection con, PrintWriter out, String selfUrl,
         String field, String input)
  throws SQLException
    {
  try {
      PreparedStatement query = con.prepareStatement
    ("SELECT count(*) FROM Course, Created_Waitlist, Person "+
     "WHERE Course.crn=Created_Waitlist.crn and Person.bid=Created_Waitlist.bid "+
     "AND " + escape(field) + " LIKE ?");
      query.setString(1, "%"+escape(input)+"%"); //add wildcard
      ResultSet result = query.executeQuery();

      // Find out how many results
      int count = 0; //initialize count
      if(result.next()) {
    count = result.getInt(1);
      }
      // Print the results and return boolean
      if (count != 0) {
    printSearchResults(req, out, con, selfUrl, count, field, input);
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
  out.println("<form class='form-inline' method='post' action='"+selfUrl+"'>");
  out.println("<div class='panel panel-primary'>"+
        "<div class='panel-heading'><h3 class='panel-title'>Search the waitlist database:</h3></div>"+
        "<div class='panel-body'>");
  out.println("<div class='form-group'>");
  out.println("<select class='form-control' required name='field'>");
  out.println("<option value=''>Choose type</option>");
  out.println("<option value='department'>Department");
  out.println("<option value='name'>Professor");
  out.println("<option value='course_name'>Course Name");
  out.println("<option value='course_num'>Course Number");
  out.println("<option value='crn'>CRN");
  out.println("</select></div> ");
  out.println("<div class='form-group'>");
  out.println("<input required type='text' name='search_term' placeholder='Enter search term...' class='form-control'>");
  out.println("</div> ");
  out.println("<input type='submit' name='submit' value='Go!' class='btn btn-success'>");
  out.println("</form>");
  out.println("</div></div>");
    }

    // Print the search results
    private void printSearchResults(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl,
            int count, String field, String input)
  throws SQLException
    {
  out.println("<div class='alert alert-info'>We found <strong>"+count+"</strong> resuts in your search!</div>");
  PreparedStatement query = con.prepareStatement
      ("SELECT Course.crn, course_num, course_name, department, course_limit, kind, name, email "+
       "FROM Course, Created_Waitlist, Person "+
       "WHERE Course.crn=Created_Waitlist.crn and Person.bid=Created_Waitlist.bid "+
       "AND " + escape(field) + " LIKE ?");
  query.setString(1, "%"+escape(input)+"%"); //add wildcard
  ResultSet rs = query.executeQuery();
  // Print the results
  out.println("<div class='panel panel-primary'>"+
        "<div class='panel-heading'><h3 class='panel-title'>List of courses with waitlists:</h3></div>"+
        "<div class='panel-body'>");
  out.println("<table class='table table-hover'><thead><tr><th>CRN</th><th>Course Number</th><th>Course Name</th><th>Department</th><th>Type</th><th>Course Limit</th><th>Instructor</th><th>Instructor Email</th><th>Add</th></thead><tbody>");
  while(rs.next()) {
      out.println("<tr><td>"+rs.getString(1)+"</td>"); //crn
      out.println("<td>"+rs.getString(2)+"</td>"); //course number
      out.println("<td>"+rs.getString(3)+"</td>"); //course name
      out.println("<td>"+rs.getString(4)+"</td>"); //department
      out.println("<td>"+rs.getString(6)+"</td>"); //type
      out.println("<td>"+rs.getString(5)+"</td>"); //course limit
      out.println("<td>"+rs.getString(7)+"</td>"); //instructor
      out.println("<td>"+rs.getString(8)+"</td>"); //instructor email
      out.println("<td><form action=/walter/servlet/WW_AddToWaitlist><button class='btn btn-xs btn-success' type='submit' name='crn' value="+rs.getString(1)+">Add</button></td>"); //add to waitlist option
      out.println("</tr>");
  }
  out.println("</tbody></table>"); //close table
  out.println("</div></div>"); // close panel div
    }
    // Print all Waitlists in the database
    private void  printWaitlists(HttpServletRequest req, PrintWriter out, Connection con, String selfUrl)
  throws SQLException
    {
  Statement query = con.createStatement();
  ResultSet rs = query.executeQuery
      ("SELECT Course.crn, course_num, course_name, department, course_limit, kind, name, email "+
       "FROM Course, Created_Waitlist, Person "+
       "WHERE Course.crn=Created_Waitlist.crn and Person.bid=Created_Waitlist.bid");
  // Print the results
  out.println("<div class='panel panel-primary'>"+
        "<div class='panel-heading'><h3 class='panel-title'>List of courses with waitlists:</h3></div>"+
        "<div class='panel-body'>");
  out.println("<table class='table table-hover'><thead><tr><th>CRN</th><th>Course Number</th><th>Course Name</th><th>Department</th><th>Type</th><th>Course Limit</th><th>Instructor</th><th>Instructor Email</th><th>Add</th></thead><tbody>");
  while(rs.next()) {
      out.println("<tr><td>"+rs.getString(1)+"</td>"); //crn
      out.println("<td>"+rs.getString(2)+"</td>"); //course #
      out.println("<td>"+rs.getString(3)+"</td>"); //course name
      out.println("<td>"+rs.getString(4)+"</td>"); //department
      out.println("<td>"+rs.getString(6)+"</td>"); //type
      out.println("<td>"+rs.getString(5)+"</td>"); //course limit
      out.println("<td>"+rs.getString(7)+"</td>"); //instructor
      out.println("<td>"+rs.getString(8)+"</td>"); //instructor email
      out.println("<td><form action=/walter/servlet/WW_AddToWaitlist><button class='btn btn-xs btn-success' type='submit' name='crn' value="+rs.getString(1)+">Add</button></td>"); //Add to waitlist option
      out.println("</tr>");
  }
  out.println("</tbody></table>"); //close table
  out.println("</div></div>"); // close panel div
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
