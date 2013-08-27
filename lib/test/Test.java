package test;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import epub.reader.EpubServlet;


public class Test {

  public static void main(String[] args) throws Exception
  {
	System.out.println( "hello epub reader v1" );
		
	Server server = new Server( 4444 );

	Context ctx =  new Context( server, "/test", Context.SESSIONS );        
    ctx.addServlet( new ServletHolder( new EpubServlet()), "/*" );

	    
    server.start();
    server.join();
  }

}
