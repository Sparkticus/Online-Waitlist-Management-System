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
    
  printPageHeader(out,session);

  String session_type = (String)session.getAttribute("session_type");
        
  if (session_type==null){
      out.println("<div class='alert alert-danger'><strong>Sorry!</strong> You don't have permission to add yourself to a waitlist. Please <a href='/walter/servlet/WW_Signin'>login or create an account</a> to access this feature.</div>");
  } else {
      if (!session_type.equals("student")){
    out.println("<div class='alert alert-danger'><strong>Sorry!</strong> You don't have permission to add yourself to a waitlist.</div>");
      } else {
    Connection con = null;
    try {
        con = WalterDSN.connect("walter_db");
        String submit = escape(req.getParameter("crn_submit"));
                
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
      out.println("<p class='lead' >Add yourself to this waitlist: "+crn+"</p>");
      Enumeration keys = session.getAttributeNames();
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
            out.println("<li><a href='WW_Logout'>Logout</a></li>");
        } else {
      out.println("<li><a href='WW_WaitlistSearch'>Browse</a></li>");
            out.println("<li><a href='/walter/servlet/WW_Signin'>Sign in</a></li>");
        }
        out.println("</ul>");
        out.println("<h3 class='text-muted'>Walter</h3>");
        out.println("</div>");
    }
  
    // ========================================================================
    // PROCESS THE REQUEST DATA
    // ========================================================================
   
    private void processForm(HttpSession session,HttpServletRequest req, PrintWriter out, Connection con)
  throws SQLException
    {
  String waitlist_id = (String)session.getAttribute("session_crn");
  String student_bid = (String)session.getAttribute("session_bid");
  String student_name =  (String)session.getAttribute("session_name");
  String major_minor =  (String)session.getAttribute("session_major_minor");
  String student_class =  (String)session.getAttribute("session_class");
      
  String explanation = req.getParameter("explanation");
    
  try {
      int rank =insertStudent(con,out,waitlist_id,student_bid,student_name,major_minor,student_class,explanation);
      if(rank>0) {
    out.println("<div class='alert alert-success'><strong>Congratulations!</strong> You've successfully added yourself to the waitlist for course crn "+waitlist_id+". You are number <b>"+rank+"</b> on the waitlist.</div>");
      }
  }
  catch (SQLException e) {
      out.println("<p>Error: "+e);
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
  int result = insertStudent(con, out, waitlist_id, student_bid, student_name, major_minor, student_class, explanation);
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
    private int insertStudent(Connection con, PrintWriter out, String waitlist_id,
            String student_bid, String student_name, String major_minor,
            String student_class, String explanation)
  throws SQLException
    {
  try {
      PreparedStatement query_max_rank = con.prepareStatement
    ("select max(rank) from Waitlist where waitlist_id=?");
      query_max_rank.setString(1, escape(waitlist_id));
      ResultSet result = query_max_rank.executeQuery();
      Integer rank =1;
      if (result.next()) {
    String max_rank = result.getString("max(rank)");
    if (max_rank!=null) {
        rank = Integer.parseInt(max_rank)+1;
    }
      }
      PreparedStatement query1 = con.prepareStatement
                ("Insert into Waitlist (waitlist_id, student_bid, student_name, major_minor, student_class, rank, explanation) VALUES (?,?,?,?,?,?,?)");
      query1.setString(1, escape(waitlist_id));
      query1.setString(2, escape(student_bid));
      query1.setString(3, escape(student_name));
      query1.setString(4, escape(major_minor));
      query1.setString(5, escape(student_class));
      query1.setString(6, escape(rank.toString()));
            query1.setString(7, escape(explanation));            
      query1.executeUpdate();
      return rank;
  }
  catch (SQLException e) {
      if (e instanceof SQLIntegrityConstraintViolationException) {
    out.println("<div class='alert alert-warning'>It looks like you're already on the waitlist for <b>course crn "+waitlist_id+"</b>!</div>");
      }
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
  out.println("<form method='post' action='"+selfUrl+"'>");
  out.println("<div class='form-group'><label>Explanation</label>");
  out.println("<textarea class='form-control' rows='3' name='explanation' placeholder='Enter explanation here...'></textarea></div>");
  out.println("<input type='submit' name='crn_submit' value='Add to Waitlist' class='btn btn-success'></form>");
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
