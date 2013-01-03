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
