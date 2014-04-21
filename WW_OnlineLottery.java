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

public class WW_OnlineLottery extends HttpServlet {

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
  // UPDATE THE DATABASES
  // ========================================================================

  // STRATEGY: control panel to choose which helper update table to execute
  // For now just this to make sure it works:
  // ========================================================================
  // HELPER METHODS: CONTROL PANEL
  // ========================================================================

  // Insert new student into the database
  private void printList(Connection con, PrintWriter out, String waitlist_id)
    throws SQLException
  {
    try {
	Statement query2 = con.createStatement();
        ResultSet result2 = query2.executeQuery("select * from Course where crn = "+waitlist_id);
	
	if (result2.next()) {
		out.println("<p>"+result2.getString("course_num")+" "+result2.getString("course_name")+"<br>"+result2.getString("kind")+" Limit: "+result2.getString("course_limit")+"<br>");
	}

	Statement query = con.createStatement();
	ResultSet result = query.executeQuery
         ("select * from Waitlist where waitlist_id = '"+waitlist_id+"' order by submitted_on asc");  
 	out.println("<ol>");
		while (result.next()) {
			String student_bid = result.getString("student_bid");
			String student_name = result.getString("student_name");
			String major_minor  = result.getString("major_minor");
			String student_class = result.getString("student_class");
			String submitted_on  = result.getString("submitted_on");
			String rank = result.getString("rank");
			String explanation = result.getString("explanation");
		if(!result.wasNull()) {
                	out.println("<li onclick=console.log('"+student_bid+"'); value='"+student_bid+"'>"+rank+" "+student_name+" "+major_minor+" "+student_class+" "+explanation+"</li>");
		} else {
                    out.println("result was not null"); //when there's no result,print error statement
                }
 	}
	out.println("</ol>");
	}

    catch (SQLException e) {
      out.println("<p>Error: "+e);
    }
  }

 private void printEmail(Connection con, PrintWriter out, String waitlist_id)
    throws SQLException
  {
    try {
        Statement query = con.createStatement();
        ResultSet result = query.executeQuery("select email from Person,Waitlist where Person.bid=Waitlist.student_bid and Waitlist.waitlist_id ="+waitlist_id);
	
	out.println("Emails of students on waitlist:<br><ul>");
        while (result.next()) {
                out.println("<li>"+result.getString("email")+"</li>");
        	}	
	out.println("</ul>");
	}
 
    catch (SQLException e) {
      out.println("<p>Error: "+e);
    }
  }

  // ========================================================================
  // PRINT THE VARIOUS FORMS
  // ========================================================================

  // Print the Student form
  private void printForm(PrintWriter out,String selfUrl)
    throws SQLException
  {
	out.println("<html><head><h1>View Waitlist</h1></head><body><form method='post' action='"+selfUrl+"'><table cols='2'> <tr><td><p>Waitlist ID: <input required type='text' name='waitlist_id'></tr></td> <tr><td><input type='submit' name='submit' value='View Waitlist'></td></tr> </table> </form>");

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
