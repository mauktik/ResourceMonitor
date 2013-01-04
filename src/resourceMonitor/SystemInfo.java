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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class SystemInfo 
{
	private long bytesPerPage = 0;
	private int processorCount;
	private String machineName = "";
	
	public SystemInfo() throws IOException
	{
		this.bytesPerPage = this.getConf("PAGESIZE");
		this.processorCount = Runtime.getRuntime().availableProcessors();
		this.machineName = InetAddress.getLocalHost().getHostName();
	}
	
	public long getBytesPerPage()
	{
		return this.bytesPerPage;
	}
	
	public int getProcessorCount()
	{
		return this.processorCount;
	}
	
	public String getMachineName()
	{
		return this.machineName;
	}
	
	private long getConf(String parameter) throws IOException
	{
		Process proc = Runtime.getRuntime().exec("getconf " + parameter);
		
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String output = "";
        String line;
        while ((line = reader.readLine()) != null)
        {
        	output += line;
        }
        reader.close();
        
        return Long.parseLong(output);
	}
}
