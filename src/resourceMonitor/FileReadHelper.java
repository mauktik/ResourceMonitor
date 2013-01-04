// ---------------------------------------------------------------------------
// Copyright 2012 Mauktik Gandhi
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ---------------------------------------------------------------------------
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
