import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.io.FileUtils;

public class WW_create_waitlist extends HttpServlet
{
    
    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        res.setContentType("text/html");
        res.setHeader("pragma", "no-cache");
        PrintWriter out = res.getWriter();
        ServletContext context = getServletContext(); // inherited method

        String path = context.getContextPath();
        out.println("<p>FYI: path is "+path);
        
        try {
            String template = loadTemplate("templates/create_waitlist.html");
		out.println(template);

         /*   HashMap<String,String> playerInfo;

            // randomly alternate
            long now = System.currentTimeMillis();
            if(now % 2 == 0) {
                playerInfo = playerInfo("roger");
            } else {
                playerInfo = playerInfo("rafa");
            }

            out.println(FilledTemplate(template,playerInfo));
*/        }
        catch (Exception e) {
            e.printStackTrace(out);
            out.println("<p>Info: Homedir is "+System.getProperty("user.home"));
            out.println("<p>Info: current dir is "+System.getProperty("user.dir"));
        }
    }

    /** This method loads the contents of the template file and returns
     * it.  It works by getting the servlet "context" which is an object
     * that represents the whole web app, which amounts to a directory in
     * /usr/share/tomcat6/webapps, such as /usr/share/tomcat6/webapps/foo
     * with a URL like cs.wellesley.edu:8080/foo.  But we wouldn't want to
     * hard-code absolute paths like /usr/share/tomcat6/webapps/foo, so
     * instead we ask the context object to map a "virtual path" to a real
     * path.  The virtual path is relative to the webapp root, which is
     * just that directory that we didn't want to hard-code.
     */

    private String loadTemplate(String virtualPath) {
        ServletContext context = getServletContext(); // inherited method
        String real = context.getRealPath(virtualPath);
        try {
            File tmplFile = new File(real);
            return FileUtils.readFileToString(tmplFile,null);
        }
        catch (IOException e) {
            // this.e = e;
            return "Failed to load "+real+" from "+virtualPath;
        }
    }
    
    public static String FilledTemplate(String tmpl, HashMap<String,String> hm)
    {
        StrSubstitutor sub = new StrSubstitutor(hm);
        return sub.replace(tmpl);
    }

    public static HashMap<String,String> playerInfo(String name) {
        HashMap<String,String> hm = new HashMap<String,String>();
        if( name.equals("roger") ) {
            hm.put("playerName","Roger Federer");
            hm.put("titles","78");
        } else if( name.equals("rafa") ) {
            hm.put("playerName","Rafael Nadal");
            hm.put("titles","60");
        } else {
            hm.put("playerName","???");
            hm.put("titles","??");
        }
        return hm;
    }

    public static String FileInfo(String path) {
        File f = new File(path);
        if( f.canRead() ) {
            try {
                String s = FileUtils.readFileToString(f,null);
                return ("File "+path+" has length "+s.length());
            }
            catch (IOException e) {
                return e.toString();
            }
        } else {
            return ("I can't read file "+f);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doRequest(req,res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doRequest(req,res);
    }

    public static void main(String args[]) {
        for(String a : args) {
            System.out.println(FileInfo(a));
        }
    }
}
