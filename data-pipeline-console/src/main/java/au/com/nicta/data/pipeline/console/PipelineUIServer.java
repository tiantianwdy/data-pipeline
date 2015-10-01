package au.com.nicta.data.pipeline.console;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tiantian on 17/08/15.
 */
public class PipelineUIServer {

    public static void start() throws Exception{

        Server server = new Server(8080);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("../data-pipeline-console/src/main/webapp");
        context.setWelcomeFiles(new String[]{ "webapp/html/index.html" });
        context.setDescriptor("webapp/WEB-INF/web.xml");
        context.setParentLoaderPriority(true);

//        ResourceHandler resourceHandler = new ResourceHandler();
//        resourceHandler.setDirectoriesListed(true);
//        resourceHandler.setWelcomeFiles(new String[]{ "webapp/res/html/index.html" });
//        resourceHandler.setResourceBase("webapp/res");
//
//        ServletHandler handler = new ServletHandler();
//        handler.addServletWithMapping(HelloServlet.class, "/hello/*");
        server.setHandler(context);

        server.start();
        server.join();
    }

    @SuppressWarnings("serial")
    public static class HelloServlet extends HttpServlet
    {
        @Override
        protected void doGet( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                IOException
        {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello from HelloServlet</h1>");
        }
    }


    public static void main(String[] args) throws Exception {
        PipelineUIServer.start();
    }
}
