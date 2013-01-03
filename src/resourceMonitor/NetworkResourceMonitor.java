package resourceMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import resourceMonitor.parsers.NetDevInterfaceReader;

public class NetworkResourceMonitor implements ResourceMonitor 
{
	private static Logger logger = Logger.getLogger(NetworkResourceMonitor.class);
	private Configuration configuration = null;
	private Map<String, InterfaceInfo> networkInterfaceInfoMap = new HashMap<String, InterfaceInfo>();
	private File netDevFile = new File("/proc/net/dev");
	
	public static String ReceiveBytesKey = "Network\\Bytes received per second";	
	public static String ReceivePacketsKey = "Network\\Packets received per second";
	public static String ReceiveErrorsKey = "Network\\Receive errors per second";
	public static String ReceiveDropKey = "Network\\Drop received packets per second";
	public static String TransmitBytesKey = "Network\\Bytes transmitted per second";	
	public static String TransmitPacketsKey = "Network\\Packets transmitted per second";
	public static String TransmitErrorsKey = "Network\\Transmit errors per second";
	public static String TransmitDropKey = "Network\\Drop transmitted packets per second";	
	
	public NetworkResourceMonitor(Configuration configuration)
	{
		logger.debug("Creating NetworkResourceMonitor");
		this.configuration = configuration;
	}
	
	@Override
	public void collect() 
	{
		logger.debug("Collecting network statistics");
		this.collectNetDevInfo();
	}

	@Override
	public void aggregate(MonitorResults results) 
	{
		logger.debug("Aggregating network statistics results");
		Map<String, List<InstanceData>> aggregatedResults = new HashMap<String, List<InstanceData>>();
		
	    Iterator<Entry<String, InterfaceInfo>> iterator = this.networkInterfaceInfoMap.entrySet().iterator();
	    while (iterator.hasNext())
	    {
	    	iterator.next().getValue().aggregate(aggregatedResults);
	    }
	    
	    results.add(aggregatedResults);
	}

	@Override
	public void stop() 
	{
		logger.debug("Stopping monitor");
	}
	
	private void collectNetDevInfo()
	{
		if (this.netDevFile.exists())
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new FileReader(this.netDevFile), 1024);
				
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					this.collectNetDevInterfaceInfo(line);
				}
				
				reader.close();
			}
			catch (IOException ex)
			{
				logger.warn("Failed to read /proc/net/dev.", ex);
				return;
			}
		}
	}
	
	private void collectNetDevInterfaceInfo(String line)
	{
		NetDevInterfaceReader reader = NetDevInterfaceReader.create(line);
		if (reader != null)
		{
			if (!this.networkInterfaceInfoMap.containsKey(reader.getName()))
			{
				this.networkInterfaceInfoMap.put(reader.getName(), new InterfaceInfo(configuration, reader.getName()));
			}
			
			InterfaceInfo interfaceInfo = this.networkInterfaceInfoMap.get(reader.getName());
			this.addSample(interfaceInfo, ReceiveBytesKey, reader.getReceiveBytes());
			this.addSample(interfaceInfo, ReceivePacketsKey, reader.getReceivePackets());	
			this.addSample(interfaceInfo, ReceiveErrorsKey, reader.getReceiveErrors());	
			this.addSample(interfaceInfo, ReceiveDropKey, reader.getReceiveDrops());	
			this.addSample(interfaceInfo, TransmitBytesKey, reader.getTransmitBytes());	
			this.addSample(interfaceInfo, TransmitPacketsKey, reader.getTransmitPackets());	
			this.addSample(interfaceInfo, TransmitErrorsKey, reader.getTransmitErrors());	
			this.addSample(interfaceInfo, TransmitDropKey, reader.getTransmitDrops());
		}		
	}
	
	private void addSample(InterfaceInfo info, String key, long newValue)
	{
		if (info.containsValue(key))
		{
			info.addSample(key, this.getValuePerSecond(info.getValue(key), newValue));
		}
		info.setValue(key, newValue);
	}
	
	private long getValuePerSecond(long oldValue, long newValue)
	{
		return (long)((double)((newValue - oldValue) * 1000)/(double)this.configuration.getCollectionPeriod());
	}
	
	private class InterfaceInfo
	{
		private Configuration configuration = null;
		private String name = null;
		private Map<String, SampleCollection> infoTable = new HashMap<String, SampleCollection>();
		private Map<String, Long> valueTable = new HashMap<String, Long>();
		
		public InterfaceInfo(Configuration configuration, String name)
		{
			this.configuration = configuration;
			this.name = name;
		}
		
		public void setValue(String name, long value)
		{
			this.valueTable.put(name, value);
		}
		
		public long getValue(String name)
		{
			return this.valueTable.get(name);
		}
		
		public boolean containsValue(String name)
		{
			return this.valueTable.containsKey(name);
		}
		
		public void addSample(String name, long value)
		{
			if (!this.infoTable.containsKey(name))
			{
				this.infoTable.put(name, new SampleCollection(this.configuration.getSamplesToAverage() + 1));
			}
			
			this.infoTable.get(name).addSample(value);
		}
		
		public void aggregate(Map<String, List<InstanceData>> results)
		{			
		    Iterator<Entry<String, SampleCollection>> it = this.infoTable.entrySet().iterator();

		    while (it.hasNext())
		    {
		        Entry<String, SampleCollection> pair = (Entry<String, SampleCollection>)it.next();
		        
		        if (!pair.getValue().isEmpty())
		        {
		        	if (!results.containsKey(pair.getKey()))
		        	{
		        		results.put(pair.getKey(), new ArrayList<InstanceData>());
		        	}
		        	results.get(pair.getKey()).add(new InstanceData(this.name, pair.getValue().getAverage()));
		        	pair.getValue().clear();
		        }
		    }
		}
	}
}
