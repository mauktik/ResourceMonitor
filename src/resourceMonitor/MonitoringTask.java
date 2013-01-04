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
