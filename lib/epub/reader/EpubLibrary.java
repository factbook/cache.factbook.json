package epub.reader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubLibrary 
{
	String _booksPath;
	String _ctxPath;

	public EpubLibrary( String booksPath )
	{
		this( booksPath, "" );
	}
	
	public EpubLibrary( String booksPath, String ctxPath )
	{
		_booksPath = booksPath;
		_ctxPath   = ctxPath;   // web context path
		
		System.out.println( "[EpubLibrary.ctor] booksPath: " + booksPath );
		System.out.println( "[EpubLibrary.ctor] ctxPath: " + ctxPath );
	}
	
	public String fetchBooks() throws Exception
	{
		System.out.println( "call fetchBooks()" );

		File root = new File( _booksPath );
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept( File dir, String name ) 
			{
			   if( name.toLowerCase().endsWith( ".epub" ) ||
				   name.toLowerCase().endsWith( ".zip" )) 
				 return true;
			   else
				return false;			
			}
		};
		
		File books[] = root.listFiles( filter );

        StringBuilder buf = new StringBuilder();
        buf.append( "<h1>" + books.length + " Books</h1>\n");
        System.out.println( "=== " + _booksPath + " ===" );
		
        System.out.println( "[EpubLibrary.fetchBooks] ctxPath: " + _ctxPath );
        
		for( File book : books )
		{
			System.out.println( "book: " + book.getName() );
			System.out.println( "bookFullpath: " + book.getCanonicalPath() );
			
			// remove .epub or .zip extension
			String bookName = book.getName().replace(".epub", "").replace(".zip", "");
			
			String line = String.format( "<a href='%s/%s'>%s</a>",
					_ctxPath,
					bookName,
					bookName );
			
			System.out.println( line );
			
			buf.append( "<p>" + line + "</p>\n" );
		}

		System.out.println( "=== end ===" );
		return buf.toString();
	}
	
	public String fetchBookContents( String bookName ) throws Exception
	{
		return fetchBookContents( bookName, false );  // filter by default - only show .xhtml|.html docs
	}
	
	
	private String getBookFullpath( String bookName )
	{
       String bookFullpath = _booksPath+"/"+bookName+".epub";
       File f = new File(bookFullpath); 
       
       if( f.exists() == false )  {
    	// try .zip as fallback
    	   bookFullpath = _booksPath+"/"+bookName+".zip";
       }
       System.out.println( "bookFullpath: " + bookFullpath );		
       return bookFullpath;	 
	}
	
	public String fetchBookContents( String bookName, boolean showAllFlag ) throws Exception
	{
		System.out.println( "call fetchBookContents()" );
		
		ZipFile zipFile = new ZipFile( getBookFullpath( bookName ) );

		Enumeration entries = zipFile.entries();
		
		StringBuilder buf = new StringBuilder();
        
        buf.append( "<h1>" + bookName + "</h1>\n" );
        System.out.println( "=== " + bookName + " ===" );
        
		while( entries.hasMoreElements() ) 
        {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            
            String line = String.format( "<a href='%s/%s/%s'>%s</a> | %d bytes | %TD",
            		_ctxPath,
            		bookName,
            		zipEntry.getName(),
                    zipEntry.getName(), 
                    zipEntry.getSize(),
                    new Date( zipEntry.getTime() ));

            System.out.println( line );
            
            if( zipEntry.getName().endsWith( "xhtml") ||
            	zipEntry.getName().endsWith( "html" ) ||
            	zipEntry.getName().endsWith( "htm" ) ||
            	showAllFlag == true )
            {            
              buf.append( "<p>" + line + "</p>\n" );
            }
        }

		System.out.println( "=== end ===" );
		return buf.toString();
	}
	
	public void copyBookEntryAndRewriteUrls( Writer writer, String bookName, String entryPath ) throws Exception
	{
		System.out.println( "call copyBookEntry()" );
	    
		System.out.println( "=== " + entryPath + " ===" );
		
		ZipFile zipFile = new ZipFile( getBookFullpath( bookName ) );

		ZipEntry zipEntry = zipFile.getEntry( entryPath );
		            
        InputStream in        = zipFile.getInputStream( zipEntry );
        BufferedReader reader = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );  // note: for now always assumes utf-8!!!!

        String base = _ctxPath + "/" + bookName + "/";
        System.out.println( " rewrite absolute urls - add >" + base +"<" );
        

    	// replace slash (/) with new base  if slash (/) follows pattern:
    	//   src='/  or src="/
    	//   href='/ or href="/        
        //
        // todo: allow spaces e.g. src = '/  -- why? why not?

        Pattern linkPattern = Pattern.compile( "((?:src|href)=['\"])(\\/)(\\w+)", Pattern.CASE_INSENSITIVE );
        
        String line;
        while( (line=reader.readLine()) != null ) {
        	
        	// todo: check find a way to reuse matcher - how? 
        	//   can we use m.xxxx to use new string???
        	Matcher m = linkPattern.matcher(  line );
        	if( m.find() )
        	{
        	  m.reset();  // do NOT forget to reset; before new match round
        	  System.out.println( "  rewrite line before |" + line + "|" );

        	  line = m.replaceAll( "$1"+base+"$3" );
        	  
        	  System.out.println( "  rewrite line after  |" + line + "|" );
        	  // "(?<=['\"])\\/(?=\\w)"
        	}
        	writer.write( line );   // todo: check if we need to add newline ???
        }
        reader.close();
   		 
		System.out.println( "Done." );
	}
	
	public void copyBookEntry( OutputStream out, String bookName, String entryPath ) throws Exception 
	{
		System.out.println( "call copyBookEntry()" );
	    
		System.out.println( "=== " + entryPath + " ===" );
		
		ZipFile zipFile = new ZipFile( getBookFullpath( bookName ) );

		ZipEntry zipEntry = zipFile.getEntry( entryPath );
		            
        InputStream in = zipFile.getInputStream( zipEntry );

		int read = 0;
		byte[] bytes = new byte[1024];
		 
		while(( read = in.read( bytes )) != -1)  
		  out.write(bytes, 0, read);
				 
		System.out.println( "Done." );
	}
	
} // class EpubLibrary
