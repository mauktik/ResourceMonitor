package resourceMonitor;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class MonitorResults 
{
	private static Logger logger = Logger.getLogger(MonitorResults.class);
	private AbstractQueue<AggregatedResults> queue = null;
	private AggregatedResults currentResults = null;
	
	public MonitorResults(AbstractQueue<AggregatedResults> queue)
	{
		this.queue = queue;
	}
	
	public void beginSession()
	{
		logger.debug("Starting monitor results session");
		this.currentResults = new AggregatedResults();
	}
	
	public void endSession()
	{
		logger.debug("Stopping monitor results session");
		this.queue.add(this.currentResults);
		this.currentResults = null;
	}
	
	public void add(String name, List<InstanceData> instanceData)
	{
		logger.debug(String.format("Adding results for counter = %s", name));
		this.currentResults.monitorResults.add(new MonitorResult(name, instanceData));
	}
	
	public void add(Map<String, List<InstanceData>> data)
	{
		logger.debug("Adding results for counters");	
	    Iterator<Entry<String, List<InstanceData>>> resultIterator = data.entrySet().iterator();
	    while (resultIterator.hasNext())
	    {
	    	Entry<String, List<InstanceData>> pair = resultIterator.next();
	    	this.add(pair.getKey(), pair.getValue());
	    }		
	}
}

