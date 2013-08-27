package epub.reader;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EpubServlet extends HttpServlet {

	
  public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException 
  {
	String booksPath = getServletConfig().getInitParameter( "booksPath" );

	System.out.println( "[EpubServlet.doGet] booksPath: " + booksPath );
	
	if( booksPath == null )
		booksPath = "c:/work/epub-reader/books";
	
	System.out.println( "[EpubServlet.doGet] servletPath: " + req.getServletPath() );
	System.out.println( "[EpubServlet.doGet] contextPath: " + req.getContextPath() );
	
	EpubLibrary lib     = new EpubLibrary( booksPath, req.getContextPath() );
	EpubHandler handler = new EpubHandler( lib );
	
	handler.handle( req,res );	  
  }
		      
	
} // class EpubServlet
