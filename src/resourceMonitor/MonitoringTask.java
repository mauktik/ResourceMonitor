package resourceMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class MonitoringTask extends TimerTask 
{
	private static Logger logger = Logger.getLogger(MonitoringTask.class);
	
	private List<ResourceMonitor> monitors = new ArrayList<ResourceMonitor>();
	private long samplesCollected;
	private Configuration configuration;
	private MonitorResults results;
	
	public MonitoringTask(Configuration configration, MonitorResults results)
	{
		this.samplesCollected = 0;
		this.configuration = configration;
		this.results = results;
		
		this.monitors.add(new ProcessResourceMonitor(configuration));
		this.monitors.add(new NetworkResourceMonitor(configuration));
	}
	
	@Override
	public void run()
	{
		logger.info("Collecting data for all monitors");
		this.samplesCollected++;
		for(ResourceMonitor monitor : monitors)
		{
			monitor.collect();
		}
		
		if (this.samplesCollected % configuration.getSamplesToAverage() == 0)
		{
			logger.info("Aggregating results across all monitors");
			this.results.beginSession();
			for (ResourceMonitor monitor: monitors)
			{
				monitor.aggregate(this.results);
			}
			this.results.endSession();
		}
	}
	
	public void stop()
	{
		logger.info("Stopping monitors");
		for (ResourceMonitor monitor : this.monitors)
		{
			monitor.stop();
		}
	}
}
