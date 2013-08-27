

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import epub.reader.EpubServlet;


public class Test {

  public static void main(String[] args) throws Exception
  {
	System.out.println( "hello epub reader v5" );
		
	Server server = new Server( 4444 );

	    
	ServletContextHandler ctx = new ServletContextHandler( ServletContextHandler.SESSIONS );
    ctx.setContextPath( "/test" );
        
    ctx.addServlet( new ServletHolder( new EpubServlet()), "/*" );

    server.setHandler( ctx );
 
	    
    server.start();
    server.join();
  }

}
