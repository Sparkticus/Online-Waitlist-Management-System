/**Joanna Bi and Lindsey Tang
   CS304: Final Project
   SPRING 2014 */

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.lang.*;
//import org.apache.commons.lang.StringEscapeUtils; //for the string escaping
import org.apache.commons.lang.*;

// ==========================================================================
// =========================== WALTER WAITLIST ==============================
// ========================= VIEW WAITLIST PAGE ============================
// ==========================================================================

public class WW_ProfHome extends HttpServlet {

  private void doRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, SQLException {

    res.setContentType("text/html; charset=UTF-8");
    PrintWriter out = res.getWriter();
    String selfUrl = res.encodeURL(req.getRequestURI());
    HttpSession session = req.getSession(true);
    String sessId = session.getId();
    Connection con = null;
        
    printPageHeader(out,session);
        
    String session_bid = (String)session.getAttribute("session_bid");
    String session_type =(String)session.getAttribute("session_type");
        //out.println(session_bid);
       // out.println(session_type);
        
       if (session_bid == null || !session_type.equals("professor")){
           out.println("Please log in or create an account.");
           out.println("<a href='/walter/servlet/WW_Signin'>Click here to sign in</a>");
        } else {
            try {
            con = WalterDSN.connect("walter_db");

            String remove_bid = req.getParameter("remove_bid");
            String remove_waitlist = req.getParameter("remove_waitlist");
            String current_crn =req.getParameter("view_waitlist");
                
            String order = req.getParameter("new_order");
            String previous_crn = (String)session.getAttribute("session_crn");
            //String current_crn = req.getParameter("waitlist_id");
                
            if (remove_bid!=null){
                removeStudent(req, out, con,remove_bid,previous_crn);
            }
            if (remove_waitlist!=null){
                removeWaitlist(req, out, con,remove_waitlist);
            }
            if (order!=null && previous_crn!=null){
                String [] sort = StringUtils.split(order,',');
                processSort(req, out, con,sort,previous_crn);
                String not_using_auto_crn = getProfActivity(session_bid, out, con, selfUrl);
            } else {
                String auto_crn = getProfActivity(session_bid, out, con, selfUrl);
            
            //out.println("prof crn: "+auto_crn);
            //out.println(previous_crn);
            //out.println(current_crn);
                
            if (current_crn==null){
                    current_crn=auto_crn;
                }
                
            }
            if (current_crn!=previous_crn) {
                if (StringUtils.length(current_crn)>3){
                    session.setAttribute("session_crn",current_crn);
                 } else {
                    session.setAttribute("session_crn",previous_crn);
                }
            }
            
            out.println("<b>View Waitlist</b><br>");
            Enumeration keys = session.getAttributeNames();
            while (keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                out.println(key + ": " + session.getValue(key) + "<br>");
            }
            
            String waitlist_id = (String)session.getAttribute("session_crn");
            //out.println("<br>crn used:"+waitlist_id);
                
            if (waitlist_id!=null) {
                processForm(req, out, con, waitlist_id, selfUrl);
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
    
    private String getProfActivity(String prof_bid, PrintWriter out, Connection con, String selfUrl)
    throws SQLException
    {
        out.println("<b>Professor's Created Waitlist</b><br>");
        PreparedStatement query_student = con.prepareStatement
        ("select * from Created_Waitlist where bid=?");
        query_student.setString(1, escape(prof_bid));
        ResultSet result_student = query_student.executeQuery();
        //Int count = 0;
        String waitlist_id ="";
        while (result_student.next()) {
            waitlist_id=result_student.getString("crn");
            PreparedStatement query_waitlist = con.prepareStatement
            ("select * from Course where crn=?");
            query_waitlist.setString(1, escape(waitlist_id));
            ResultSet result_waitlist = query_waitlist.executeQuery();
            if (result_waitlist.next()) {
                String course_name =result_waitlist.getString("course_name");
                String course_num =result_waitlist.getString("course_num");
                String department =result_waitlist.getString("department");
                String course_limit = result_waitlist.getString("course_limit");
                String kind = result_waitlist.getString("kind");
                out.println("waitlist_id: "+ waitlist_id);
                out.println(" course_name: "+ course_name);
                out.println(" course_num: "+ course_num);
                out.println(" department: "+ department);
                out.println(" course_limit: "+ course_limit);
                out.println(" kind: "+ kind);
                //out.println("<button onclick=remove_waitlist('"+waitlist_id+"')>Remove</button>");
                out.println("<button onclick=view_waitlist('"+waitlist_id+"')>View</button>");
                //out.println("<form method='post' action='"+selfUrl+"'><button type='submit' name='view_waitlist' value='"+waitlist_id+"'>View</button></form>");
                out.println("<br>");
            }
        }
        out.println("<hr>");
        return waitlist_id;
    }

    private int isLoggedIn(HttpSession session){
        String session_bid = (String)session.getAttribute("session_bid");
        if (session_bid!=null){
            return 1;
        } else {
            return -1;
        }
    }
    private void printPageHeader(PrintWriter out,HttpSession session) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Walter Waitlist</title>");
        out.println("<h1><a href='/walter/servlet/WW_Signin'>Walter Waitlist</a></h1>");
        if (isLoggedIn(session)>0){
            String type = (String)session.getAttribute("session_type");
            if (type.equals("student")){
                out.println("<a href='/walter/servlet/WW_StudentHome'>Dashboard</a>");
            } else {
                out.println("<a href='/walter/servlet/WW_ProfHome'>Dashboard</a>");
                out.println("<a href='/walter/servlet/WW_CreateWaitlist'>Create Waitlist</a>");
            }
            out.println("<a href='/walter/servlet/WW_WaitlistSearch'>Browse</a>");
            out.println("<a href='/walter/servlet/WW_Logout'>Log out</a>");
        }
        out.println("<link rel='stylesheet' href='//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css'>");
        out.println("<script src='//code.jquery.com/jquery-1.10.2.js'></script>");
        out.println("<script src='//code.jquery.com/ui/1.10.4/jquery-ui.js'></script>");
        out.println("</head><hr>");
        out.println("<body>");
    }

    
  // ========================================================================
  // PROCESS THE REQUEST DATA
  // ========================================================================
   
    
    private void removeStudent(HttpServletRequest req, PrintWriter out, Connection con, String remove_bid,String waitlist_id)
    throws SQLException
    {
            PreparedStatement query = con.prepareStatement
            ("DELETE from Waitlist where waitlist_id=? and student_bid=?");
            query.setString(1, escape(waitlist_id) );
            query.setString(2, escape(remove_bid) );
        int result = query.executeUpdate();
            out.println("Student Removed<br>");
    }

    private void removeWaitlist(HttpServletRequest req, PrintWriter out, Connection con, String waitlist_id)
    throws SQLException
    {
        PreparedStatement query = con.prepareStatement
        ("DELETE from Course where crn=?");
        query.setString(1, escape(waitlist_id) );
        int result = query.executeUpdate();
        out.println("Waitlist Removed<br>");
    }
    
    private void processSort(HttpServletRequest req, PrintWriter out, Connection con, String [] sort, String waitlist_id)
                             throws SQLException
    {
        for (int i=0; i<ArrayUtils.getLength(sort); i++){
        PreparedStatement query = con.prepareStatement
        ("UPDATE Waitlist SET rank = ? where waitlist_id=? and student_bid=?");
            
            query.setString(1, String.valueOf(i+1) );
            query.setString(2, escape(waitlist_id) );
            query.setString(3, escape(sort[i]) );
            int result = query.executeUpdate();
         }
        out.println("Order Saved!<br>");
    }
    
    
  private void processForm(HttpServletRequest req, PrintWriter out, Connection con, String waitlist_id, String selfUrl )
    throws SQLException
  {
    try {
      printList( con,out,waitlist_id, selfUrl);
      printEmail( con, out,waitlist_id);
    } catch (Exception e) {
      out.println("<p>Error:"+e);
    }    
  }
  
  // ========================================================================
  // HELPER METHOD: PRINTING THE WAITLISTS AND EMAILS
  // ========================================================================

  // Prints list of students on searched for waitlist
  private void printList(Connection con, PrintWriter out, String waitlist_id, String selfUrl)
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
        ("select * from Waitlist where waitlist_id = '"+waitlist_id+"' order by rank asc");
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
                out.println("<li id='"+student_bid+"'>"+rank+
                      " "+student_name+" "+major_minor+" "+student_class+" "+explanation+
                            "<button onclick=remove_student('"+student_bid+"')>Remove</button></li>");
              } else {
                out.println("result was not null"); //when there's no result, print error statement
              }
        }
        out.println("</ul>");
        //out.println("<form method='post' action='/walter/servlet/WW_ProfHome'><button  type='submit' name='waitlist_id' value="+waitlist_id+">Start Over</button></form>");
        out.println("<button onclick=start_over('"+waitlist_id+"')>Refresh Order</button>");
        out.println("<button id='sort'>Save Order</button>");
        out.println("<button id='email_button'>View Email List</button>");
        out.println("<button onclick=remove_waitlist('"+waitlist_id+"')>Remove Waitlist</button><br>");
        printScript(out, selfUrl);
    } catch (SQLException e) {
      out.println("<p>Error: "+e);
    }
  }

private void printScript(PrintWriter out, String selfUrl){
        out.println("<script>$('#students').sortable();</script>");
        out.println("<script>"+
                    "function post_to_url(path, params, method) {"+
                    "method = method || 'post';"+
                    "var form = document.createElement('form');"+
                    "form.setAttribute('method', method);"+
                    "form.setAttribute('action', path);"+
                    "for(var key in params) {"+
                    "     if(params.hasOwnProperty(key)) {"+
                    "        var hiddenField = document.createElement('input');"+
                    "        hiddenField.setAttribute('type', 'hidden');"+
                    "        hiddenField.setAttribute('name', key);"+
                    "        hiddenField.setAttribute('value', params[key]);"+
                    "        form.appendChild(hiddenField);"+
                    "    }"+
                    "}"+
                    "document.body.appendChild(form);"+
                    "form.submit();"+
                    "}");
        out.println("function remove_student(bid) {"+
                    "if (confirm('Are you sure you want to remove student from waitlist?')) {"+
                        "post_to_url('"+selfUrl+"', {remove_bid:bid.toString()});"+
                    "} else {"+
                        "}"+
                    "};");
        out.println("function remove_waitlist(waitlist_id) {"+
                    "if (confirm('Are you sure you want to remove the waitlist?')) {"+
                    "post_to_url('"+selfUrl+"', {remove_waitlist:waitlist_id.toString()});"+
                    "} else {"+
                    "}"+
                    "};");
    out.println("function start_over(waitlist_id) {"+
                "post_to_url('"+selfUrl+"', {waitlist_id:waitlist_id.toString()});"+
                "};");
    out.println("function view_waitlist(waitlist_id) {"+
                "post_to_url('"+selfUrl+"', {view_waitlist:waitlist_id.toString()});"+
                "};");
        out.println("$(document).ready(function(){"+
                    "$('#sort').click(function(){"+
                        "var sorted = $( '#students' ).sortable( 'toArray' );"+
                        "post_to_url('"+selfUrl+"',{new_order:sorted});"+
                        "});"+
                    "$('#email_button').click(function(){"+
                        "alert($('#email_list').text());"+
                        "});"+
                    "});"+
                    "</script>");
    }
  // Prints emails of all students on waitlist
  private void printEmail(Connection con, PrintWriter out, String waitlist_id)
    throws SQLException
  {
    try {
      Statement query = con.createStatement();
      ResultSet result = query.executeQuery
        ("select email from Person,Waitlist where Person.bid=Waitlist.student_bid and Waitlist.waitlist_id ="+waitlist_id);
      out.println("<div id='email_list' style='display: none;'> Students Emails:");
      while (result.next()) {
    out.println(result.getString("email")+";");
      }
      out.println("</div><br>");
    }
    catch (SQLException e) {
      out.println("<p>Error: "+e);
    }
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
