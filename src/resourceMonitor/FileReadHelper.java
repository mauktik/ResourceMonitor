package resourceMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;

public class FileReadHelper 
{
	private static Logger logger = Logger.getLogger(FileReadHelper.class);
	
	public static String getFirstLine(File file)
	{
		try
		{
		    BufferedReader reader = new BufferedReader(new FileReader(file), 1024);
		    String line = reader.readLine();
		    reader.close();
		    return (line != null) ? line : "";
		}
		catch (IOException ex)
		{
			logger.warn(String.format("Failed to read contents from file %s. Returning a default empty string.", file.toString()), ex);
			return "";
		}
	}
	
	public static String getLineBeginningWith(File file, String value)
	{
		try
		{
		    BufferedReader reader = new BufferedReader(new FileReader(file));
		    
		    String line;
		    while ((line = reader.readLine()) != null)
		    {
		    	if (line.startsWith(value))
		    	{
		    		break;
		    	}
		    }
		    
		    reader.close();
		    return (line != null) ? line : "";
		}
		catch (IOException ex)
		{
			logger.warn(String.format("Failed to read contents from file %s. Returning a default empty string.", file.toString()), ex);
			return "";
		}		
	}
}
