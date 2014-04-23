package epub.reader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class EpubHandler
{
  EpubLibrary _lib;
	
  public EpubHandler( EpubLibrary lib )
  {
	  _lib = lib;
  }  
	
  public void handle( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException
  {
	System.out.println( "===" );
    System.out.println( "requestURI: >>" + req.getRequestURI() + "<<" );
    System.out.println( "pathInfo: >>" + req.getPathInfo() + "<<" );
    System.out.println( "contextPath: >>" + req.getContextPath() + "<<" );
    System.out.println( "servletPath: >>" + req.getServletPath() + "<<" );
    
    // System.out.println( "contentType: " + request.getContentType());  // NB: always NULL (thus, of no use)
 
    
    String pathInfo = req.getPathInfo();

  try
  {
      
    if( pathInfo.equals( "/favicon.ico" ))
    {
    	 // todo: return 404 not found
    	res.setStatus( HttpServletResponse.SC_NOT_FOUND );
        // baseRequest.setHandled(true);
        System.out.println( "404 NOT FOUND" );
    }
    else if( pathInfo.equals( "/" ) || 
    		 pathInfo.equals( "/books" ) ||
    		 pathInfo.equals( "/b" )  || 
    		 pathInfo.equals( "/ls" ) ||
    		 pathInfo.equals( "/l" ) ||
    		 pathInfo.equals( "/dir" ) ||
    		 pathInfo.equals( "/d" ) ||
    		 pathInfo.startsWith( "/index" )) 
    {
    	String html = _lib.fetchBooks();

        res.setContentType("text/html;charset=utf-8");
    
	    PrintWriter writer = res.getWriter();     
        writer.print( html );
        writer.println();
        // writer.flush();  // needed? usefull? why? why not??
  
        // response.setStatus(HttpServletResponse.SC_OK);
        // baseRequest.setHandled(true);

	    System.out.println( "200 OK" );
    }
    else
    {
        // split pathInfo  into zipName and entryName
    	int pos = pathInfo.indexOf( '/', 1 ); // NB: skip leading slash
    	System.out.println( "pos: " + pos );
    	
    	String bookName;
    	String entryName;
    	
    	if( pos == -1 ) {
    	    bookName  = pathInfo.substring( 1 );
    		entryName = "";
    	}
    	else {
        	bookName   = pathInfo.substring( 1, pos ); // NB: skip leading slash e.g. /london.epub becomes london.epub
    		entryName  = pathInfo.substring( pos+1 );  // NB: skip leading slash e.g. /title_page.html becomes title_page.html
    	}
    	
    	System.out.println( "bookName: >>" + bookName + "<<" );
    	System.out.println( "entryName: >>" + entryName + "<<" );
    	
    	  if( entryName.equals( "" ) ||  // NB: support empty string "" for /bookname, for example
    		  entryName.equals( "contents" ) ||
    	      entryName.equals( "toc" )) 
    	  {
    		  String html = _lib.fetchBookContents( bookName );

    	      res.setContentType("text/html;charset=utf-8");
    	      // response.setContentType("text/html");
    	      
    	      PrintWriter writer = res.getWriter();     
    	      writer.print( html );
    	      writer.println();
    	      //  writer.flush();   // needed? usefull? why? why not??
    		  
    	      // response.setStatus(HttpServletResponse.SC_OK);    	      
    	      // baseRequest.setHandled(true);

    		  System.out.println( "200 OK" );
    	  }
    	  else
    	  {    
    		if( entryName.endsWith( ".html") ||
    			entryName.endsWith( ".xhtml") || 
    			entryName.endsWith( ".htm") )
    		{
      	      res.setContentType("text/html;charset=utf-8");
    	      // response.setContentType("text/html");
    	      
    	      PrintWriter writer = res.getWriter();     
      		  _lib.copyBookEntryAndRewriteUrls( writer, bookName, entryName );

      		  res.setStatus(HttpServletResponse.SC_OK);
    			
         	    System.out.println( "200 OK" );

         	    writer.flush();  // needed? usefull? why? why not??
           	    writer.close();
    		}
    		else
    		{
        	   	OutputStream out = res.getOutputStream(); 

       		 _lib.copyBookEntry( out, bookName, entryName );

               res.setStatus(HttpServletResponse.SC_OK);
           
       	    out.flush();
       	    out.close();
       	
               // response.setContentType("text/html;charset=utf-8");
           
               // response.getWriter().println("<h1>Hello World</h1>");

       	    // baseRequest.setHandled(true);
       	  
       	    System.out.println( "200 OK" );
    		}
    	  }
      }
	}
	catch( Exception ex )
	{
		System.out.println( "500 INTERNAL SERVER ERROR" );
		System.out.println( "*** error: " + ex.toString() );
		
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        // baseRequest.setHandled(true);
	}
    
    
  } // method handle
} // class EpubHandler
